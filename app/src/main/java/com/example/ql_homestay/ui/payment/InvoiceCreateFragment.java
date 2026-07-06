package com.example.ql_homestay.ui.payment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.DatPhongDAO;
import com.example.ql_homestay.model.ChiTietPhuThu;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.model.HoaDon;
import com.example.ql_homestay.repository.InvoiceRepository;
import com.example.ql_homestay.util.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * G3 – Lập hóa đơn mới.
 * AutoComplete tìm DatPhong (chỉ hiện đặt phòng DangO chưa có HĐ),
 * card tự điền thông tin phòng/khách,
 * card phụ thu động (+ Thêm dòng phí),
 * tổng cộng realtime, nút Tạo hóa đơn.
 */
public class InvoiceCreateFragment extends Fragment {

    private InvoiceRepository invoiceRepo;
    private DatPhongDAO datPhongDAO;
    private SessionManager session;

    // Views
    private AutoCompleteTextView acDatPhong;
    private LinearLayout cardAutoFill, cardFees, llPhuongThucWrapper, llGhiChuWrapper;
    private TextView tvAfPhong, tvAfKhach, tvAfCheckin, tvAfCheckout, tvAfTienPhong;
    private LinearLayout llFeesContainer;
    private LinearLayout btnAddFeeRow;
    private TextView tvTongCong;
    private Spinner spinnerPhuongThuc;
    private EditText etGhiChu;
    private LinearLayout btnCreateInvoice;

    // Dữ liệu
    private DatPhong selectedDatPhong;
    private final List<FeeRowData> feeRows = new ArrayList<>();
    private List<DatPhong> availableDatPhong = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invoice_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        invoiceRepo  = new InvoiceRepository(dbHelper);
        datPhongDAO  = new DatPhongDAO(dbHelper);
        session      = SessionManager.getInstance(requireContext());

        bindViews(view);
        setupBackButton(view);
        setupPhuongThucSpinner();
        loadAvailableBookings();
        setupCreateButton();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        acDatPhong          = view.findViewById(R.id.ac_dat_phong);
        cardAutoFill        = view.findViewById(R.id.card_auto_fill);
        cardFees            = view.findViewById(R.id.card_fees);
        llPhuongThucWrapper = view.findViewById(R.id.ll_phuong_thuc_wrapper);
        llGhiChuWrapper     = view.findViewById(R.id.ll_ghi_chu_wrapper);
        tvAfPhong           = view.findViewById(R.id.tv_af_phong);
        tvAfKhach           = view.findViewById(R.id.tv_af_khach);
        tvAfCheckin         = view.findViewById(R.id.tv_af_checkin);
        tvAfCheckout        = view.findViewById(R.id.tv_af_checkout);
        tvAfTienPhong       = view.findViewById(R.id.tv_af_tien_phong);
        llFeesContainer     = view.findViewById(R.id.ll_fees_container);
        btnAddFeeRow        = view.findViewById(R.id.btn_add_fee_row);
        tvTongCong          = view.findViewById(R.id.tv_tong_cong);
        spinnerPhuongThuc   = view.findViewById(R.id.spinner_phuong_thuc);
        etGhiChu            = view.findViewById(R.id.et_ghi_chu);
        btnCreateInvoice    = view.findViewById(R.id.btn_create_invoice);

