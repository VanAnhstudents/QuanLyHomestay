package com.example.ql_homestay.ui.account;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.model.TaiKhoan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thêm / Sửa tài khoản.
 * Mode thêm mới (maTK == -1): hiện trường mật khẩu.
 * Mode sửa (maTK > 0): ẩn trường mật khẩu.
 */
public class AccountAddEditFragment extends Fragment {

    private static final String ARG_MA_TK = "maTK";
    private static final String[] VAI_TRO_DISPLAY = {"Admin", "Lễ tân", "Kế toán", "Nhân viên"};
    private static final String[] VAI_TRO_VALUE = {"Admin", "LeTan", "KeToan", "NhanVien"};

    public static AccountAddEditFragment newInstance(int maTK) {
        AccountAddEditFragment f = new AccountAddEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_TK, maTK);
        f.setArguments(args);
        return f;
    }

    private int maTK = -1;
    private TaiKhoanDAO taiKhoanDAO;
    private DatabaseHelper dbHelper;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private TextInputLayout tilUsername, tilPassword, tilConfirm;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirm;
    private MaterialAutoCompleteTextView dropdownVaiTro;
    private SwitchMaterial switchTrangThai;
    private MaterialButton btnLuu;
    private TextView labelPassword, labelConfirm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maTK = getArguments().getInt(ARG_MA_TK, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        taiKhoanDAO = new TaiKhoanDAO(dbHelper);

        tilUsername = view.findViewById(R.id.til_username);
        tilPassword = view.findViewById(R.id.til_password);
        tilConfirm = view.findViewById(R.id.til_confirm_password);
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirm = view.findViewById(R.id.et_confirm_password);
        dropdownVaiTro = view.findViewById(R.id.dropdown_vai_tro);
        switchTrangThai = view.findViewById(R.id.switch_trang_thai);
        btnLuu = view.findViewById(R.id.btn_luu);
        labelPassword = view.findViewById(R.id.label_password);
        labelConfirm = view.findViewById(R.id.label_confirm_password);

        setupBreadcrumb(view);
        setupVaiTroDropdown();

        if (maTK > 0) {
            // Mode sửa: ẩn trường mật khẩu
            labelPassword.setVisibility(View.GONE);
            tilPassword.setVisibility(View.GONE);
            labelConfirm.setVisibility(View.GONE);
            tilConfirm.setVisibility(View.GONE);
            btnLuu.setText("Cập nhật");
            loadExisting();
        }

        btnLuu.setOnClickListener(v -> onSave());
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText(maTK > 0
                ? "Trang chủ → Tài khoản → Chỉnh sửa"
                : "Trang chủ → Tài khoản → Thêm mới");
    }

    private void setupVaiTroDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, VAI_TRO_DISPLAY);
        dropdownVaiTro.setAdapter(adapter);
    }

    private void loadExisting() {
        executor.execute(() -> {
            TaiKhoan tk = taiKhoanDAO.findById(maTK);
            mainHandler.post(() -> {
                if (!isAdded() || tk == null) return;
                etUsername.setText(tk.getTenDangNhap());
                etEmail.setText(tk.getEmail());
                switchTrangThai.setChecked("HoatDong".equals(tk.getTrangThai()));
                // Pre-select vai trò
                for (int i = 0; i < VAI_TRO_VALUE.length; i++) {
                    if (VAI_TRO_VALUE[i].equals(tk.getVaiTro())) {
                        dropdownVaiTro.setText(VAI_TRO_DISPLAY[i], false);
                        break;
                    }
                }
            });
        });
    }

    private void onSave() {
        String username = text(etUsername);
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Tên đăng nhập không được để trống");
            valid = false;
        } else { tilUsername.setError(null); }

        if (maTK <= 0) {
            String password = text(etPassword);
            String confirm = text(etConfirm);
            if (TextUtils.isEmpty(password)) {
                tilPassword.setError("Mật khẩu không được để trống");
                valid = false;
            } else { tilPassword.setError(null); }
            if (!password.equals(confirm)) {
                tilConfirm.setError("Mật khẩu xác nhận không khớp");
                valid = false;
            } else { tilConfirm.setError(null); }
        }
        if (!valid) return;

        TaiKhoan tk = new TaiKhoan();
        if (maTK > 0) tk.setMaTK(maTK);
        tk.setTenDangNhap(username);
        tk.setEmail(text(etEmail));
        tk.setVaiTro(selectedVaiTro());
        tk.setTrangThai(switchTrangThai.isChecked() ? "HoatDong" : "Khoa");
        tk.setNgayTao(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        if (maTK <= 0) {
            tk.setMatKhau(text(etPassword));
        }

        btnLuu.setEnabled(false);
        executor.execute(() -> {
            boolean success;
            if (maTK > 0) {
                // Sửa: giữ nguyên mật khẩu nếu để trống
                if (maTK > 0) {
                    TaiKhoan existing = taiKhoanDAO.findById(maTK);
                    if (existing != null) tk.setMatKhau(existing.getMatKhau());
                }
                success = taiKhoanDAO.update(tk) > 0;
            } else {
                // Thêm mới
                success = taiKhoanDAO.insert(tk) > 0;
            }
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (success) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    btnLuu.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Lưu thất bại. Tên đăng nhập có thể đã tồn tại.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String selectedVaiTro() {
        String display = dropdownVaiTro.getText().toString().trim();
        for (int i = 0; i < VAI_TRO_DISPLAY.length; i++) {
            if (VAI_TRO_DISPLAY[i].equals(display)) return VAI_TRO_VALUE[i];
        }
        return "NhanVien";
    }

    @Override
    public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
