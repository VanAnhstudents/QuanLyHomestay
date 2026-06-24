package com.example.ql_homestay.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ql_homestay.MainActivity;
import com.example.ql_homestay.R;
import com.example.ql_homestay.model.TaiKhoan;
import com.example.ql_homestay.repository.AuthRepository;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity — màn hình Đăng nhập.
 * Layout: activity_login.xml
 * Flow:
 *   1. Nếu SessionManager.isLoggedIn() → bypass thẳng tới MainActivity.
 *   2. Nếu SharedPreferences có lưu username (ghi nhớ) → điền sẵn.
 *   3. Nhấn "Đăng nhập":
 *        a. Validate không rỗng.
 *        b. Gọi AuthRepository.login().
 *        c. Thành công → SessionManager.login() → startActivity(MainActivity) → finish().
 *        d. Thất bại → Snackbar lỗi.
 *   4. CheckBox "Ghi nhớ" → lưu/xóa username trong SharedPreferences.
 */
public class LoginActivity extends AppCompatActivity {
    // SharedPreferences key cho "Ghi nhớ đăng nhập"
    private static final String PREF_REMEMBER = "LalaHouseRemember";
    private static final String KEY_SAVED_USER = "savedUsername";
    private static final String KEY_REMEMBER = "rememberMe";

    // Views
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private CheckBox cbRemember;
    private MaterialButton btnLogin;

    // Dependencies
    private AuthRepository authRepo;
    private SessionManager session;
    private SharedPreferences rememberPrefs;

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── Khởi tạo dependencies trước khi setContentView để có thể bypass ──
        session = SessionManager.getInstance(this);
        authRepo = new AuthRepository(this);
        rememberPrefs = getSharedPreferences(PREF_REMEMBER, MODE_PRIVATE);

        // ── Nếu đã đăng nhập (app reopen) → vào thẳng MainActivity ──────────
        if (session.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();
        restoreRememberMe();
        setupClickListeners();
    }

    // Bind views
    private void bindViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);
    }

    // "Ghi nhớ đăng nhập" — restore từ SharedPreferences
    private void restoreRememberMe() {
        boolean remember = rememberPrefs.getBoolean(KEY_REMEMBER, false);
        cbRemember.setChecked(remember);
        if (remember) {
            String saved = rememberPrefs.getString(KEY_SAVED_USER, "");
            if (!TextUtils.isEmpty(saved)) {
                etUsername.setText(saved);
                etPassword.requestFocus();
            }
        }
    }

    // Click listeners
    private void setupClickListeners() {

        // Đăng nhập
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    // Login logic
    private void attemptLogin() {
        // 1. Lấy giá trị, clear lỗi cũ
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = getText(etUsername);
        String password = getText(etPassword);

        // 2. Validate client-side
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            if (valid) tilPassword.requestFocus(); // focus vào field lỗi đầu tiên
            valid = false;
        }
        if (!valid) return;

        // 3. Gọi AuthRepository.login()
        // (Chạy trên main thread; với DB nhỏ/offline demo, phép đọc SQLite rất nhanh và không gây ANR. Production: dùng AsyncTask/Executor.)
        TaiKhoan tk = authRepo.login(username, password);

        if (tk == null) {
            // Sai thông tin hoặc tài khoản bị khóa
            String msg = "Tên đăng nhập hoặc mật khẩu không đúng";
            showError(msg);
            tilPassword.setError(" ");   // đánh dấu đỏ ô mật khẩu
            etPassword.setText("");
            return;
        }

        // 4. Lưu "Ghi nhớ" vào SharedPreferences
        SharedPreferences.Editor ed = rememberPrefs.edit();
        if (cbRemember.isChecked()) {
            ed.putBoolean(KEY_REMEMBER, true);
            ed.putString( KEY_SAVED_USER, username);
        } else {
            ed.putBoolean(KEY_REMEMBER, false);
            ed.remove(KEY_SAVED_USER);
        }
        ed.apply();

        // 5. Lưu session
        String hoTen = authRepo.getHoTenByMaTK(tk.getMaTK(), tk.getTenDangNhap());
        session.login(tk.getMaTK(), tk.getTenDangNhap(), hoTen, tk.getVaiTro(), tk.getAvatar());

        // 6. Chuyển sang MainActivity
        goToMain();
    }

    // Navigation
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        // FLAG_ACTIVITY_NEW_TASK + CLEAR_TASK: xóa back-stack, người dùng
        // không thể nhấn Back quay lại Login sau khi đã đăng nhập.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Helpers
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    /**
     * Hiển thị Snackbar lỗi ở cuối màn hình.
     * Dùng sv_login_root (ScrollView) làm anchor để Snackbar không bị che bởi
     * bàn phím ảo.
     */
    private void showError(String message) {
        View root = findViewById(R.id.sv_login_root);
        if (root == null) root = getWindow().getDecorView().getRootView();
        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.text_primary, getTheme()))
                .setTextColor(getResources().getColor(R.color.text_on_primary, getTheme()))
                .show();
    }
}