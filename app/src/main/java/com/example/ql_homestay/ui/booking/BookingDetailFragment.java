package com.example.ql_homestay.ui.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.NhanVienDAO;
import com.example.ql_homestay.data.dao.KhachHangDAO;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.model.KhachHang;
import com.example.ql_homestay.model.NhanVien;
import com.example.ql_homestay.repository.BookingRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BookingDetailFragment – Chi tiết đặt phòng (C2).
 *
 * Logic Row 6A/6B theo bảng trạng thái (ux_ui.md C2):
 *   SapDen     → Sửa VISIBLE, Xóa VISIBLE, Check-in VISIBLE, Check-out GONE
 *   DangO      → Sửa GONE,    Xóa GONE,    Check-in GONE,    Check-out VISIBLE
 *   DaTraPhong → ẩn cả Row 6
 *   DaHuy      → ẩn cả Row 6
 *
 * Check-in: DatPhong→DangO, Phong→DangThue, log CheckInOut
 * Check-out: DatPhong→DaTraPhong, Phong→Trong, log CheckInOut, tăng SoLanThue
 *
 * RBAC: KeToan/NhanVien → ẩn Row 6, hiện tv_trang_thai_readonly.
 */
public class BookingDetailFragment extends Fragment {

    private static final String ARG_MA_DAT_PHONG = "ma_dat_phong";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private int maDatPhong;

    // Views – Card tổng quan
    private TextView tvMaDatPhong, tvTrangThai, tvTenPhong, tvNgayCiCo, tvSoDem;
    // Views – Card khách hàng
    private TextView tvAvatarInitials, tvTenKhach, tvSdtKhach;
    // Views – Card thanh toán
    private TextView tvTienPhong, tvSoLuongKhach, tvPhuongThucTT, tvTongCong;
    // Views – Ghi chú
    private TextView tvGhiChu;
    // Row actions
    private LinearLayout rowAction, row6a, row6b;
    private MaterialButton btnEditBooking, btnDeleteBooking, btnCheckin, btnCheckout;
    private TextView tvTrangThaiReadonly;

    private DatPhong currentDatPhong;
    private BookingRepository bookingRepository;
    private KhachHangDAO khachHangDAO;
    private NhanVienDAO nhanVienDAO;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static BookingDetailFragment newInstance(int maDatPhong) {
        BookingDetailFragment f = new BookingDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_DAT_PHONG, maDatPhong);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            maDatPhong = getArguments().getInt(ARG_MA_DAT_PHONG, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager    = SessionManager.getInstance(requireContext());
        dbHelper          = DatabaseHelper.getInstance(requireContext());
        bookingRepository = new BookingRepository(dbHelper);
        khachHangDAO      = new KhachHangDAO(dbHelper);
        nhanVienDAO       = new NhanVienDAO(dbHelper);

        bindViews(view);
        setupBreadcrumb(view);

        if (maDatPhong > 0) loadDetail();
    }

    private void bindViews(View view) {
        tvMaDatPhong       = view.findViewById(R.id.tv_ma_dat_phong);
        tvTrangThai        = view.findViewById(R.id.tv_trang_thai);
        tvTenPhong         = view.findViewById(R.id.tv_ten_phong);
        tvNgayCiCo         = view.findViewById(R.id.tv_ngay_ci_co);
        tvSoDem            = view.findViewById(R.id.tv_so_dem);
        tvAvatarInitials   = view.findViewById(R.id.tv_avatar_initials);
        tvTenKhach         = view.findViewById(R.id.tv_ten_khach);
        tvSdtKhach         = view.findViewById(R.id.tv_sdt_khach);
        tvTienPhong        = view.findViewById(R.id.tv_tien_phong);
        tvSoLuongKhach     = view.findViewById(R.id.tv_so_luong_khach);
        tvPhuongThucTT     = view.findViewById(R.id.tv_phuong_thuc_tt);
        tvTongCong         = view.findViewById(R.id.tv_tong_cong);
        tvGhiChu           = view.findViewById(R.id.tv_ghi_chu);
        rowAction          = view.findViewById(R.id.row_action);
        row6a              = view.findViewById(R.id.row_6a);
        row6b              = view.findViewById(R.id.row_6b);
        btnEditBooking     = view.findViewById(R.id.btn_edit_booking);
        btnDeleteBooking   = view.findViewById(R.id.btn_delete_booking);
        btnCheckin         = view.findViewById(R.id.btn_checkin);
        btnCheckout        = view.findViewById(R.id.btn_checkout);
        tvTrangThaiReadonly = view.findViewById(R.id.tv_trang_thai_readonly);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Đặt phòng → Chi tiết");
    }

