package com.example.ql_homestay.ui.staff;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.adapter.StaffAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.NhanVien;
import com.example.ql_homestay.repository.StaffRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffListFragment extends Fragment {

    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private RecyclerView rvStaffList;
    private View emptyState;
    private FloatingActionButton fabAdd;
    private TextInputEditText etSearch;
    private TextView chipAll, chipQuanLy, chipLeTan, chipKeToan, chipDonPhong, chipBaoVe;
    private String selectedChucVu = null;

    private StaffAdapter adapter;
    private StaffRepository repository;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    private TextWatcher searchWatcher;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        repository = new StaffRepository(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        setupRecyclerView();
        setupSearch();
        setupChucVuFilter();
        setupFab();
        applyPermission();
        loadStaff(null);
    }

    private void bindViews(View view) {
        rvStaffList = view.findViewById(R.id.rv_staff_list);
        emptyState = view.findViewById(R.id.empty_state);
        fabAdd = view.findViewById(R.id.fab_add_staff);
        etSearch = view.findViewById(R.id.et_search);
        chipAll = view.findViewById(R.id.chip_all);
        chipQuanLy = view.findViewById(R.id.chip_quan_ly);
        chipLeTan = view.findViewById(R.id.chip_le_tan);
        chipKeToan = view.findViewById(R.id.chip_ke_toan);
        chipDonPhong = view.findViewById(R.id.chip_don_phong);
        chipBaoVe = view.findViewById(R.id.chip_bao_ve);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Nhân viên");
    }

    private void setupRecyclerView() {
        adapter = new StaffAdapter(this::openDetail);
        rvStaffList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStaffList.setAdapter(adapter);
    }

    private void setupSearch() {
        searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                reloadStaff();
            }
        };
        etSearch.addTextChangedListener(searchWatcher);
    }

    private void setupChucVuFilter() {
        View.OnClickListener chipClick = v -> {
            setAllChipsInactive();
            selectedChucVu = null;
            if (v == chipAll) {
                setChipActive(chipAll, true);
            } else if (v == chipQuanLy) {
                selectedChucVu = "QuanLy";
                setChipActive(chipQuanLy, true);
            } else if (v == chipLeTan) {
                selectedChucVu = "LeTan";
                setChipActive(chipLeTan, true);
            } else if (v == chipKeToan) {
                selectedChucVu = "KeToan";
                setChipActive(chipKeToan, true);
            } else if (v == chipDonPhong) {
                selectedChucVu = "DonPhong";
                setChipActive(chipDonPhong, true);
            } else if (v == chipBaoVe) {
                selectedChucVu = "BaoVe";
                setChipActive(chipBaoVe, true);
            }
            reloadStaff();
        };

        chipAll.setOnClickListener(chipClick);
        chipQuanLy.setOnClickListener(chipClick);
        chipLeTan.setOnClickListener(chipClick);
        chipKeToan.setOnClickListener(chipClick);
        chipDonPhong.setOnClickListener(chipClick);
        chipBaoVe.setOnClickListener(chipClick);
        setChipActive(chipAll, true);
    }

    private void setAllChipsInactive() {
        setChipActive(chipAll, false);
        setChipActive(chipQuanLy, false);
        setChipActive(chipLeTan, false);
        setChipActive(chipKeToan, false);
        setChipActive(chipDonPhong, false);
        setChipActive(chipBaoVe, false);
    }

    private void setChipActive(TextView chip, boolean active) {
        if (chip == null) return;
        chip.setBackgroundResource(active
                ? R.drawable.bg_chip_filter_active
                : R.drawable.bg_chip_filter_inactive);
        chip.setTextColor(requireContext().getResources().getColor(active
                ? R.color.text_primary
                : R.color.text_secondary, null));
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> openAddEdit(-1));
    }

    private void applyPermission() {
        boolean isAdmin = PermissionHelper.hasFullAccess(
                dbHelper, session.getVaiTro(), PermissionHelper.MODULE_NHAN_VIEN);
        fabAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void loadStaff(@Nullable String keyword) {
        executor.execute(() -> {
            List<NhanVien> result = (keyword == null)
                    ? repository.getAllStaff()
                    : repository.searchStaff(keyword);
            if (selectedChucVu != null) {
                List<NhanVien> filtered = new ArrayList<>();
                for (NhanVien nv : result) {
                    if (selectedChucVu.equals(nv.getChucVu())) filtered.add(nv);
                }
                result = filtered;
            }
            List<NhanVien> displayResult = result;
            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.setData(displayResult);
                boolean empty = displayResult == null || displayResult.isEmpty();
                if (emptyState != null) emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvStaffList.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void reloadStaff() {
        String kw = etSearch != null && etSearch.getText() != null
                ? etSearch.getText().toString().trim() : "";
        loadStaff(kw.isEmpty() ? null : kw);
    }

    private void openDetail(NhanVien nv) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, StaffDetailFragment.newInstance(nv.getMaNV()))
                .addToBackStack(null)
                .commit();
    }

    private void openAddEdit(int maNV) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, StaffAddEditFragment.newInstance(maNV))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadStaff();
        applyPermission();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchWatcher != null) {
            etSearch.removeTextChangedListener(searchWatcher);
        }
        rvStaffList = null;
        emptyState = null;
        fabAdd = null;
        etSearch = null;
        chipAll = null;
        chipQuanLy = null;
        chipLeTan = null;
        chipKeToan = null;
        chipDonPhong = null;
        chipBaoVe = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
