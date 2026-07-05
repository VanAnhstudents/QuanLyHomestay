package com.example.ql_homestay.ui.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
 * Truyền maDatPhong = -1 để tạo mới; maDatPhong > 0 để chỉnh sửa.
 * Truyền maPhongPreSelect > 0 để pre-select phòng (từ RoomDetailFragment).
 *
 * Tính tiền realtime:
 *   SoDem = (checkOut - checkIn) / 1 day
 *   ThanhTien = GiaMoiDem * SoDem
 */
public class BookingAddEditFragment extends Fragment {

    private static final String ARG_MA_DAT_PHONG = "ma_dat_phong";
    private static final String ARG_MA_PHONG_PRESELECT = "ma_phong_pre";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private int maDatPhong = -1;
    private int maPhongPreselect = -1;

    // Views
    private AutoCompleteTextView actvKhachHang;
    private Button btnAddNewCustomer;
    private Spinner spinnerPhong;
    private TextView tvNgayCheckin, tvNgayCheckout;
    private EditText etSoLuongKhach, etGhiChu;
    private Spinner spinnerPhuongThucTT;
    private TextView tvSoDemPreview, tvDonGiaPreview, tvThanhTienPreview;
    private Button btnSaveBooking;

    // Data
    private List<KhachHang> khachHangList = new ArrayList<>();
    private List<Phong> phongList         = new ArrayList<>();
    private KhachHang selectedKhach       = null;
    private String ngayCheckin            = null;
    private String ngayCheckout           = null;

    private BookingRepository bookingRepository;
    private RoomRepository roomRepository;
    private KhachHangDAO khachHangDAO;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static BookingAddEditFragment newInstance(int maDatPhong, int maPhongPreselect) {
        BookingAddEditFragment f = new BookingAddEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_DAT_PHONG, maDatPhong);
        args.putInt(ARG_MA_PHONG_PRESELECT, maPhongPreselect);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maDatPhong      = getArguments().getInt(ARG_MA_DAT_PHONG, -1);
            maPhongPreselect = getArguments().getInt(ARG_MA_PHONG_PRESELECT, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
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
        setupBackButton(view);
        setupDatePickers();
        setupPhongSpinner();
        setupKhachHangAutoComplete();
        setupPhuongThucTTSpinner();
        setupSaveButton();

        loadFormData();
    }

    private void bindViews(View view) {
        actvKhachHang         = view.findViewById(R.id.actv_khach_hang);
        btnAddNewCustomer     = view.findViewById(R.id.btn_add_new_customer);
        spinnerPhong          = view.findViewById(R.id.spinner_phong);
        tvNgayCheckin         = view.findViewById(R.id.tv_ngay_checkin);
        tvNgayCheckout        = view.findViewById(R.id.tv_ngay_checkout);
        etSoLuongKhach        = view.findViewById(R.id.et_so_luong_khach);
        spinnerPhuongThucTT   = view.findViewById(R.id.spinner_phuong_thuc_tt);
        etGhiChu              = view.findViewById(R.id.et_ghi_chu);
        tvSoDemPreview        = view.findViewById(R.id.tv_so_dem_preview);
        tvDonGiaPreview       = view.findViewById(R.id.tv_don_gia_preview);
        tvThanhTienPreview    = view.findViewById(R.id.tv_thanh_tien_preview);
        btnSaveBooking        = view.findViewById(R.id.btn_save_booking);
    }