    private void loadDetail() {
        dbExecutor.execute(() -> {
            DatPhong dp = bookingRepository.findDatPhongById(maDatPhong);
            KhachHang kh = dp != null ? khachHangDAO.findById(dp.getMaKH()) : null;
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (dp != null) {
                    currentDatPhong = dp;
                    bindData(dp, kh);
                    applyPermissionAndState(dp);
                }
            });
        });
    }

    private void bindData(DatPhong dp, KhachHang kh) {
        // Card tổng quan
        tvMaDatPhong.setText("Đặt phòng #" + dp.getMaDatPhong());
        tvTrangThai.setText(getTrangThaiLabel(dp.getTrangThai()));
        applyBadgeStyle(tvTrangThai, dp.getTrangThai());
        tvTenPhong.setText(dp.getTenPhong() != null ? dp.getTenPhong() : "");
        tvNgayCiCo.setText(dp.getNgayCheckIn() + " → " + dp.getNgayCheckOut());
        tvSoDem.setText(dp.getSoDem() + " đêm");

        // Card khách hàng
        if (kh != null) {
            tvTenKhach.setText(kh.getHoTen());
            tvSdtKhach.setText(kh.getSdt() != null ? kh.getSdt() : "");
            String initials = kh.getHoTen() != null && kh.getHoTen().length() >= 2
                    ? kh.getHoTen().substring(0, 2).toUpperCase() : "KH";
            tvAvatarInitials.setText(initials);
        } else if (dp.getTenKhachHang() != null) {
            tvTenKhach.setText(dp.getTenKhachHang());
            String initials = dp.getTenKhachHang().length() >= 2
                    ? dp.getTenKhachHang().substring(0, 2).toUpperCase() : "KH";
            tvAvatarInitials.setText(initials);
        }

        // Card thanh toán
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

        // Tính tiền: GiaMoiDem * SoDem (GiaMoiDem được JOIN từ Phong)
        double giaMoiDem = dp.getGiaMoiDem();
        int soDem = dp.getSoDem();

        // Nếu GiaMoiDem = 0 (dữ liệu cũ chưa có JOIN), thử lấy từ SoDem đã lưu
        double tienPhong = giaMoiDem > 0 ? giaMoiDem * soDem : 0;

        tvTienPhong.setText(giaMoiDem > 0
                ? nf.format((long) giaMoiDem) + " đ × " + soDem + " đêm"
                : "—");
        tvSoLuongKhach.setText(dp.getSoLuongKhach() + " người");

        // Hiển thị tên đầy đủ phương thức thanh toán
        tvPhuongThucTT.setText(getPhuongThucLabel(dp.getPhuongThucThanhToan()));

        tvTongCong.setText(tienPhong > 0
                ? nf.format((long) tienPhong) + " đ"
                : "—");

        // Ghi chú
        tvGhiChu.setText(dp.getGhiChu() != null && !dp.getGhiChu().isEmpty()
                ? dp.getGhiChu() : "(Không có ghi chú)");
    }

    /** Chuyển mã ngắn thành tên đầy đủ phương thức thanh toán */
    private String getPhuongThucLabel(String code) {
        if (code == null) return "—";
        switch (code) {
            case "TM":    return "Tiền mặt";
            case "CK":    return "Chuyển khoản";
            case "VNPAY": return "VNPAY";
            default:      return code;
        }
    }

    /**
     * Áp dụng RBAC và cập nhật trạng thái hiển thị Row 6A/6B.
     * KeToan / NhanVien → ẩn row_action, hiện tvTrangThaiReadonly.
     * Admin / LeTan → hiển thị theo bảng trạng thái.
     */
    private void applyPermissionAndState(DatPhong dp) {
        String vaiTro = sessionManager.getVaiTro();
        boolean canManage = PermissionHelper.hasAccess(dbHelper, vaiTro,
                PermissionHelper.MODULE_QUAN_LY_DAT_PHONG, PermissionHelper.QUYEN_TOAN_QUYEN);

        if (!canManage) {
            // Read-only: ẩn toàn bộ row hành động
            if (row6a != null) row6a.setVisibility(View.GONE);
            if (row6b != null) row6b.setVisibility(View.GONE);
            if (tvTrangThaiReadonly != null) {
                tvTrangThaiReadonly.setVisibility(View.VISIBLE);
                tvTrangThaiReadonly.setText("Trạng thái: " + getTrangThaiLabel(dp.getTrangThai()));
            }
            return;
        }

        // Admin / LeTan: hiển thị theo bảng logic trạng thái
        updateRowVisibility(dp.getTrangThai());
        setupActionButtons(dp);
    }

    /**
     * Cập nhật visibility Row 6A/6B theo bảng:
     *   SapDen    → Sửa✓ Xóa✓ CI✓ CO✗
     *   DangO     → Sửa✗ Xóa✗ CI✗ CO✓
     *   DaTraPhong/DaHuy → ẩn cả row_action
     */
    private void updateRowVisibility(String trangThai) {
        if (trangThai == null) return;
        switch (trangThai) {
            case "SapDen":
                setVisible(row6a, true);
                setVisible(row6b, true);
                setVisible(btnEditBooking,   true);
                setVisible(btnDeleteBooking, true);
                setVisible(btnCheckin,       true);
                setVisible(btnCheckout,      false);
                break;
            case "DangO":
                setVisible(row6a, false);
                setVisible(row6b, true);
                setVisible(btnEditBooking,   false);
                setVisible(btnDeleteBooking, false);
                setVisible(btnCheckin,       false);
                setVisible(btnCheckout,      true);
                break;
            case "DaTraPhong":
            case "DaHuy":
            default:
                setVisible(row6a, false);
                setVisible(row6b, false);
                if (rowAction != null) rowAction.setVisibility(View.GONE);
                break;
        }
    }

    private void setVisible(View v, boolean visible) {
        if (v != null) v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setupActionButtons(DatPhong dp) {
        if (btnEditBooking != null) {
            btnEditBooking.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(FRAGMENT_CONTAINER_ID,
                                    BookingAddEditFragment.newInstance(dp.getMaDatPhong(), -1))
                            .addToBackStack(null)
                            .commit());
        }

        if (btnDeleteBooking != null) {
            btnDeleteBooking.setOnClickListener(v -> showDeleteConfirm(dp));
        }

        if (btnCheckin != null) {
            btnCheckin.setOnClickListener(v -> performCheckIn(dp));
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> performCheckOut(dp));
        }
    }

    // -------------------------------------------------------------------------
    // CHECK-IN
    // -------------------------------------------------------------------------
    private void performCheckIn(DatPhong dp) {
        int maNV = getMaNVHienTai();
        dbExecutor.execute(() -> {
            boolean ok = bookingRepository.doCheckIn(dp.getMaDatPhong(), dp.getMaPhong(), maNV);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (ok) {
                    Toast.makeText(requireContext(),
                            "✅ Check-in thành công! Phòng " + dp.getTenPhong() + " đang được sử dụng.",
                            Toast.LENGTH_LONG).show();
                    // Refresh detail
                    dp.setTrangThai("DangO");
                    currentDatPhong = dp;
                    tvTrangThai.setText(getTrangThaiLabel("DangO"));
                    applyBadgeStyle(tvTrangThai, "DangO");
                    updateRowVisibility("DangO");
                } else {
                    Toast.makeText(requireContext(),
                            "❌ Không thể thực hiện. Vui lòng thử lại.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // CHECK-OUT
    // -------------------------------------------------------------------------
    private void performCheckOut(DatPhong dp) {
        int maNV = getMaNVHienTai();
        dbExecutor.execute(() -> {
            boolean ok = bookingRepository.doCheckOut(
                    dp.getMaDatPhong(), dp.getMaPhong(), dp.getMaKH(), maNV);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (ok) {
                    Toast.makeText(requireContext(),
                            "✅ Check-out thành công! Phòng " + dp.getTenPhong() + " đã được trả.",
                            Toast.LENGTH_LONG).show();
                    dp.setTrangThai("DaTraPhong");
                    currentDatPhong = dp;
                    tvTrangThai.setText(getTrangThaiLabel("DaTraPhong"));
                    applyBadgeStyle(tvTrangThai, "DaTraPhong");
                    updateRowVisibility("DaTraPhong");
                    if (rowAction != null) rowAction.setVisibility(View.GONE);
                } else {
                    Toast.makeText(requireContext(),
                            "❌ Không thể thực hiện. Vui lòng thử lại.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------
    private void showDeleteConfirm(DatPhong dp) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa đặt phòng")
                .setMessage("Bạn có chắc chắn muốn xóa đặt phòng #" + dp.getMaDatPhong() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBooking(dp))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBooking(DatPhong dp) {
        dbExecutor.execute(() -> {
            int rows = bookingRepository.deleteDatPhong(dp.getMaDatPhong());
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (rows > 0) {
                    Snackbar.make(requireView(), "Đã xóa đặt phòng #" + dp.getMaDatPhong(),
                            Snackbar.LENGTH_LONG).show();
                    if (getParentFragmentManager().getBackStackEntryCount() > 0)
                        getParentFragmentManager().popBackStack();
                } else {
                    Snackbar.make(requireView(), "Xóa thất bại.", Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /** Lấy MaNV của nhân viên đang đăng nhập từ TaiKhoan → NhanVien. */
    private int getMaNVHienTai() {
        int maTK = sessionManager.getMaTK();
        if (maTK <= 0) return 0;
        NhanVien nv = nhanVienDAO.findByMaTK(maTK);
        return nv != null ? nv.getMaNV() : 0;
    }

    private String getTrangThaiLabel(String tt) {
        if (tt == null) return "";
        switch (tt) {
            case "SapDen":      return "Sắp đến";
            case "DangO":       return "Đang ở";
            case "DaTraPhong":  return "Đã trả phòng";
            case "DaHuy":       return "Đã hủy";
            default:            return tt;
        }
    }

    private void applyBadgeStyle(TextView tv, String trangThai) {
        if (tv == null || trangThai == null) return;
        switch (trangThai) {
            case "SapDen":
                tv.setBackgroundResource(R.drawable.bg_badge_dadat);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_dadat_text, null));
                break;
            case "DangO":
                tv.setBackgroundResource(R.drawable.bg_badge_dangthue);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_dangthue_text, null));
                break;
            case "DaTraPhong":
                tv.setBackgroundResource(R.drawable.bg_badge_dathanhtoan);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_dathanhtoan_text, null));
                break;
            case "DaHuy":
                tv.setBackgroundResource(R.drawable.bg_badge_dahuy);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_dahuy_text, null));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
