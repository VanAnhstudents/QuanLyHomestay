package com.example.ql_homestay.ui.booking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.MainActivity;
import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.KhachHangDAO;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.model.KhachHang;
import com.example.ql_homestay.model.Phong;
import com.example.ql_homestay.repository.BookingRepository;
import com.example.ql_homestay.repository.RoomRepository;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * BookingAddEditFragment – Thêm/Sửa đặt phòng (C3).
 * - Tìm khách có sẵn qua AutoComplete (tên / SĐT)
 * - Nếu không tìm thấy: nhập Tên, SĐT, CCCD để tự động tạo khách mới
 * - Spinner phòng chỉ liệt kê phòng Trống
 * - DatePicker + TimePicker cho check-in / check-out
 * - Tính tiền realtime
 */
public class BookingAddEditFragment extends Fragment {

    private static final String ARG_MA_DAT_PHONG     = "ma_dat_phong";
    private static final String ARG_MA_PHONG_PRESELECT = "ma_phong_pre";
    private static final int    FRAGMENT_CONTAINER_ID  = R.id.fragment_container;

    private int maDatPhong      = -1;
    private int maPhongPreselect = -1;

    // ---- Views: Khách hàng ----
    private AutoCompleteTextView actvKhachHang;
    private LinearLayout         llNewCustomerSection;
    private EditText             etTenKhach, etSdtKhach, etCccdKhach;

    // ---- Views: Đặt phòng ----
    private Spinner  spinnerPhong;
    private TextView tvNgayCheckin,  tvGioCheckin;
    private TextView tvNgayCheckout, tvGioCheckout;
    private EditText etSoLuongKhach, etGhiChu;
    private Spinner  spinnerPhuongThucTT;

    // ---- Views: Preview chi phí ----
    private TextView tvSoDemPreview, tvDonGiaPreview, tvThanhTienPreview;

    // ---- Views: Nút ----
    private Button btnSaveBooking;

    // ---- Data ----
    private List<KhachHang> khachHangList  = new ArrayList<>();
    private List<Phong>     phongList      = new ArrayList<>();
    private KhachHang       selectedKhach  = null;
    private String ngayCheckin  = null;
    private String gioCheckin   = "14:00";
    private String ngayCheckout = null;
    private String gioCheckout  = "12:00";

    // ---- DAOs / Repos ----
    private BookingRepository bookingRepository;
    private RoomRepository    roomRepository;
    private KhachHangDAO      khachHangDAO;
    private DatabaseHelper    dbHelper;
    private SessionManager    sessionManager;

    private final ExecutorService dbExecutor  = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // =========================================================================
    // Factory
    // =========================================================================

    public static BookingAddEditFragment newInstance(int maDatPhong, int maPhongPreselect) {
        BookingAddEditFragment f = new BookingAddEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_DAT_PHONG,      maDatPhong);
        args.putInt(ARG_MA_PHONG_PRESELECT, maPhongPreselect);
        f.setArguments(args);
        return f;
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maDatPhong       = getArguments().getInt(ARG_MA_DAT_PHONG,      -1);
            maPhongPreselect = getArguments().getInt(ARG_MA_PHONG_PRESELECT, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager    = SessionManager.getInstance(requireContext());
        dbHelper          = DatabaseHelper.getInstance(requireContext());
        bookingRepository = new BookingRepository(dbHelper);
        roomRepository    = new RoomRepository(dbHelper);
        khachHangDAO      = new KhachHangDAO(dbHelper);

        bindViews(view);
        updateAppBarTitle();
        setupCancelButton(view);
        setupKhachHangAutoComplete();
        setupDateTimePickers();
        setupPhuongThucTTSpinner();
        setupSaveButton();

        loadFormData();
    }

    // =========================================================================
    // Bind & AppBar
    // =========================================================================