    private void setupBackButton(View view) {
        View appbar = view.findViewById(R.id.appbar);
        View btnBack = appbar != null ? appbar.findViewById(R.id.btn_appbar_back) : null;
        if (btnBack == null) btnBack = view.findViewById(R.id.btn_appbar_back);
        if (btnBack != null)
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
            });
    }

    private void setupKhachHangAutoComplete() {
        actvKhachHang.setThreshold(1);
        actvKhachHang.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String kw = s != null ? s.toString().trim() : "";
                if (kw.length() < 1) return;
                dbExecutor.execute(() -> {
                    List<KhachHang> results = khachHangDAO.search(kw);
                    mainHandler.post(() -> {
                        if (!isAdded()) return;
                        khachHangList = results;
                        List<String> names = new ArrayList<>();
                        for (KhachHang kh : results)
                            names.add(kh.getHoTen() + " (" + kh.getSdt() + ")");
                        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_dropdown_item_1line, names);
                        actvKhachHang.setAdapter(a);
                        actvKhachHang.showDropDown();
                        if (btnAddNewCustomer != null)
                            btnAddNewCustomer.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                });
            }
        });

        actvKhachHang.setOnItemClickListener((parent, v, position, id) -> {
            if (position < khachHangList.size()) {
                selectedKhach = khachHangList.get(position);
                if (btnAddNewCustomer != null) btnAddNewCustomer.setVisibility(View.GONE);
            }
        });
    }

    private void setupPhongSpinner() {
        // Phòng sẽ được load sau khi loadFormData() chạy xong
    }

    private void setupPhuongThucTTSpinner() {
        if (spinnerPhuongThucTT == null) return;
        String[] options = {"Tiền mặt (TM)", "Chuyển khoản (CK)", "VNPAY"};
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhuongThucTT.setAdapter(a);
    }

    private void setupDatePickers() {
        if (tvNgayCheckin != null) {
            tvNgayCheckin.setOnClickListener(v -> showDatePicker(true));
        }
        if (tvNgayCheckout != null) {
            tvNgayCheckout.setOnClickListener(v -> showDatePicker(false));
        }
    }

    private void showDatePicker(boolean isCheckin) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    if (isCheckin) {
                        ngayCheckin = date;
                        if (tvNgayCheckin != null) tvNgayCheckin.setText(date);
                    } else {
                        ngayCheckout = date;
                        if (tvNgayCheckout != null) tvNgayCheckout.setText(date);
                    }
                    updatePricePreview();
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /** Tính lại SoDem + ThanhTien và hiện lên preview */
    private void updatePricePreview() {
        if (ngayCheckin == null || ngayCheckout == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(ngayCheckin);
            Date d2 = sdf.parse(ngayCheckout);
            if (d1 == null || d2 == null) return;
            long diffMs = d2.getTime() - d1.getTime();
            int soDem = (int) TimeUnit.MILLISECONDS.toDays(diffMs);
            if (soDem <= 0) {
                Toast.makeText(requireContext(), "Ngày check-out phải sau check-in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy giá phòng đã chọn
            double giaMoiDem = 0;
            if (phongList != null && spinnerPhong != null
                    && spinnerPhong.getSelectedItemPosition() < phongList.size()) {
                giaMoiDem = phongList.get(spinnerPhong.getSelectedItemPosition()).getGiaMoiDem();
            }

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
            double thanhTien = giaMoiDem * soDem;
            if (tvSoDemPreview != null)     tvSoDemPreview.setText(soDem + " đêm");
            if (tvDonGiaPreview != null)    tvDonGiaPreview.setText(nf.format((long) giaMoiDem) + " đ");
            if (tvThanhTienPreview != null) tvThanhTienPreview.setText(nf.format((long) thanhTien) + " đ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFormData() {
        dbExecutor.execute(() -> {
            // Chỉ lấy phòng Trống (cho form đặt phòng mới)
            List<Phong> availablePhong = maDatPhong > 0
                    ? roomRepository.getAllPhong()  // khi edit, hiện tất cả
                    : roomRepository.getAvailablePhong();

            DatPhong existing = maDatPhong > 0 ? bookingRepository.findDatPhongById(maDatPhong) : null;

            mainHandler.post(() -> {
                if (!isAdded()) return;
                phongList = availablePhong;
                setupPhongSpinnerData(availablePhong);
                if (existing != null) fillFormForEdit(existing);
                else if (maPhongPreselect > 0) preselectPhong(maPhongPreselect);
            });
        });
    }

    private void setupPhongSpinnerData(List<Phong> list) {
        if (spinnerPhong == null || list == null) return;
        List<String> labels = new ArrayList<>();
        for (Phong p : list) labels.add(p.getTenPhong() + " (" + p.getTenLoaiPhong() + ")");
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhong.setAdapter(a);

        // Khi đổi phòng, cập nhật giá preview
        spinnerPhong.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                updatePricePreview();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
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
        if (tvNgayCheckin  != null) tvNgayCheckin.setText(ngayCheckin);
        if (tvNgayCheckout != null) tvNgayCheckout.setText(ngayCheckout);
        if (etSoLuongKhach != null) etSoLuongKhach.setText(String.valueOf(dp.getSoLuongKhach()));
        if (etGhiChu != null && dp.getGhiChu() != null) etGhiChu.setText(dp.getGhiChu());
        if (actvKhachHang != null && dp.getTenKhachHang() != null)
            actvKhachHang.setText(dp.getTenKhachHang());

        // Pre-select phòng
        preselectPhong(dp.getMaPhong());
        updatePricePreview();
    }

    private void setupSaveButton() {
        if (btnSaveBooking != null) btnSaveBooking.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        if (selectedKhach == null && actvKhachHang != null) {
            // Cố tìm theo text nhập
            String kw = actvKhachHang.getText() != null ? actvKhachHang.getText().toString().trim() : "";
            if (kw.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (ngayCheckin == null || ngayCheckin.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày check-in", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ngayCheckout == null || ngayCheckout.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày check-out", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phongList == null || phongList.isEmpty()) {
            Toast.makeText(requireContext(), "Không có phòng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        int maKH   = selectedKhach != null ? selectedKhach.getMaKH() : 0;
        int maPhong = phongList.get(spinnerPhong.getSelectedItemPosition()).getMaPhong();

        int soDem = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d1 = sdf.parse(ngayCheckin);
            Date d2 = sdf.parse(ngayCheckout);
            if (d1 != null && d2 != null)
                soDem = (int) TimeUnit.MILLISECONDS.toDays(d2.getTime() - d1.getTime());
        } catch (Exception ignored) {}

        if (soDem <= 0) {
            Toast.makeText(requireContext(), "Ngày check-out phải sau check-in", Toast.LENGTH_SHORT).show();
            return;
        }

        int soLuongKhach = 1;
        try { soLuongKhach = Integer.parseInt(etSoLuongKhach.getText().toString().trim()); }
        catch (Exception ignored) {}

        String[] ptOptions = {"TM", "CK", "VNPAY"};
        String phuongThuc = ptOptions[spinnerPhuongThucTT.getSelectedItemPosition()];
        String ghiChu = etGhiChu.getText() != null ? etGhiChu.getText().toString().trim() : "";

        String ngayTao = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatPhong dp = new DatPhong();
        dp.setMaKH(maKH);
        dp.setMaPhong(maPhong);
        dp.setNgayCheckIn(ngayCheckin);
        dp.setNgayCheckOut(ngayCheckout);
        dp.setSoDem(soDem);
        dp.setSoLuongKhach(soLuongKhach);
        dp.setTrangThai("SapDen");
        dp.setPhuongThucThanhToan(phuongThuc);
        dp.setGhiChu(ghiChu.isEmpty() ? null : ghiChu);
        dp.setNgayTao(ngayTao);

        if (maDatPhong > 0) dp.setMaDatPhong(maDatPhong);

        dbExecutor.execute(() -> {
            boolean success;
            if (maDatPhong > 0) {
                success = bookingRepository.updateDatPhong(dp) > 0;
            } else {
                success = bookingRepository.createBooking(dp) > 0;
            }
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (success) {
                    Snackbar.make(requireView(),
                            maDatPhong > 0 ? "Cập nhật đặt phòng thành công" : "Đặt phòng thành công!",
                            Snackbar.LENGTH_LONG).show();
                    if (getParentFragmentManager().getBackStackEntryCount() > 0)
                        getParentFragmentManager().popBackStack();
                } else {
                    Snackbar.make(requireView(), "Lưu thất bại. Vui lòng thử lại.",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
