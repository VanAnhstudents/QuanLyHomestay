package com.example.ql_homestay.ui.staff;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.NhanVien;
import com.example.ql_homestay.model.PhanCongCa;
import com.example.ql_homestay.repository.StaffRepository;
import com.example.ql_homestay.util.AvatarHelper;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chi tiết nhân viên.
 * - Hiển thị thông tin + bảng ca làm việc (CheckBox read-only).
 * - Button "Phân công" → mở F4.
 * - Row Sửa/Xóa chỉ Admin thấy.
 */
public class StaffDetailFragment extends Fragment {

    private static final String ARG_MA_NV = "maNV";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    public static StaffDetailFragment newInstance(int maNV) {
        StaffDetailFragment f = new StaffDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_NV, maNV);
        f.setArguments(args);
        return f;
    }

    private int maNV = -1;
    private StaffRepository repository;
    private SessionManager session;
    private DatabaseHelper dbHelper;
    private String displayedWeekStart = StaffRepository.currentWeekStart();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views — thông tin
    private ImageView ivAvatar;
    private TextView tvInitials, tvTen, tvChucVu;
    private TextView tvSdt, tvEmail, tvCccd, tvDiaChi, tvNgayVaoLam;
    private MaterialButton btnPhanCong, btnChinhSua, btnXoa;
    private View rowAction;

    // CheckBox ca Sáng
    private CheckBox cbSangT2, cbSangT3, cbSangT4, cbSangT5, cbSangT6, cbSangT7, cbSangCN;
    // CheckBox ca Chiều
    private CheckBox cbChieuT2, cbChieuT3, cbChieuT4, cbChieuT5, cbChieuT6, cbChieuT7, cbChieuCN;
    // CheckBox ca Tối
    private CheckBox cbToiT2, cbToiT3, cbToiT4, cbToiT5, cbToiT6, cbToiT7, cbToiCN;

    // Mapping: maCa 1=Sáng, 2=Chiều, 3=Tối; thu 1=T2..7=CN
    // (khớp seed data DatabaseHelper)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maNV = getArguments().getInt(ARG_MA_NV, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        repository = new StaffRepository(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        applyPermission();
        loadData();
    }

    private void bindViews(View v) {
        ivAvatar = v.findViewById(R.id.iv_avatar_detail);
        tvInitials = v.findViewById(R.id.tv_initials_detail);
        tvTen = v.findViewById(R.id.tv_ten_nv_detail);
        tvChucVu = v.findViewById(R.id.tv_chuc_vu_detail);
        tvSdt = v.findViewById(R.id.tv_sdt_detail);
        tvEmail = v.findViewById(R.id.tv_email_detail);
        tvCccd = v.findViewById(R.id.tv_cccd_detail);
        tvDiaChi = v.findViewById(R.id.tv_dia_chi_detail);
        tvNgayVaoLam = v.findViewById(R.id.tv_ngay_vao_lam_detail);
        btnPhanCong = v.findViewById(R.id.btn_phan_cong);
        rowAction = v.findViewById(R.id.row_action_buttons);
        btnChinhSua = v.findViewById(R.id.btn_chinh_sua);
        btnXoa = v.findViewById(R.id.btn_xoa);

        // Ca Sáng
        cbSangT2 = v.findViewById(R.id.cb_sang_t2); cbSangT3 = v.findViewById(R.id.cb_sang_t3);
        cbSangT4 = v.findViewById(R.id.cb_sang_t4); cbSangT5 = v.findViewById(R.id.cb_sang_t5);
        cbSangT6 = v.findViewById(R.id.cb_sang_t6); cbSangT7 = v.findViewById(R.id.cb_sang_t7);
        cbSangCN = v.findViewById(R.id.cb_sang_cn);
        // Ca Chiều
        cbChieuT2 = v.findViewById(R.id.cb_chieu_t2); cbChieuT3 = v.findViewById(R.id.cb_chieu_t3);
        cbChieuT4 = v.findViewById(R.id.cb_chieu_t4); cbChieuT5 = v.findViewById(R.id.cb_chieu_t5);
        cbChieuT6 = v.findViewById(R.id.cb_chieu_t6); cbChieuT7 = v.findViewById(R.id.cb_chieu_t7);
        cbChieuCN = v.findViewById(R.id.cb_chieu_cn);
        // Ca Tối
        cbToiT2 = v.findViewById(R.id.cb_toi_t2); cbToiT3 = v.findViewById(R.id.cb_toi_t3);
        cbToiT4 = v.findViewById(R.id.cb_toi_t4); cbToiT5 = v.findViewById(R.id.cb_toi_t5);
        cbToiT6 = v.findViewById(R.id.cb_toi_t6); cbToiT7 = v.findViewById(R.id.cb_toi_t7);
        cbToiCN = v.findViewById(R.id.cb_toi_cn);
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc != null) {
            TextView tv = bc.findViewById(R.id.tv_breadcrumb);
            if (tv != null) tv.setText("Trang chủ → Nhân viên → Chi tiết");
        }
    }

    private void applyPermission() {
        boolean isAdmin = PermissionHelper.hasFullAccess(
                dbHelper, session.getVaiTro(), PermissionHelper.MODULE_NHAN_VIEN);
        rowAction.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnPhanCong.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        executor.execute(() -> {
            NhanVien nv = repository.getStaffById(maNV);
            List<PhanCongCa> shifts = repository.getShiftAssignments(maNV);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (nv != null) bindStaff(nv);
                bindShifts(shifts);
            });
        });
    }

    private void bindStaff(NhanVien nv) {
        AvatarHelper.loadAvatar(requireContext(), nv.getAvatar(), nv.getHoTen(), ivAvatar, tvInitials);
        tvTen.setText(nv.getHoTen());
        tvChucVu.setText(mapChucVu(nv.getChucVu()));
        tvSdt.setText(orDash(nv.getSdt()));
        tvEmail.setText(orDash(nv.getEmail()));
        tvCccd.setText(orDash(nv.getCccd()));
        tvDiaChi.setText(orDash(nv.getDiaChi()));
        tvNgayVaoLam.setText(orDash(nv.getNgayVaoLam()));

        if (rowAction.getVisibility() == View.VISIBLE) {
            btnChinhSua.setOnClickListener(v -> openEdit(nv.getMaNV()));
            btnXoa.setOnClickListener(v -> confirmDelete(nv));
        }
        btnPhanCong.setOnClickListener(v -> openShiftAssignment(nv.getMaNV()));
    }

    /** Đánh dấu CheckBox dựa trên danh sách PhanCongCa.
     *  maCa: 1=Sáng, 2=Chiều, 3=Tối | thuTrongTuan: 1=T2 … 7=CN */
    private void bindShifts(List<PhanCongCa> list) {
        clearAllCheckboxes();
        if (list == null) return;
        displayedWeekStart = StaffRepository.currentWeekStart();
        for (PhanCongCa pc : list) {
            CheckBox cb = getCheckBox(pc.getMaCa(), pc.getThuTrongTuan());
            if (cb != null) cb.setChecked(true);
        }
    }

    private void clearAllCheckboxes() {
        CheckBox[] all = {
                cbSangT2, cbSangT3, cbSangT4, cbSangT5, cbSangT6, cbSangT7, cbSangCN,
                cbChieuT2, cbChieuT3, cbChieuT4, cbChieuT5, cbChieuT6, cbChieuT7, cbChieuCN,
                cbToiT2, cbToiT3, cbToiT4, cbToiT5, cbToiT6, cbToiT7, cbToiCN
        };
        for (CheckBox cb : all) if (cb != null) cb.setChecked(false);
    }

    @Nullable
    private CheckBox getCheckBox(int maCa, int thu) {
        switch (maCa) {
            case 1: // Sáng
                switch (thu) {
                    case 1: return cbSangT2; case 2: return cbSangT3; case 3: return cbSangT4;
                    case 4: return cbSangT5; case 5: return cbSangT6; case 6: return cbSangT7;
                    case 7: return cbSangCN;
                }
                break;
            case 2: // Chiều
                switch (thu) {
                    case 1: return cbChieuT2; case 2: return cbChieuT3; case 3: return cbChieuT4;
                    case 4: return cbChieuT5; case 5: return cbChieuT6; case 6: return cbChieuT7;
                    case 7: return cbChieuCN;
                }
                break;
            case 3: // Tối
                switch (thu) {
                    case 1: return cbToiT2; case 2: return cbToiT3; case 3: return cbToiT4;
                    case 4: return cbToiT5; case 5: return cbToiT6; case 6: return cbToiT7;
                    case 7: return cbToiCN;
                }
                break;
        }
        return null;
    }

    private void openEdit(int maNV) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, StaffAddEditFragment.newInstance(maNV))
                .addToBackStack(null)
                .commit();
    }

    private void openShiftAssignment(int maNV) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, ShiftAssignmentFragment.newInstance(maNV, displayedWeekStart))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) loadData();
    }

    private void confirmDelete(NhanVien nv) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((TextView) dialogView.findViewById(R.id.tv_dialog_title)).setText("Xóa nhân viên");
        ((TextView) dialogView.findViewById(R.id.tv_dialog_message))
                .setText("Xóa nhân viên \"" + nv.getHoTen() + "\"? Hành động không thể hoàn tác.");

        Dialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView).create();

        dialogView.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_dialog_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            executor.execute(() -> {
                int rows = repository.deleteStaff(nv.getMaNV());
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    if (rows > 0) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(requireContext(),
                                "Không thể xóa nhân viên này.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        dialog.show();
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    private static String mapChucVu(String cv) {
        if (cv == null) return "";
        switch (cv) {
            case "QuanLy": return "Quản lý";
            case "LeTan":  return "Lễ tân";
            case "KeToan": return "Kế toán";
            case "DonPhong": return "Dọn phòng";
            case "BaoVe":  return "Bảo vệ";
            default: return cv;
        }
    }

    @Override
    public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