        btnAddFeeRow.setOnClickListener(v -> addFeeRow("", 0));
    }

    private void setupBackButton(View view) {
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupPhuongThucSpinner() {
        String[] methods = {"TM", "CK", "VNPAY", "Thẻ ngân hàng"};
        ArrayAdapter<String> adp = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, methods);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhuongThuc.setAdapter(adp);
    }

    // ─── Load data ───────────────────────────────────────────────────────────

    private void loadAvailableBookings() {
        new Thread(() -> {
            // Lấy DatPhong DangO chưa có HĐ
            List<DatPhong> allDangO = datPhongDAO.filterByTrangThai("DangO");
            List<DatPhong> filtered = new ArrayList<>();
            for (DatPhong dp : allDangO) {
                HoaDon existing = invoiceRepo.getInvoiceByDatPhong(dp.getMaDatPhong());
                if (existing == null) {
                    filtered.add(dp);
                }
            }
            // Cũng thêm DaTraPhong chưa có HĐ
            List<DatPhong> allTraPhong = datPhongDAO.filterByTrangThai("DaTraPhong");
            for (DatPhong dp : allTraPhong) {
                HoaDon existing = invoiceRepo.getInvoiceByDatPhong(dp.getMaDatPhong());
                if (existing == null) {
                    filtered.add(dp);
                }
            }

            availableDatPhong = filtered;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> setupAutoComplete());
            }
        }).start();
    }

    private void setupAutoComplete() {
        List<String> displayList = new ArrayList<>();
        for (DatPhong dp : availableDatPhong) {
            String label = "#" + dp.getMaDatPhong() + " – " +
                    (dp.getTenKhachHang() != null ? dp.getTenKhachHang() : "KH?") +
                    " (" + (dp.getTenPhong() != null ? dp.getTenPhong() : "P?") + ")";
            displayList.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, displayList);
        acDatPhong.setAdapter(adapter);

        acDatPhong.setOnItemClickListener((parent, v, position, id) -> {
            if (position < availableDatPhong.size()) {
                selectedDatPhong = availableDatPhong.get(position);
                fillAutoFillCard(selectedDatPhong);
                showFormSections();
            }
        });
    }

    private void fillAutoFillCard(DatPhong dp) {
        tvAfPhong.setText(dp.getTenPhong() != null ? dp.getTenPhong() : "—");
        tvAfKhach.setText(dp.getTenKhachHang() != null ? dp.getTenKhachHang() : "—");
        tvAfCheckin.setText(formatDate(dp.getNgayCheckIn()));
        tvAfCheckout.setText(formatDate(dp.getNgayCheckOut()));

        double tienPhong = dp.getGiaMoiDem() * dp.getSoDem();
        tvAfTienPhong.setText(formatMoney(tienPhong));
        updateTotal();
    }

    private void showFormSections() {
        cardAutoFill.setVisibility(View.VISIBLE);
        cardFees.setVisibility(View.VISIBLE);
        llPhuongThucWrapper.setVisibility(View.VISIBLE);
        llGhiChuWrapper.setVisibility(View.VISIBLE);
    }

    // ─── Fee rows ─────────────────────────────────────────────────────────────

    private void addFeeRow(String tenDefault, double soTienDefault) {
        FeeRowData rowData = new FeeRowData();
        feeRows.add(rowData);

        LinearLayout row = new LinearLayout(requireContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(44));
        rowParams.setMargins(0, 0, 0, dpToPx(6));
        row.setLayoutParams(rowParams);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        // EditText tên phụ thu
        EditText etTen = new EditText(requireContext());
        LinearLayout.LayoutParams tenParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 2f);
        etTen.setLayoutParams(tenParams);
        etTen.setHint("Tên phụ thu");
        etTen.setHintTextColor(getResources().getColor(R.color.text_placeholder, null));
        etTen.setTextSize(13f);
        etTen.setTextColor(getResources().getColor(R.color.text_primary, null));
        etTen.setBackground(getResources().getDrawable(R.drawable.bg_input_normal, null));
        etTen.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        etTen.setText(tenDefault);
        etTen.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                rowData.ten = s.toString().trim();
            }
        });

        // EditText số tiền
        EditText etSoTien = new EditText(requireContext());
        LinearLayout.LayoutParams soTienParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        soTienParams.setMargins(dpToPx(6), 0, 0, 0);
        etSoTien.setLayoutParams(soTienParams);
        etSoTien.setHint("Số tiền");
        etSoTien.setHintTextColor(getResources().getColor(R.color.text_placeholder, null));
        etSoTien.setTextSize(13f);
        etSoTien.setTextColor(getResources().getColor(R.color.text_primary, null));
        etSoTien.setBackground(getResources().getDrawable(R.drawable.bg_input_normal, null));
        etSoTien.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        etSoTien.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (soTienDefault > 0) etSoTien.setText(String.valueOf((long) soTienDefault));
        etSoTien.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                try { rowData.soTien = Double.parseDouble(s.toString()); }
                catch (NumberFormatException e) { rowData.soTien = 0; }
                updateTotal();
            }
        });

        // Nút xóa dòng
        TextView btnDelete = new TextView(requireContext());
        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(
                dpToPx(36), dpToPx(36));
        delParams.setMargins(dpToPx(4), 0, 0, 0);
        btnDelete.setLayoutParams(delParams);
        btnDelete.setGravity(Gravity.CENTER);
        btnDelete.setTextSize(16f);
        btnDelete.setText("✕");
        btnDelete.setTextColor(getResources().getColor(R.color.status_error, null));
        btnDelete.setClickable(true);
        btnDelete.setFocusable(true);

        row.addView(etTen);
        row.addView(etSoTien);
        row.addView(btnDelete);

        rowData.view = row;
        llFeesContainer.addView(row);

        btnDelete.setOnClickListener(v -> {
            llFeesContainer.removeView(row);
            feeRows.remove(rowData);
            updateTotal();
        });
    }

    private void updateTotal() {
        double tienPhong = 0;
        if (selectedDatPhong != null) {
            tienPhong = selectedDatPhong.getGiaMoiDem() * selectedDatPhong.getSoDem();
        }
        double phuThu = 0;
        for (FeeRowData rd : feeRows) phuThu += rd.soTien;
        double total = tienPhong + phuThu;
        tvTongCong.setText(formatMoney(total));
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    private void setupCreateButton() {
        btnCreateInvoice.setOnClickListener(v -> createInvoice());
    }

    private void createInvoice() {
        if (selectedDatPhong == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn đặt phòng!", Toast.LENGTH_SHORT).show();
            return;
        }

        double tienPhong = selectedDatPhong.getGiaMoiDem() * selectedDatPhong.getSoDem();
        double phuThu = 0;
        List<ChiTietPhuThu> chiTietList = new ArrayList<>();
        for (FeeRowData rd : feeRows) {
            if (!rd.ten.isEmpty() && rd.soTien > 0) {
                phuThu += rd.soTien;
                chiTietList.add(new ChiTietPhuThu(0, rd.ten, rd.soTien));
            }
        }
        double tongCong = tienPhong + phuThu;

        HoaDon hd = new HoaDon();
        hd.setMaDatPhong(selectedDatPhong.getMaDatPhong());
        hd.setNgayLap(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        hd.setTienPhong(tienPhong);
        hd.setPhuThuDichVu(phuThu);
        hd.setGiamGia(0);
        hd.setTongCong(tongCong);
        hd.setTrangThai("ChuaThanhToan");
        // Phương thức thanh toán
        Object selected = spinnerPhuongThuc.getSelectedItem();
        if (selected != null) hd.setPhuongThucTT(selected.toString());
        // Người thu = NhanVien đang đăng nhập (tra MaNV theo MaTK)
        hd.setMaNV(0); // để null nếu không tìm được

        new Thread(() -> {
            long maHD = invoiceRepo.createInvoice(hd, chiTietList);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (maHD > 0) {
                        Toast.makeText(requireContext(),
                                "✅ Tạo hóa đơn #" + String.format(Locale.getDefault(), "%03d", (int) maHD) + " thành công!",
                                Toast.LENGTH_LONG).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(requireContext(),
                                "❌ Không thể tạo hóa đơn. Đặt phòng này có thể đã có hóa đơn.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String formatDate(String date) {
        if (date == null || date.length() < 10) return "—";
        try {
            String[] parts = date.substring(0, 10).split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) { return date; }
    }

    private String formatMoney(double amount) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format((long) amount) + " đ";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // ─── Inner class ─────────────────────────────────────────────────────────

    private static class FeeRowData {
        String ten = "";
        double soTien = 0;
        View view;
    }
}
