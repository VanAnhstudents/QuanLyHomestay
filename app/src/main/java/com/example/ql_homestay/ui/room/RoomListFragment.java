package com.example.ql_homestay.ui.room;

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
import com.example.ql_homestay.adapter.RoomAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.Phong;
import com.example.ql_homestay.repository.RoomRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RoomListFragment – Danh sách phòng (B1).
 * - RecyclerView + RoomAdapter
 * - SearchBar realtime
 * - Filter chips: Tất cả / Trống / Đang thuê / Đã đặt
 * - FAB + Button "Thêm" chỉ hiện cho Admin
 */
public class RoomListFragment extends Fragment {

    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private RecyclerView rvRoomList;
    private View layoutEmpty;
    private FloatingActionButton fabAddRoom;
    private android.widget.Button btnAddRoom;
    private EditText etSearch;

    // Filter chips
    private TextView chipAll, chipTrong, chipDangThue, chipDaDat;
    private String currentFilter = null; // null = tất cả

    private RoomAdapter adapter;
    private RoomRepository roomRepository;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextWatcher searchTextWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager    = SessionManager.getInstance(requireContext());
        dbHelper          = DatabaseHelper.getInstance(requireContext());
        roomRepository    = new RoomRepository(dbHelper);

        bindViews(view);
        setupBreadcrumb(view);
        setupRecyclerView();
        setupSearchBar();
        setupFilterChips();
        setupFab();
        applyPermission();

        loadRooms(null, null);
    }

    private void bindViews(View view) {
        rvRoomList   = view.findViewById(R.id.rv_room_list);
        layoutEmpty  = view.findViewById(R.id.layout_empty);
        fabAddRoom   = view.findViewById(R.id.fab_add_room);
        btnAddRoom   = view.findViewById(R.id.btn_add_room);
        etSearch     = view.findViewById(R.id.et_search);
        chipAll      = view.findViewById(R.id.chip_all);
        chipTrong    = view.findViewById(R.id.chip_trong);
        chipDangThue = view.findViewById(R.id.chip_dang_thue);
        chipDaDat    = view.findViewById(R.id.chip_da_dat);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc != null) {
            TextView tv = bc.findViewById(R.id.tv_breadcrumb);
            if (tv != null) tv.setText("Trang chủ → Phòng");
        }
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter(requireContext());
        adapter.setOnRoomClickListener(this::openRoomDetail);
        rvRoomList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRoomList.setAdapter(adapter);
    }

    private void setupSearchBar() {
        searchTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String kw = s != null ? s.toString().trim() : "";
                loadRooms(kw.isEmpty() ? null : kw, currentFilter);
            }
        };
        etSearch.addTextChangedListener(searchTextWatcher);
    }

    private void setupFilterChips() {
        View.OnClickListener chipClick = v -> {
            // Reset tất cả về inactive
            setChipActive(chipAll,      false);
            setChipActive(chipTrong,    false);
            setChipActive(chipDangThue, false);
            setChipActive(chipDaDat,    false);

            String newFilter = null;
            if (v == chipAll) {
                setChipActive(chipAll, true);
            } else if (v == chipTrong) {
                setChipActive(chipTrong, true);
                newFilter = "Trong";
            } else if (v == chipDangThue) {
                setChipActive(chipDangThue, true);
                newFilter = "DangThue";
            } else if (v == chipDaDat) {
                setChipActive(chipDaDat, true);
                newFilter = "DaDat";
            }
            currentFilter = newFilter;
            String kw = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            loadRooms(kw.isEmpty() ? null : kw, currentFilter);
        };

        chipAll.setOnClickListener(chipClick);
        chipTrong.setOnClickListener(chipClick);
        chipDangThue.setOnClickListener(chipClick);
        chipDaDat.setOnClickListener(chipClick);

        // Mặc định "Tất cả" active
        setChipActive(chipAll, true);
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
        View.OnClickListener addClick = v -> openAddEditRoom(-1);
        if (fabAddRoom != null) fabAddRoom.setOnClickListener(addClick);
        if (btnAddRoom != null) btnAddRoom.setOnClickListener(addClick);
    }

    /**
     * Áp dụng RBAC: chỉ Admin thấy nút Thêm phòng.
     */
    private void applyPermission() {
        String vaiTro = sessionManager.getVaiTro();
        boolean canAdd = PermissionHelper.hasFullAccess(dbHelper, vaiTro,
                PermissionHelper.MODULE_QUAN_LY_PHONG);
        int vis = canAdd ? View.VISIBLE : View.GONE;
        if (fabAddRoom != null) fabAddRoom.setVisibility(vis);
        if (btnAddRoom != null) btnAddRoom.setVisibility(vis);
    }

    /**
     * Load phòng trên thread nền.
     * keyword != null → tìm kiếm; filter != null → lọc theo trạng thái.
     * Ưu tiên keyword hơn filter nếu cả hai đều có.
     */
    private void loadRooms(@Nullable String keyword, @Nullable String filter) {
        dbExecutor.execute(() -> {
            final List<Phong> result;
            if (keyword != null && !keyword.isEmpty()) {
                result = roomRepository.searchPhong(keyword);
            } else if (filter != null) {
                result = roomRepository.filterByTrangThai(filter);
            } else {
                result = roomRepository.getAllPhong();
            }
            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.setData(result);
                boolean isEmpty = result == null || result.isEmpty();
                if (layoutEmpty != null) layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                rvRoomList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void openRoomDetail(Phong phong) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, RoomDetailFragment.newInstance(phong.getMaPhong()))
                .addToBackStack(null)
                .commit();
    }

    private void openAddEditRoom(int maPhong) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, RoomAddEditFragment.newInstance(maPhong))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        String kw = etSearch != null && etSearch.getText() != null
                ? etSearch.getText().toString().trim() : "";
        loadRooms(kw.isEmpty() ? null : kw, currentFilter);
        applyPermission();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchTextWatcher != null)
            etSearch.removeTextChangedListener(searchTextWatcher);
        rvRoomList = null; layoutEmpty = null;
        fabAddRoom = null; btnAddRoom = null; etSearch = null;
        chipAll = null; chipTrong = null; chipDangThue = null; chipDaDat = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
