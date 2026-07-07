package com.example.ql_homestay.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.ChiTietPhuThu;
import com.example.ql_homestay.model.HoaDon;
import com.example.ql_homestay.repository.InvoiceRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * G2 – Chi tiết hóa đơn.
 * Hiển thị 4 card: thông tin HĐ → đặt phòng → chi tiết tiền → thanh toán.
 * Phân quyền:
 *   - KeToan / Admin: thấy nút "Xác nhận đã TT" và "In hóa đơn"
 *   - LeTan: chỉ thấy "In hóa đơn"
 */
public class InvoiceDetailFragment extends Fragment {

    private int maHD = -1;
    private HoaDon hoaDon;
    private InvoiceRepository invoiceRepo;
    private SessionManager session;
    private DatabaseHelper dbHelper;

    // Views
    private TextView tvMaHD, tvTrangThai, tvNgayLap;
    private TextView tvTenPhong, tvTenKhach, tvCheckIn, tvCheckOut, tvSoDem;
    private TextView tvTienPhong, tvGiamGia, tvTongCong;
    private TextView tvPhuongThuc, tvNgayTT, tvNguoiThu;
    private LinearLayout llPhuThuContainer;
    private LinearLayout llGiamGiaRow;
    private LinearLayout btnPrint, btnConfirmPayment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invoice_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            maHD = getArguments().getInt("maHD", -1);
        }

        dbHelper    = DatabaseHelper.getInstance(requireContext());
        invoiceRepo = new InvoiceRepository(dbHelper);
        session     = SessionManager.getInstance(requireContext());

        bindViews(view);
        setupBackButton(view);
        loadData();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void setupBackButton(View view) {
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack());
        }
    }

    private void bindViews(View view) {
        tvMaHD              = view.findViewById(R.id.tv_ma_hd);
        tvTrangThai         = view.findViewById(R.id.tv_trang_thai);
        tvNgayLap           = view.findViewById(R.id.tv_ngay_lap);
        tvTenPhong          = view.findViewById(R.id.tv_ten_phong);
        tvTenKhach          = view.findViewById(R.id.tv_ten_khach);
        tvCheckIn           = view.findViewById(R.id.tv_check_in);
        tvCheckOut          = view.findViewById(R.id.tv_check_out);
        tvSoDem             = view.findViewById(R.id.tv_so_dem);
        tvTienPhong         = view.findViewById(R.id.tv_tien_phong);
        llPhuThuContainer   = view.findViewById(R.id.ll_phu_thu_container);
        llGiamGiaRow        = view.findViewById(R.id.ll_giam_gia_row);
        tvGiamGia           = view.findViewById(R.id.tv_giam_gia);
        tvTongCong          = view.findViewById(R.id.tv_tong_cong);
        tvPhuongThuc        = view.findViewById(R.id.tv_phuong_thuc);
        tvNgayTT            = view.findViewById(R.id.tv_ngay_tt);
        tvNguoiThu          = view.findViewById(R.id.tv_nguoi_thu);
        btnPrint            = view.findViewById(R.id.btn_print);
        btnConfirmPayment   = view.findViewById(R.id.btn_confirm_payment);
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    private void loadData() {
        if (maHD < 0) return;

        new Thread(() -> {
            hoaDon = invoiceRepo.getInvoiceById(maHD);
            List<ChiTietPhuThu> chiTietList = invoiceRepo.getChiTietByHoaDon(maHD);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (hoaDon != null) {
                        bindInvoiceData(hoaDon, chiTietList);
                        applyPermissions();
                    }
                });
            }
        }).start();
    }

    private void bindInvoiceData(HoaDon hd, List<ChiTietPhuThu> chiTietList) {
        // Card thông tin
        tvMaHD.setText("Hóa đơn #" + String.format(Locale.getDefault(), "%03d", hd.getMaHD()));
        tvNgayLap.setText(formatDate(hd.getNgayLap()));

        // Badge trạng thái
        tvTrangThai.setText(hd.getTrangThaiLabel());
        applyBadgeStyle(tvTrangThai, hd.getTrangThai());

        // Card đặt phòng
        tvTenPhong.setText(hd.getTenPhong() != null ? hd.getTenPhong() : "—");
        tvTenKhach.setText(hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "—");
        tvCheckIn.setText(formatDate(hd.getNgayCheckIn()));
        tvCheckOut.setText(formatDate(hd.getNgayCheckOut()));
        tvSoDem.setText(hd.getSoDem() + " đêm");

        // Card chi tiết tiền
        tvTienPhong.setText(formatMoney(hd.getTienPhong()));

        // Phụ thu dòng động
        llPhuThuContainer.removeAllViews();
        for (ChiTietPhuThu ct : chiTietList) {
            addPhuThuRow(ct.getTenPhuThu(), ct.getSoTien());
        }

        // Giảm giá
        if (hd.getGiamGia() > 0) {
            tvGiamGia.setText("-" + formatMoney(hd.getGiamGia()));
            llGiamGiaRow.setVisibility(View.VISIBLE);
        } else {
            llGiamGiaRow.setVisibility(View.GONE);
        }
        tvTongCong.setText(formatMoney(hd.getTongCong()));

        // Card thanh toán
        tvPhuongThuc.setText(hd.getPhuongThucTT() != null ? hd.getPhuongThucTT() : "—");
        tvNgayTT.setText(hd.getNgayTT() != null ? formatDate(hd.getNgayTT()) : "—");
        tvNguoiThu.setText(hd.getTenNhanVien() != null ? hd.getTenNhanVien() : "—");

        // Nút confirm: ẩn nếu đã thanh toán
        if ("DaThanhToan".equals(hd.getTrangThai()) || "HoanTien".equals(hd.getTrangThai())) {
            btnConfirmPayment.setVisibility(View.GONE);
        }
    }

    private void addPhuThuRow(String ten, double soTien) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(36)));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvLabel = new TextView(requireContext());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvLabel.setLayoutParams(labelParams);
        tvLabel.setTextSize(13f);
        tvLabel.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tvLabel.setText(ten + ":");

        TextView tvValue = new TextView(requireContext());
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvValue.setTextSize(14f);
        tvValue.setTextColor(getResources().getColor(R.color.text_primary, null));
        tvValue.setText(formatMoney(soTien));

        row.addView(tvLabel);
        row.addView(tvValue);
        llPhuThuContainer.addView(row);
    }

    // ─── Permissions ─────────────────────────────────────────────────────────

    private void applyPermissions() {
        String vaiTro = session.getVaiTro();
        boolean canConfirm = PermissionHelper.hasFullAccess(dbHelper, vaiTro,
                PermissionHelper.MODULE_HOA_DON);

        // Nút xác nhận: chỉ KeToan / Admin
        if (canConfirm && hoaDon != null && "ChuaThanhToan".equals(hoaDon.getTrangThai())) {
            btnConfirmPayment.setVisibility(View.VISIBLE);
            btnConfirmPayment.setOnClickListener(v -> confirmPayment());
        } else {
            btnConfirmPayment.setVisibility(View.GONE);
        }

        // Nút In hóa đơn: mọi role có thể truy cập module
        btnPrint.setOnClickListener(v -> printInvoice());
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void confirmPayment() {
        if (hoaDon == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String phuongThuc = hoaDon.getPhuongThucTT() != null ? hoaDon.getPhuongThucTT() : "TM";

        new Thread(() -> {
            int rows = invoiceRepo.confirmPayment(hoaDon.getMaHD(), today, phuongThuc);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(requireContext(),
                                "✅ Đã xác nhận thanh toán hóa đơn #" +
                                String.format(Locale.getDefault(), "%03d", hoaDon.getMaHD()),
                                Toast.LENGTH_LONG).show();
                        loadData(); // refresh
                    } else {
                        Toast.makeText(requireContext(),
                                "❌ Không thể xác nhận. Vui lòng thử lại.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void printInvoice() {
        if (hoaDon == null) return;

        // Build nội dung text để share
        StringBuilder sb = new StringBuilder();
        sb.append("=== HÓA ĐƠN LALA HOUSE ===\n");
        sb.append("Mã HĐ: #").append(String.format(Locale.getDefault(), "%03d", hoaDon.getMaHD())).append("\n");
        sb.append("Ngày lập: ").append(formatDate(hoaDon.getNgayLap())).append("\n");
        sb.append("Khách hàng: ").append(hoaDon.getTenKhachHang()).append("\n");
        sb.append("Phòng: ").append(hoaDon.getTenPhong()).append("\n");
        sb.append("Check-in: ").append(formatDate(hoaDon.getNgayCheckIn())).append("\n");
        sb.append("Check-out: ").append(formatDate(hoaDon.getNgayCheckOut())).append("\n");
        sb.append("Số đêm: ").append(hoaDon.getSoDem()).append("\n");
        sb.append("Tiền phòng: ").append(formatMoney(hoaDon.getTienPhong())).append("\n");
        if (hoaDon.getPhuThuDichVu() > 0)
            sb.append("Phụ thu: ").append(formatMoney(hoaDon.getPhuThuDichVu())).append("\n");
        if (hoaDon.getGiamGia() > 0)
            sb.append("Giảm giá: -").append(formatMoney(hoaDon.getGiamGia())).append("\n");
        sb.append("TỔNG CỘNG: ").append(formatMoney(hoaDon.getTongCong())).append("\n");
        sb.append("Trạng thái: ").append(hoaDon.getTrangThaiLabel()).append("\n");
        sb.append("=========================");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ hóa đơn"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void applyBadgeStyle(TextView tv, String trangThai) {
        if (trangThai == null) return;
        switch (trangThai) {
            case "DaThanhToan":
                tv.setBackgroundResource(R.drawable.bg_badge_dathanhtoan);
                tv.setTextColor(getResources().getColor(R.color.badge_dathanhtoan_text, null));
                break;
            case "ChuaThanhToan":
                tv.setBackgroundResource(R.drawable.bg_badge_chuathanhtoan);
                tv.setTextColor(getResources().getColor(R.color.badge_chuathanhtoan_text, null));
                break;
            case "HoanTien":
                tv.setBackgroundResource(R.drawable.bg_badge_dahuy);
                tv.setTextColor(getResources().getColor(R.color.badge_dahuy_text, null));
                break;
        }
    }

    private String formatDate(String date) {
        if (date == null || date.length() < 10) return "—";
        try {
            String[] parts = date.substring(0, 10).split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return date;
        }
    }

    private String formatMoney(double amount) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format((long) amount) + " đ";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
