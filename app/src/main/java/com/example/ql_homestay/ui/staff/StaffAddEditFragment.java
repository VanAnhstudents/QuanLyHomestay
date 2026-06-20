package com.example.ql_homestay.ui.staff;

import android.app.DatePickerDialog;
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
import com.example.ql_homestay.model.NhanVien;
import com.example.ql_homestay.model.TaiKhoan;
import com.example.ql_homestay.repository.StaffRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * F3. Thêm / Sửa nhân viên.
 * Khi thêm mới (maNV == -1): cũng tạo TaiKhoan trong 1 transaction.
 */
public class StaffAddEditFragment extends Fragment {

    private static final String ARG_MA_NV = "maNV";
    private static final String[] CHUC_VU_DISPLAY = {"Quản lý", "Lễ tân", "Kế toán", "Dọn phòng", "Bảo vệ"};
    private static final String[] CHUC_VU_VALUE   = {"QuanLy", "LeTan", "KeToan", "DonPhong", "BaoVe"};

    public static StaffAddEditFragment newInstance(int maNV) {
        StaffAddEditFragment f = new StaffAddEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_NV, maNV);
        f.setArguments(args);
        return f;
    }

    private int maNV = -1;
    private StaffRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextInputLayout tilHoTen, tilUsername, tilPassword;
    private TextInputEditText etHoTen, etSdt, etEmail, etCccd, etNgayVaoLam, etDiaChi;
    private TextInputEditText etUsername, etPassword;
    private MaterialAutoCompleteTextView dropdownChucVu;
    private View cardTaiKhoan;
    private MaterialButton btnLuu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maNV = getArguments().getInt(ARG_MA_NV, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new StaffRepository(requireContext());

        tilHoTen      = view.findViewById(R.id.til_ho_ten);
        tilUsername   = view.findViewById(R.id.til_username);
        tilPassword   = view.findViewById(R.id.til_password);
        etHoTen       = view.findViewById(R.id.et_ho_ten);
        etSdt         = view.findViewById(R.id.et_sdt);
        etEmail       = view.findViewById(R.id.et_email);
        etCccd        = view.findViewById(R.id.et_cccd);
        etNgayVaoLam  = view.findViewById(R.id.et_ngay_vao_lam);
        etDiaChi      = view.findViewById(R.id.et_dia_chi);
        etUsername    = view.findViewById(R.id.et_username);
        etPassword    = view.findViewById(R.id.et_password);
        dropdownChucVu = view.findViewById(R.id.dropdown_chuc_vu);
        cardTaiKhoan  = view.findViewById(R.id.card_tai_khoan);
        btnLuu        = view.findViewById(R.id.btn_luu);

        setupBreadcrumb(view);
        setupDropdown();
        setupDatePicker();

        if (maNV > 0) {
            // Chế độ sửa: ẩn phần tài khoản, đổi text nút
            cardTaiKhoan.setVisibility(View.GONE);
            btnLuu.setText("Cập nhật");
            loadExisting();
        } else {
            cardTaiKhoan.setVisibility(View.VISIBLE);
        }

        btnLuu.setOnClickListener(v -> onSave());
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText(maNV > 0
                ? "Trang chủ → Nhân viên → Chỉnh sửa"
                : "Trang chủ → Nhân viên → Thêm mới");
    }

    private void setupDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, CHUC_VU_DISPLAY);
        dropdownChucVu.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etNgayVaoLam.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etNgayVaoLam.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    private void loadExisting() {
        executor.execute(() -> {
            NhanVien nv = repository.getStaffById(maNV);
            mainHandler.post(() -> {
                if (!isAdded() || nv == null) return;
                etHoTen.setText(nv.getHoTen());
                etSdt.setText(nv.getSdt());
                etEmail.setText(nv.getEmail());
                etCccd.setText(nv.getCccd());
                etNgayVaoLam.setText(nv.getNgayVaoLam());
                etDiaChi.setText(nv.getDiaChi());
                String cv = nv.getChucVu();
                for (int i = 0; i < CHUC_VU_VALUE.length; i++) {
                    if (CHUC_VU_VALUE[i].equals(cv)) {
                        dropdownChucVu.setText(CHUC_VU_DISPLAY[i], false);
                        break;
                    }
                }
            });
        });
    }

    private void onSave() {
        String hoTen = text(etHoTen);
        boolean valid = true;
        if (TextUtils.isEmpty(hoTen)) {
            tilHoTen.setError("Họ và tên không được để trống");
            valid = false;
        } else {
            tilHoTen.setError(null);
        }

        if (maNV <= 0) {
            // validate tài khoản
            if (TextUtils.isEmpty(text(etUsername))) {
                tilUsername.setError("Tên đăng nhập không được để trống");
                valid = false;
            } else { tilUsername.setError(null); }
            if (TextUtils.isEmpty(text(etPassword))) {
                tilPassword.setError("Mật khẩu không được để trống");
                valid = false;
            } else { tilPassword.setError(null); }
        }
        if (!valid) return;

        NhanVien nv = buildNhanVien(hoTen);
        btnLuu.setEnabled(false);

        if (maNV > 0) {
            // Chỉ update NhanVien
            executor.execute(() -> {
                int rows = repository.updateStaff(nv);
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    if (rows > 0) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        btnLuu.setEnabled(true);
                        Toast.makeText(requireContext(), "Lưu thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            // Tạo mới NhanVien + TaiKhoan trong 1 transaction
            TaiKhoan tk = buildTaiKhoan();
            executor.execute(() -> {
                long newMaNV = repository.createStaffWithAccount(nv, tk);
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    if (newMaNV > 0) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        btnLuu.setEnabled(true);
                        Toast.makeText(requireContext(), "Tạo thất bại. Tên đăng nhập có thể đã tồn tại.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    private NhanVien buildNhanVien(String hoTen) {
        NhanVien nv = new NhanVien();
        if (maNV > 0) nv.setMaNV(maNV);
        nv.setHoTen(hoTen);
        nv.setChucVu(selectedChucVu());
        nv.setSdt(text(etSdt));
        nv.setEmail(text(etEmail));
        nv.setCccd(text(etCccd));
        nv.setNgayVaoLam(text(etNgayVaoLam));
        nv.setDiaChi(text(etDiaChi));
        return nv;
    }

    private TaiKhoan buildTaiKhoan() {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(text(etUsername));
        tk.setMatKhau(text(etPassword));
        tk.setEmail(text(etEmail));
        // Vai trò mặc định theo chức vụ
        String cv = selectedChucVu();
        tk.setVaiTro("LeTan".equals(cv) ? "LeTan"
                : "KeToan".equals(cv) ? "KeToan" : "NhanVien");
        tk.setTrangThai("HoatDong");
        tk.setNgayTao(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));
        return tk;
    }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String selectedChucVu() {
        String display = dropdownChucVu.getText().toString().trim();
        for (int i = 0; i < CHUC_VU_DISPLAY.length; i++) {
            if (CHUC_VU_DISPLAY[i].equals(display)) return CHUC_VU_VALUE[i];
        }
        return "LeTan";
    }

    @Override
    public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