    private void bindViews(View view) {
        actvKhachHang        = view.findViewById(R.id.actv_khach_hang);
        llNewCustomerSection = view.findViewById(R.id.ll_new_customer_section);
        etTenKhach           = view.findViewById(R.id.et_ten_khach);
        etSdtKhach           = view.findViewById(R.id.et_sdt_khach);
        etCccdKhach          = view.findViewById(R.id.et_cccd_khach);

        spinnerPhong         = view.findViewById(R.id.spinner_phong);
        tvNgayCheckin        = view.findViewById(R.id.tv_ngay_checkin);
        tvGioCheckin         = view.findViewById(R.id.tv_gio_checkin);
        tvNgayCheckout       = view.findViewById(R.id.tv_ngay_checkout);
        tvGioCheckout        = view.findViewById(R.id.tv_gio_checkout);
        etSoLuongKhach       = view.findViewById(R.id.et_so_luong_khach);
        spinnerPhuongThucTT  = view.findViewById(R.id.spinner_phuong_thuc_tt);
        etGhiChu             = view.findViewById(R.id.et_ghi_chu);

        tvSoDemPreview       = view.findViewById(R.id.tv_so_dem_preview);
        tvDonGiaPreview      = view.findViewById(R.id.tv_don_gia_preview);
        tvThanhTienPreview   = view.findViewById(R.id.tv_thanh_tien_preview);

        btnSaveBooking       = view.findViewById(R.id.btn_save_booking);

        // Set giờ mặc định
        if (tvGioCheckin  != null) tvGioCheckin.setText(gioCheckin);
        if (tvGioCheckout != null) tvGioCheckout.setText(gioCheckout);
    }

