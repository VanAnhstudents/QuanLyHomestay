package com.example.ql_homestay.ui.customer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.adapter.CustomerAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.KhachHang;
import com.example.ql_homestay.repository.CustomerRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CustomerListFragment — Danh sách khách hàng (B1 trong lo_trinh.md / D1 trong ux_ui.md).
 * Chức năng:
 *   - RecyclerView dùng item_customer_row.xml (avatar initials + tên + SĐT) qua CustomerAdapter.
 *   - SearchBar lọc realtime qua TextWatcher (gọi CustomerRepository.searchCustomers()).
 *   - FAB "+ Thêm" → ẩn nếu PermissionHelper.hasAccess(dbHelper, vaiTro, QuanLyKhachHang, ToanQuyen) = false.
 *   - Bấm vào 1 dòng → điều hướng sang CustomerDetailFragment (stub, sẽ hoàn thiện ở D2).
 *   - Bấm FAB → điều hướng sang CustomerAddEditFragment với maKH = -1 (stub, sẽ hoàn thiện ở D3).
 * LƯU Ý QUAN TRỌNG VỀ ID CONTAINER:
 *   Mã dưới đây dùng requireActivity().getSupportFragmentManager() và giả định
 *   container Fragment trong activity_main.xml có id = R.id.fragment_container.
 *   Nếu MainActivity dùng id khác (ví dụ R.id.frame_container), cần sửa lại
 *   hằng số FRAGMENT_CONTAINER_ID bên dưới cho khớp — file activity_main.xml
 *   thật chưa được cung cấp tại thời điểm viết class này.
 * Threading: theo lưu ý kỹ thuật mục 5 trong lo_trinh.md — không xử lý DB trên
 * Main Thread. Dùng ExecutorService (1 thread nền) + Handler(Looper.getMainLooper())
 * để post kết quả về UI, thay vì AsyncTask (đã deprecated).
 */
public class CustomerListFragment extends Fragment {

    // TODO: đổi lại nếu id container thật trong activity_main.xml khác.
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private RecyclerView rvCustomerList;
    private View emptyStateView;
    private FloatingActionButton fabAddCustomer;
    private TextInputEditText etSearch;

    private CustomerAdapter adapter;
    private CustomerRepository customerRepository;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextWatcher searchTextWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        customerRepository = new CustomerRepository(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        setupRecyclerView();
        setupSearchBar();
        setupFab();
        applyPermission();

        loadCustomers(null);
    }

    private void bindViews(View view) {
        rvCustomerList = view.findViewById(R.id.rv_customer_list);
        emptyStateView = view.findViewById(R.id.empty_state);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        etSearch = view.findViewById(R.id.et_search);
    }

    /** Breadcrumb "Trang chủ → Khách hàng" theo quy ước layout_breadcrumb.xml. */
    private void setupBreadcrumb(View view) {
        View breadcrumbInclude = view.findViewById(R.id.breadcrumb);
        if (breadcrumbInclude != null) {
            android.widget.TextView tvBreadcrumb = breadcrumbInclude.findViewById(R.id.tv_breadcrumb);
            if (tvBreadcrumb != null) {
                tvBreadcrumb.setText("Trang chủ → Khách hàng");
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter(this::onCustomerClicked);
        rvCustomerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCustomerList.setAdapter(adapter);
    }

    /** SearchBar lọc realtime qua TextWatcher (theo lo_trinh.md B1). */
    private void setupSearchBar() {
        searchTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s != null ? s.toString().trim() : "";
                loadCustomers(keyword.isEmpty() ? null : keyword);
            }
        };
        etSearch.addTextChangedListener(searchTextWatcher);
    }

    /**
     * FAB "+ Thêm" → mở CustomerAddEditFragment với maKH = -1 (chế độ thêm mới).
     */
    private void setupFab() {
        fabAddCustomer.setOnClickListener(v -> openAddEditCustomer(-1));
    }

    /**
     * Áp dụng RBAC: ẩn FAB nếu vai trò hiện tại không có ToanQuyen trên module
     * QuanLyKhachHang (Kế toán, Nhân viên → ChiXem theo seed PhanQuyen_VaiTro).
     */
    private void applyPermission() {
        String vaiTro = sessionManager.getVaiTro();
        boolean coToanQuyen = PermissionHelper.hasAccess(
                dbHelper, vaiTro,
                PermissionHelper.MODULE_QUAN_LY_KHACH,
                PermissionHelper.QUYEN_TOAN_QUYEN);
        fabAddCustomer.setVisibility(coToanQuyen ? View.VISIBLE : View.GONE);
    }

    /**
     * Load danh sách khách hàng (toàn bộ nếu keyword == null, hoặc lọc theo
     * keyword) trên thread nền, rồi cập nhật UI trên Main Thread.
     */
    private void loadCustomers(@Nullable String keyword) {
        dbExecutor.execute(() -> {
            final List<KhachHang> result = (keyword == null)
                    ? customerRepository.getAllCustomers()
                    : customerRepository.searchCustomers(keyword);

            mainHandler.post(() -> {
                if (!isAdded()) return; // Fragment có thể đã bị detach khi kết quả trả về
                adapter.setData(result);
                toggleEmptyState(result == null || result.isEmpty());
            });
        });
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (emptyStateView == null) return;
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCustomerList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void onCustomerClicked(KhachHang khachHang) {
        openCustomerDetail(khachHang.getMaKH());
    }

    private void openCustomerDetail(int maKH) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, CustomerDetailFragment.newInstance(maKH))
                .addToBackStack(null)
                .commit();
    }

    private void openAddEditCustomer(int maKH) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, CustomerAddEditFragment.newInstance(maKH))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (etSearch == null) return;
        String currentKeyword = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        loadCustomers(currentKeyword.isEmpty() ? null : currentKeyword);
        applyPermission();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchTextWatcher != null) {
            etSearch.removeTextChangedListener(searchTextWatcher);
        }
        rvCustomerList = null;
        emptyStateView = null;
        fabAddCustomer = null;
        etSearch = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}