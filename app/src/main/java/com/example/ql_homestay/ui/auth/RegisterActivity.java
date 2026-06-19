package com.example.ql_homestay.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ql_homestay.R;
import com.example.ql_homestay.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * RegisterActivity — màn hình Đăng ký tài khoản mới.
 * Layout: activity_register.xml (spec A2, ux_ui.md)
 * Flow:
 *   1. Validate tất cả trường phía client.
 *   2. Gọi AuthRepository.register().
 *   3. Thành công → Snackbar xanh → finish() (quay về LoginActivity).
 *   4. Thất bại (username đã tồn tại) → Snackbar lỗi.
 *   5. Nhấn "Đăng nhập ngay" → finish().
 * Validate rules:
 *   - Họ tên: không được rỗng
 *   - Email: đúng định dạng (Patterns.EMAIL_ADDRESS)
 *   - Mật khẩu: ≥ 6 ký tự
 *   - Xác nhận: phải khớp với mật khẩu
 */
public class RegisterActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Views
    private TextInputLayout tilFullname;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etFullname;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvGoLogin;

    // Dependencies
    private AuthRepository authRepo;

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepo = new AuthRepository(this);

        bindViews();
        setupClickListeners();
    }

    // Bind views
    private void bindViews() {
        tilFullname = findViewById(R.id.til_fullname);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        etFullname = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        tvGoLogin = findViewById(R.id.tv_go_login);
    }

    // Click listeners
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v  -> finish());
    }

    // Register logic
    private void attemptRegister() {
        // 1. Clear lỗi cũ
        tilFullname.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // 2. Lấy giá trị
        String fullname = getText(etFullname);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirm = getText(etConfirmPassword);

        // 3. Validate theo thứ tự từ trên xuống
        TextInputLayout firstError = null;

        if (TextUtils.isEmpty(fullname)) {
            tilFullname.setError("Vui lòng nhập họ và tên");
            firstError = tilFullname;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            if (firstError == null) firstError = tilEmail;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            if (firstError == null) firstError = tilEmail;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            if (firstError == null) firstError = tilPassword;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPassword.setError("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
            if (firstError == null) firstError = tilPassword;
        }

        if (TextUtils.isEmpty(confirm)) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            if (firstError == null) firstError = tilConfirmPassword;
        } else if (!confirm.equals(password)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            if (firstError == null) firstError = tilConfirmPassword;
        }

        // Có lỗi → dừng, focus vào trường lỗi đầu tiên
        if (firstError != null) {
            firstError.requestFocus();
            return;
        }

        // 4. Gọi AuthRepository.register()
        // Dùng email làm TenDangNhap nếu không tách thêm trường username; theo spec A2, không có trường username riêng → dùng email.
        boolean success = authRepo.register(email, email, password);

        if (success) {
            // Thành công → Snackbar xanh → đóng màn hình
            View root = getWindow().getDecorView().getRootView();
            Snackbar.make(root,
                            "Đăng ký thành công! Hãy đăng nhập để tiếp tục.",
                            Snackbar.LENGTH_LONG)
                    .setBackgroundTint(
                            getResources().getColor(R.color.status_success, getTheme()))
                    .setTextColor(
                            getResources().getColor(R.color.text_on_primary, getTheme()))
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar sb, int event) {
                            finish();    // đóng RegisterActivity, quay về Login
                        }
                    })
                    .show();
        } else {
            tilEmail.setError("Email này đã được đăng ký");
            tilEmail.requestFocus();
            showError("Email này đã được sử dụng. Vui lòng chọn email khác.");
        }
    }

    // Helpers
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showError(String message) {
        View root = getWindow().getDecorView().getRootView();
        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.text_primary, getTheme()))
                .setTextColor(getResources().getColor(R.color.text_on_primary, getTheme()))
                .show();
    }
}