    private void updateAppBarTitle() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setAppBarTitle(
                    maDatPhong > 0 ? "Sửa đặt phòng" : "Đặt phòng");
        }
    }

    private void setupCancelButton(View view) {
        Button btnCancel = view.findViewById(R.id.btn_cancel_booking);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> navigateBack());
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }

    // =========================================================================
    // Khách hàng AutoComplete
    // =========================================================================

    private void setupKhachHangAutoComplete() {
        if (actvKhachHang == null) return;
        actvKhachHang.setThreshold(1);
        actvKhachHang.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String kw = s != null ? s.toString().trim() : "";
                if (kw.length() < 1) {
                    // Reset nếu xóa hết
                    selectedKhach = null;
                    return;
                }
                dbExecutor.execute(() -> {
                    List<KhachHang> results = khachHangDAO.search(kw);
                    mainHandler.post(() -> {
                        if (!isAdded()) return;
                        khachHangList = results;
                        List<String> names = new ArrayList<>();
                        for (KhachHang kh : results)
                            names.add(kh.getHoTen() + " – " + kh.getSdt());
                        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_dropdown_item_1line, names);
                        actvKhachHang.setAdapter(a);
                        actvKhachHang.showDropDown();
                    });
                });
            }
        });

        actvKhachHang.setOnItemClickListener((parent, v, position, id) -> {
            if (position < khachHangList.size()) {
                selectedKhach = khachHangList.get(position);
                // Điền thông tin đã có để người dùng tham khảo
                if (etTenKhach  != null) etTenKhach.setText(selectedKhach.getHoTen());
                if (etSdtKhach  != null) etSdtKhach.setText(selectedKhach.getSdt());
                if (etCccdKhach != null && selectedKhach.getCccd() != null)
                    etCccdKhach.setText(selectedKhach.getCccd());
            }
        });
    }

    // =========================================================================
    // DatePicker + TimePicker
    // =========================================================================

    private void setupDateTimePickers() {
        View root = getView();
        if (root == null) return;

        View llCheckinDate  = root.findViewById(R.id.ll_checkin_date);
        View llCheckinTime  = root.findViewById(R.id.ll_checkin_time);
        View llCheckoutDate = root.findViewById(R.id.ll_checkout_date);
        View llCheckoutTime = root.findViewById(R.id.ll_checkout_time);

        if (llCheckinDate  != null) llCheckinDate .setOnClickListener(v -> showDatePicker(true));
        if (llCheckoutDate != null) llCheckoutDate.setOnClickListener(v -> showDatePicker(false));
        if (llCheckinTime  != null) llCheckinTime .setOnClickListener(v -> showTimePicker(true));
        if (llCheckoutTime != null) llCheckoutTime.setOnClickListener(v -> showTimePicker(false));

        // Cũng set click trực tiếp lên TextView
        if (tvNgayCheckin  != null) tvNgayCheckin .setOnClickListener(v -> showDatePicker(true));
        if (tvNgayCheckout != null) tvNgayCheckout.setOnClickListener(v -> showDatePicker(false));
        if (tvGioCheckin   != null) tvGioCheckin  .setOnClickListener(v -> showTimePicker(true));
        if (tvGioCheckout  != null) tvGioCheckout .setOnClickListener(v -> showTimePicker(false));
    }

    private void showDatePicker(boolean isCheckin) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            year, month + 1, day);
                    if (isCheckin) {
                        ngayCheckin = date;
                        if (tvNgayCheckin != null) tvNgayCheckin.setText(date);
                    } else {
                        ngayCheckout = date;
                        if (tvNgayCheckout != null) tvNgayCheckout.setText(date);
                    }
                    updatePricePreview();
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker(boolean isCheckin) {
        Calendar cal = Calendar.getInstance();
        int defaultHour = isCheckin ? 14 : 12;
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (isCheckin) {
                        gioCheckin = time;
                        if (tvGioCheckin != null) tvGioCheckin.setText(time);
                    } else {
                        gioCheckout = time;
                        if (tvGioCheckout != null) tvGioCheckout.setText(time);
                    }
                },
                defaultHour, 0, true)
                .show();
    }

    // =========================================================================
    // Spinner phương thức thanh toán
    // =========================================================================

    private void setupPhuongThucTTSpinner() {
        if (spinnerPhuongThucTT == null) return;
        String[] options = {"Tiền mặt (TM)", "Chuyển khoản (CK)", "VNPAY"};
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhuongThucTT.setAdapter(a);
    }

    // =========================================================================
    // Tính tiền preview
    // =========================================================================

    private void updatePricePreview() {
        if (ngayCheckin == null || ngayCheckout == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(ngayCheckin);
            Date d2 = sdf.parse(ngayCheckout);
            if (d1 == null || d2 == null) return;
            long diffMs = d2.getTime() - d1.getTime();
            int soDem = (int) TimeUnit.MILLISECONDS.toDays(diffMs);
            if (soDem <= 0) return;

            double giaMoiDem = 0;
            if (phongList != null && spinnerPhong != null
                    && spinnerPhong.getSelectedItemPosition() < phongList.size()) {
                giaMoiDem = phongList.get(spinnerPhong.getSelectedItemPosition()).getGiaMoiDem();
            }

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
            double thanhTien = giaMoiDem * soDem;
            if (tvSoDemPreview    != null) tvSoDemPreview   .setText(soDem + " đêm");
            if (tvDonGiaPreview   != null) tvDonGiaPreview  .setText(nf.format((long) giaMoiDem) + " đ");
            if (tvThanhTienPreview!= null) tvThanhTienPreview.setText(nf.format((long) thanhTien) + " đ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Load dữ liệu ban đầu
    // =========================================================================

    private void loadFormData() {
        dbExecutor.execute(() -> {
            // Khi thêm mới: chỉ lấy phòng Trống
            // Khi sửa: lấy tất cả phòng
            List<Phong> rooms = maDatPhong > 0
                    ? roomRepository.getAllPhong()
                    : roomRepository.getAvailablePhong();

            DatPhong existing = maDatPhong > 0
                    ? bookingRepository.findDatPhongById(maDatPhong) : null;

            mainHandler.post(() -> {
                if (!isAdded()) return;
                phongList = rooms;
                setupPhongSpinnerData(rooms);
                if (existing != null) fillFormForEdit(existing);
                else if (maPhongPreselect > 0) preselectPhong(maPhongPreselect);
            });
        });
    }

    private void setupPhongSpinnerData(List<Phong> list) {
        if (spinnerPhong == null || list == null) return;
        List<String> labels = new ArrayList<>();
        for (Phong p : list)
            labels.add(p.getTenPhong() + " (" + p.getTenLoaiPhong() + ")");
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhong.setAdapter(a);

        spinnerPhong.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                updatePricePreview();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    }

    private void preselectPhong(int maPhong) {
        if (phongList == null || spinnerPhong == null) return;
        for (int i = 0; i < phongList.size(); i++) {
            if (phongList.get(i).getMaPhong() == maPhong) {
                spinnerPhong.setSelection(i);
                break;
            }
        }
    }

    private void fillFormForEdit(DatPhong dp) {
        ngayCheckin  = dp.getNgayCheckIn();
        ngayCheckout = dp.getNgayCheckOut();
        if (tvNgayCheckin  != null) tvNgayCheckin .setText(ngayCheckin != null  ? ngayCheckin  : "");
        if (tvNgayCheckout != null) tvNgayCheckout.setText(ngayCheckout != null ? ngayCheckout : "");
        if (etSoLuongKhach != null) etSoLuongKhach.setText(String.valueOf(dp.getSoLuongKhach()));
        if (etGhiChu != null && dp.getGhiChu() != null) etGhiChu.setText(dp.getGhiChu());
        if (actvKhachHang != null && dp.getTenKhachHang() != null)
            actvKhachHang.setText(dp.getTenKhachHang());
        preselectPhong(dp.getMaPhong());
        updatePricePreview();
    }

    // =========================================================================
    // Save
    // =========================================================================

    private void setupSaveButton() {
        if (btnSaveBooking != null) btnSaveBooking.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        // --- Bước 1: Xác định khách hàng ---
        // Ưu tiên: khách đã chọn từ autocomplete
        // Nếu chưa chọn: thử tạo khách mới từ form nhập tay
        String tenKhach  = etTenKhach  != null ? etTenKhach .getText().toString().trim() : "";
        String sdtKhach  = etSdtKhach  != null ? etSdtKhach .getText().toString().trim() : "";
        String cccdKhach = etCccdKhach != null ? etCccdKhach.getText().toString().trim() : "";

        if (selectedKhach == null) {
            // Kiểm tra form nhập khách mới
            if (TextUtils.isEmpty(tenKhach)) {
                Toast.makeText(requireContext(),
                        "Vui lòng chọn hoặc nhập tên khách hàng", Toast.LENGTH_SHORT).show();
                if (etTenKhach != null) etTenKhach.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(sdtKhach)) {
                Toast.makeText(requireContext(),
                        "Vui lòng nhập số điện thoại khách", Toast.LENGTH_SHORT).show();
                if (etSdtKhach != null) etSdtKhach.requestFocus();
                return;
            }
        }

        // --- Bước 2: Validate phòng & ngày ---
        if (phongList == null || phongList.isEmpty()) {
            Toast.makeText(requireContext(), "Không có phòng trống", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ngayCheckin)) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày check-in", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ngayCheckout)) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày check-out", Toast.LENGTH_SHORT).show();
            return;
        }

        int soDem = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(ngayCheckin);
            Date d2 = sdf.parse(ngayCheckout);
            if (d1 != null && d2 != null)
                soDem = (int) TimeUnit.MILLISECONDS.toDays(d2.getTime() - d1.getTime());
        } catch (Exception ignored) {}

        if (soDem <= 0) {
            Toast.makeText(requireContext(),
                    "Ngày check-out phải sau ngày check-in", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Bước 3: Build data ---
        final int maPhong = phongList.get(spinnerPhong.getSelectedItemPosition()).getMaPhong();
        int soLuongKhach = 1;
        try {
            soLuongKhach = Integer.parseInt(
                    etSoLuongKhach.getText().toString().trim());
        } catch (Exception ignored) {}

        String[] ptOptions = {"TM", "CK", "VNPAY"};
        String phuongThuc = ptOptions[spinnerPhuongThucTT.getSelectedItemPosition()];
        String ghiChu = etGhiChu.getText() != null ? etGhiChu.getText().toString().trim() : "";
        String ngayTao = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Ghi check-in/out có giờ vào ghi chú ngày (lưu vào cột NgayCheckIn/Out là date)
        // Giờ được lưu kèm trong chuỗi ngày: "yyyy-MM-dd HH:mm"
        final String checkinFull  = ngayCheckin  + " " + gioCheckin;
        final String checkoutFull = ngayCheckout + " " + gioCheckout;

        final int finalSoDem         = soDem;
        final int finalSoLuongKhach  = soLuongKhach;
        final String finalPhuongThuc = phuongThuc;
        final String finalGhiChu     = ghiChu.isEmpty() ? null : ghiChu;
        final String finalNgayTao    = ngayTao;
        final String finalTenKhach   = tenKhach;
        final String finalSdtKhach   = sdtKhach;
        final String finalCccdKhach  = cccdKhach;

        // Vô hiệu hoá nút để tránh double-click
        if (btnSaveBooking != null) btnSaveBooking.setEnabled(false);

        dbExecutor.execute(() -> {
            // Xác định MaKH: dùng khách đã chọn hoặc tạo mới
            int maKH = 0;
            if (selectedKhach != null) {
                maKH = selectedKhach.getMaKH();
            } else {
                // Tạo khách mới
                KhachHang newKH = new KhachHang();
                newKH.setHoTen(finalTenKhach);
                newKH.setSdt(finalSdtKhach);
                newKH.setCccd(finalCccdKhach.isEmpty() ? null : finalCccdKhach);
                newKH.setGioiTinh("Khac");
                long insertedId = khachHangDAO.insert(newKH);
                if (insertedId > 0) maKH = (int) insertedId;
            }

            if (maKH <= 0) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    if (btnSaveBooking != null) btnSaveBooking.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Không thể xác định khách hàng. Vui lòng thử lại.",
                            Toast.LENGTH_SHORT).show();
                });
                return;
            }

            final int finalMaKH = maKH;

            DatPhong dp = new DatPhong();
            if (maDatPhong > 0) dp.setMaDatPhong(maDatPhong);
            dp.setMaKH(finalMaKH);
            dp.setMaPhong(maPhong);
            // Lưu ngày thuần (yyyy-MM-dd), không ghép giờ để tránh lỗi parse FK/CHECK
            dp.setNgayCheckIn(ngayCheckin);
            dp.setNgayCheckOut(ngayCheckout);
            dp.setSoDem(finalSoDem);
            dp.setSoLuongKhach(finalSoLuongKhach);
            dp.setTrangThai("SapDen");
            dp.setPhuongThucThanhToan(finalPhuongThuc);
            // Ghép giờ vào GhiChu nếu người dùng nhập, để không mất thông tin
            String ghiChuFull = finalGhiChu != null ? finalGhiChu : "";
            if (!gioCheckin.equals("14:00") || !gioCheckout.equals("12:00")) {
                String gioInfo = "[Check-in: " + gioCheckin + ", Check-out: " + gioCheckout + "]";
                ghiChuFull = ghiChuFull.isEmpty() ? gioInfo : gioInfo + " " + ghiChuFull;
            }
            dp.setGhiChu(ghiChuFull.isEmpty() ? null : ghiChuFull);
            dp.setNgayTao(finalNgayTao);
            // MaNV = 0 không hợp lệ với FK → đặt 0, DatPhongDAO.toContentValues
            // sẽ cần xử lý null cho MaNV
            dp.setMaNV(0);

            boolean success;
            if (maDatPhong > 0) {
                success = bookingRepository.updateDatPhong(dp) > 0;
            } else {
                success = bookingRepository.createBooking(dp) > 0;
            }

            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (btnSaveBooking != null) btnSaveBooking.setEnabled(true);
                if (success) {
                    Snackbar.make(requireView(),
                            maDatPhong > 0 ? "Cập nhật đặt phòng thành công" : "Đặt phòng thành công!",
                            Snackbar.LENGTH_LONG).show();
                    navigateBack();
                } else {
                    Snackbar.make(requireView(),
                            "Lưu thất bại. Vui lòng thử lại.",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    // =========================================================================
    // Cleanup
    // =========================================================================

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
