package com.example.ql_homestay.ui.payment;

import android.os.Bundle;
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
import com.example.ql_homestay.adapter.InvoiceAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.HoaDon;
import com.example.ql_homestay.repository.InvoiceRepository;
import com.example.ql_homestay.ui.booking.BookingAddEditFragment;
import com.example.ql_homestay.util.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * G1 – Danh sách hóa đơn.
 * Hiển thị row tóm tắt nhanh (doanh thu hôm nay / số HĐ),
 * SearchBar lọc realtime, filter chips theo trạng thái.
 */
public class InvoiceListFragment extends Fragment {

    private InvoiceRepository invoiceRepo;
    private InvoiceAdapter adapter;
    private SessionManager session;

    // Views
    private TextView tvTodayRevenue, tvInvoiceCount;
    private EditText etSearch;
    private TextView chipAll, chipPaid, chipUnpaid, chipRefund;
    private RecyclerView rvInvoices;
    private View emptyState;

    /** Toàn bộ danh sách gốc, dùng để filter */
    private List<HoaDon> allInvoices = new ArrayList<>();
    /** Filter hiện tại: null = Tất cả */
    private String currentFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invoice_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        invoiceRepo = new InvoiceRepository(db);
        session     = SessionManager.getInstance(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        setupEmptyState(view);
        setupRecyclerView();
        setupSearch();
        setupFilterChips();

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // refresh khi quay lại từ Detail/Create
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvTodayRevenue  = view.findViewById(R.id.tv_today_revenue);
        tvInvoiceCount  = view.findViewById(R.id.tv_invoice_count);
        etSearch        = view.findViewById(R.id.et_search);
        chipAll         = view.findViewById(R.id.chip_all);
        chipPaid        = view.findViewById(R.id.chip_paid);
        chipUnpaid      = view.findViewById(R.id.chip_unpaid);
        chipRefund      = view.findViewById(R.id.chip_refund);
        rvInvoices      = view.findViewById(R.id.rv_invoices);
        emptyState      = view.findViewById(R.id.empty_state);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Thanh toán");
    }

    private void setupEmptyState(View view) {
        // Gán text gợi ý và click cho nút "Thêm mới" trong empty state
        View emptyView = view.findViewById(R.id.empty_state);
        if (emptyView == null) return;

        TextView tvSub = emptyView.findViewById(R.id.tv_empty_sub);
        if (tvSub != null) {
            tvSub.setText("Nhấn nút bên dưới để tạo đặt phòng mới");
            tvSub.setVisibility(View.VISIBLE);
        }

        TextView tvMsg = emptyView.findViewById(R.id.tv_empty_message);
        if (tvMsg != null) tvMsg.setText("Chưa có hóa đơn nào");

        View btnAdd = emptyView.findViewById(R.id.btn_empty_add);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> navigateToCreate());
        }
    }

    /** Mở màn hình tạo đặt phòng mới; hóa đơn sẽ được tạo tự động theo đặt phòng. */
    private void navigateToCreate() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, BookingAddEditFragment.newInstance(-1, -1))
                .addToBackStack(null)
                .commit();
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(requireContext());
        rvInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInvoices.setAdapter(adapter);

        adapter.setOnInvoiceClickListener(hoaDon -> {
            // Mở InvoiceDetailFragment
            Bundle args = new Bundle();
            args.putInt("maHD", hoaDon.getMaHD());
            InvoiceDetailFragment detailFrag = new InvoiceDetailFragment();
            detailFrag.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFrag)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(currentFilter, s.toString().trim());
            }
        });
    }

    private void setupFilterChips() {
        View.OnClickListener chipClick = v -> {
            String filter = null;
            if (v.getId() == R.id.chip_paid)   filter = "DaThanhToan";
            else if (v.getId() == R.id.chip_unpaid) filter = "ChuaThanhToan";
            else if (v.getId() == R.id.chip_refund) filter = "HoanTien";
            // chip_all → filter = null

            currentFilter = filter;
            updateChipStyles(v.getId());
            applyFilter(currentFilter, etSearch.getText().toString().trim());
        };

        chipAll.setOnClickListener(chipClick);
        chipPaid.setOnClickListener(chipClick);
        chipUnpaid.setOnClickListener(chipClick);
        chipRefund.setOnClickListener(chipClick);
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    private void loadData() {
        new Thread(() -> {
            allInvoices = invoiceRepo.getAllInvoices();

            // Tóm tắt nhanh
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            double todayRevenue = invoiceRepo.getTodayRevenue(today);
            int todayCount = invoiceRepo.getTodayInvoiceCount(today);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Summary row
                    String revenueStr = NumberFormat.getNumberInstance(Locale.getDefault())
                            .format((long) todayRevenue) + " đ";
                    tvTodayRevenue.setText("Tổng hôm nay: " + revenueStr);
                    tvInvoiceCount.setText("Số HĐ: " + todayCount);

                    applyFilter(currentFilter, etSearch.getText().toString().trim());
                });
            }
        }).start();
    }

    private void applyFilter(@Nullable String trangThai, @NonNull String keyword) {
        List<HoaDon> filtered = new ArrayList<>();
        for (HoaDon hd : allInvoices) {
            // Filter theo trạng thái
            if (trangThai != null && !trangThai.equals(hd.getTrangThai())) continue;
            // Filter theo keyword
            if (!keyword.isEmpty()) {
                String maHDStr = String.valueOf(hd.getMaHD());
                String tenKhach = hd.getTenKhachHang() != null ? hd.getTenKhachHang().toLowerCase() : "";
                if (!tenKhach.contains(keyword.toLowerCase()) && !maHDStr.contains(keyword)) continue;
            }
            filtered.add(hd);
        }
        adapter.setData(filtered);

        // Empty state
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvInvoices.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateChipStyles(int activeChipId) {
        int activeRes   = R.drawable.bg_chip_filter_active;
        int inactiveRes = R.drawable.bg_chip_filter_inactive;
        int activeTxt   = getResources().getColor(R.color.text_primary, null);
        int inactiveTxt = getResources().getColor(R.color.text_secondary, null);

        chipAll.setBackgroundResource(activeChipId == R.id.chip_all ? activeRes : inactiveRes);
        chipAll.setTextColor(activeChipId == R.id.chip_all ? activeTxt : inactiveTxt);

        chipPaid.setBackgroundResource(activeChipId == R.id.chip_paid ? activeRes : inactiveRes);
        chipPaid.setTextColor(activeChipId == R.id.chip_paid ? activeTxt : inactiveTxt);

        chipUnpaid.setBackgroundResource(activeChipId == R.id.chip_unpaid ? activeRes : inactiveRes);
        chipUnpaid.setTextColor(activeChipId == R.id.chip_unpaid ? activeTxt : inactiveTxt);

        chipRefund.setBackgroundResource(activeChipId == R.id.chip_refund ? activeRes : inactiveRes);
        chipRefund.setTextColor(activeChipId == R.id.chip_refund ? activeTxt : inactiveTxt);
    }
}
