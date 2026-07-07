package com.example.ql_homestay.ui.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.adapter.BookingAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.repository.BookingRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BookingListFragment – Danh sách đặt phòng (C1).
 * - RecyclerView + BookingAdapter
 * - SearchBar realtime
 * - Filter chips: Tất cả / Sắp đến / Đang ở / Đã trả phòng / Đã hủy
 * - FAB ẩn nếu không phải Admin / Lễ tân
 */
public class BookingListFragment extends Fragment {

    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private RecyclerView rvBookingList;
    private View layoutEmpty;
    private FloatingActionButton fabAddBooking;
    private EditText etSearch;

    // Filter chips
    private TextView chipAll, chipSapDen, chipDangO, chipDaTra, chipDaHuy;
    private String currentFilter = null;

    private BookingAdapter adapter;
    private BookingRepository bookingRepository;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextWatcher searchWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        bookingRepository = new BookingRepository(dbHelper);

        bindViews(view);
        setupBreadcrumb(view);
        setupRecyclerView();
        setupSearchBar();
        setupFilterChips();
        setupFab();
        applyPermission();

        loadBookings(null, null);
    }

    private void bindViews(View view) {
        rvBookingList = view.findViewById(R.id.rv_booking_list);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fabAddBooking = view.findViewById(R.id.fab_add_booking);
        etSearch = view.findViewById(R.id.et_search);
        chipAll = view.findViewById(R.id.chip_all);
        chipSapDen = view.findViewById(R.id.chip_sap_den);
        chipDangO = view.findViewById(R.id.chip_dang_o);
        chipDaTra = view.findViewById(R.id.chip_da_tra);
        chipDaHuy = view.findViewById(R.id.chip_da_huy);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc != null) {
            TextView tv = bc.findViewById(R.id.tv_breadcrumb);
            if (tv != null) tv.setText("Trang chủ → Đặt phòng");
        }
    }

    private void setupRecyclerView() {
        adapter = new BookingAdapter(requireContext());
        adapter.setOnBookingClickListener(this::openBookingDetail);
        rvBookingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookingList.setAdapter(adapter);
    }

    private void setupSearchBar() {
        searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String kw = s != null ? s.toString().trim() : "";
                loadBookings(kw.isEmpty() ? null : kw, currentFilter);
            }
        };
        etSearch.addTextChangedListener(searchWatcher);
    }

    private void setupFilterChips() {
        View.OnClickListener chipClick = v -> {
            resetChips();
            String newFilter = null;
            if (v == chipAll)     { setChipActive(chipAll, true); }
            else if (v == chipSapDen) { setChipActive(chipSapDen, true); newFilter = "SapDen"; }
            else if (v == chipDangO)  { setChipActive(chipDangO,  true); newFilter = "DangO"; }
            else if (v == chipDaTra)  { setChipActive(chipDaTra,  true); newFilter = "DaTraPhong"; }
            else if (v == chipDaHuy)  { setChipActive(chipDaHuy,  true); newFilter = "DaHuy"; }

            currentFilter = newFilter;
            String kw = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            loadBookings(kw.isEmpty() ? null : kw, currentFilter);
        };
        chipAll.setOnClickListener(chipClick);
        chipSapDen.setOnClickListener(chipClick);
        chipDangO.setOnClickListener(chipClick);
        chipDaTra.setOnClickListener(chipClick);
        chipDaHuy.setOnClickListener(chipClick);

        // Mặc định role NhanVien → active "Đang ở"
        if ("NhanVien".equals(sessionManager.getVaiTro())) {
            resetChips();
            setChipActive(chipDangO, true);
            currentFilter = "DangO";
        } else {
            setChipActive(chipAll, true);
        }
    }

    private void resetChips() {
        setChipActive(chipAll,    false);
        setChipActive(chipSapDen, false);
        setChipActive(chipDangO,  false);
        setChipActive(chipDaTra,  false);
        setChipActive(chipDaHuy,  false);
    }

    private void setChipActive(TextView chip, boolean active) {
        if (chip == null) return;
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_filter_active);
            chip.setTextColor(requireContext().getResources().getColor(R.color.text_primary, null));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_filter_inactive);
            chip.setTextColor(requireContext().getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void setupFab() {
        if (fabAddBooking != null)
            fabAddBooking.setOnClickListener(v -> openAddEditBooking(-1));
    }

    /**
     * RBAC: FAB ẩn nếu không phải Admin / Lễ tân.
     */
    private void applyPermission() {
        String vaiTro = sessionManager.getVaiTro();
        boolean canAdd = PermissionHelper.hasAccess(dbHelper, vaiTro,
                PermissionHelper.MODULE_QUAN_LY_DAT_PHONG, PermissionHelper.QUYEN_TOAN_QUYEN);
        if (fabAddBooking != null)
            fabAddBooking.setVisibility(canAdd ? View.VISIBLE : View.GONE);
    }

    private void loadBookings(@Nullable String keyword, @Nullable String filter) {
        dbExecutor.execute(() -> {
            final List<DatPhong> result;
            if (keyword != null && !keyword.isEmpty()) {
                result = bookingRepository.searchDatPhong(keyword);
            } else if (filter != null) {
                result = bookingRepository.filterByTrangThai(filter);
            } else {
                result = bookingRepository.getAllDatPhong();
            }
            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.setData(result);
                boolean empty = result == null || result.isEmpty();
                if (layoutEmpty != null) layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvBookingList.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void openBookingDetail(DatPhong dp) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID,
                        BookingDetailFragment.newInstance(dp.getMaDatPhong()))
                .addToBackStack(null)
                .commit();
    }

    private void openAddEditBooking(int maDatPhong) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID,
                        BookingAddEditFragment.newInstance(maDatPhong, -1))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        String kw = etSearch != null && etSearch.getText() != null
                ? etSearch.getText().toString().trim() : "";
        loadBookings(kw.isEmpty() ? null : kw, currentFilter);
        applyPermission();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchWatcher != null)
            etSearch.removeTextChangedListener(searchWatcher);
        rvBookingList = null; layoutEmpty = null; fabAddBooking = null; etSearch = null;
        chipAll = null; chipSapDen = null; chipDangO = null; chipDaTra = null; chipDaHuy = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
