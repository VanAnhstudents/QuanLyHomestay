package com.example.ql_homestay.ui.account;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.adapter.AccountAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.model.TaiKhoan;
import com.example.ql_homestay.repository.PermissionRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Danh sách tài khoản — chỉ Admin truy cập.
 * Filter chips: Tất cả / Admin / Nhân viên / Đã khóa.
 * PopupMenu từ icon 3 chấm: Sửa / Khóa (toggle) / Xóa.
 */
public class AccountListFragment extends Fragment {

    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;
    private static final String FILTER_ALL = "all";
    private static final String FILTER_ADMIN = "Admin";
    private static final String FILTER_NHANVIEN = "NhanVien";
    private static final String FILTER_KHOA = "khoa";

    private RecyclerView rvList;
    private View emptyState;
    private FloatingActionButton fabAdd;
    private TextInputEditText etSearch;
    private TextView chipAll, chipAdmin, chipNhanVien, chipKhoa;

    private AccountAdapter adapter;
    private TaiKhoanDAO taiKhoanDAO;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    private String currentFilter = FILTER_ALL;
    private String currentKeyword = "";
    private TextWatcher searchWatcher;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        taiKhoanDAO = new TaiKhoanDAO(dbHelper);

        rvList = view.findViewById(R.id.rv_account_list);
        emptyState = view.findViewById(R.id.empty_state);
        fabAdd = view.findViewById(R.id.fab_add_account);
        etSearch = view.findViewById(R.id.et_search);
        chipAll = view.findViewById(R.id.chip_all);
        chipAdmin = view.findViewById(R.id.chip_admin);
        chipNhanVien = view.findViewById(R.id.chip_nhanvien);
        chipKhoa = view.findViewById(R.id.chip_khoa);

        setupBreadcrumb(view);
        setupRecyclerView();
        setupSearch();
        setupChips();
        setupFab();
        applyPermission();
        load();
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc != null) {
            TextView tv = bc.findViewById(R.id.tv_breadcrumb);
            if (tv != null) tv.setText("Trang chủ → Tài khoản");
        }
    }

    private void setupRecyclerView() {
        adapter = new AccountAdapter(new AccountAdapter.OnActionListener() {
            @Override public void onItemClick(TaiKhoan tk) { openDetail(tk.getMaTK()); }
            @Override public void onEdit(TaiKhoan tk)      { openAddEdit(tk.getMaTK()); }
            @Override public void onToggleLock(TaiKhoan tk){ toggleLock(tk); }
            @Override public void onDelete(TaiKhoan tk)    { confirmDelete(tk); }
        });
        rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvList.setAdapter(adapter);
    }

    private void setupSearch() {
        searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int start, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentKeyword = s != null ? s.toString().trim() : "";
                load();
            }
        };
        etSearch.addTextChangedListener(searchWatcher);
    }

    private void setupChips() {
        View.OnClickListener chipClick = v -> {
            if (v.getId() == R.id.chip_all)      setFilter(FILTER_ALL,      chipAll);
            else if (v.getId() == R.id.chip_admin) setFilter(FILTER_ADMIN,   chipAdmin);
            else if (v.getId() == R.id.chip_nhanvien) setFilter(FILTER_NHANVIEN, chipNhanVien);
            else if (v.getId() == R.id.chip_khoa)  setFilter(FILTER_KHOA,   chipKhoa);
        };
        chipAll.setOnClickListener(chipClick);
        chipAdmin.setOnClickListener(chipClick);
        chipNhanVien.setOnClickListener(chipClick);
        chipKhoa.setOnClickListener(chipClick);
    }

    private void setFilter(String filter, TextView activeChip) {
        currentFilter = filter;
        chipAll.setBackgroundResource(R.drawable.bg_chip_filter_inactive);
        chipAdmin.setBackgroundResource(R.drawable.bg_chip_filter_inactive);
        chipNhanVien.setBackgroundResource(R.drawable.bg_chip_filter_inactive);
        chipKhoa.setBackgroundResource(R.drawable.bg_chip_filter_inactive);
        activeChip.setBackgroundResource(R.drawable.bg_chip_filter_active);
        load();
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> openAddEdit(-1));
    }

    private void applyPermission() {
        // Module Tài khoản = CaiDatHeThong, chỉ Admin
        boolean isAdmin = PermissionHelper.hasFullAccess(
                dbHelper, session.getVaiTro(), PermissionHelper.MODULE_CAI_DAT);
        fabAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void load() {
        executor.execute(() -> {
            List<TaiKhoan> raw;
            switch (currentFilter) {
                case FILTER_ADMIN:
                    raw = taiKhoanDAO.filterByVaiTro("Admin");
                    break;
                case FILTER_NHANVIEN:
                    // "Nhân viên" gộp: LeTan, KeToan, NhanVien
                    List<TaiKhoan> le = taiKhoanDAO.filterByVaiTro("LeTan");
                    List<TaiKhoan> ke = taiKhoanDAO.filterByVaiTro("KeToan");
                    List<TaiKhoan> nv = taiKhoanDAO.filterByVaiTro("NhanVien");
                    raw = new ArrayList<>();
                    raw.addAll(le); raw.addAll(ke); raw.addAll(nv);
                    break;
                case FILTER_KHOA: {
                    List<TaiKhoan> all = taiKhoanDAO.getAll();
                    raw = new ArrayList<>();
                    for (TaiKhoan tk : all) if ("Khoa".equals(tk.getTrangThai())) raw.add(tk);
                    break;
                }
                default:
                    raw = taiKhoanDAO.getAll();
            }

            // Lọc theo keyword nếu có
            final List<TaiKhoan> result;
            if (!currentKeyword.isEmpty()) {
                String kw = currentKeyword.toLowerCase();
                List<TaiKhoan> filtered = new ArrayList<>();
                for (TaiKhoan tk : raw) {
                    if ((tk.getTenDangNhap() != null && tk.getTenDangNhap().toLowerCase().contains(kw))
                            || (tk.getEmail() != null && tk.getEmail().toLowerCase().contains(kw))) {
                        filtered.add(tk);
                    }
                }
                result = filtered;
            } else {
                result = raw;
            }

            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.setData(result);
                boolean empty = result.isEmpty();
                if (emptyState != null) emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvList.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void toggleLock(TaiKhoan tk) {
        // Không cho phép khóa tài khoản đang đăng nhập
        if (tk.getMaTK() == session.getMaTK()) {
            Toast.makeText(requireContext(),
                    "Không thể khóa tài khoản đang đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }
        String newStatus = "HoatDong".equals(tk.getTrangThai()) ? "Khoa" : "HoatDong";
        executor.execute(() -> {
            taiKhoanDAO.updateTrangThai(tk.getMaTK(), newStatus);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                String msg = "Khoa".equals(newStatus) ? "Tài khoản đã bị khóa." : "Tài khoản đã được mở khóa.";
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                load();
            });
        });
    }

    private void confirmDelete(TaiKhoan tk) {
        if (tk.getMaTK() == session.getMaTK()) {
            Toast.makeText(requireContext(),
                    "Không thể xóa tài khoản đang đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((TextView) dialogView.findViewById(R.id.tv_dialog_title)).setText("Xóa tài khoản");
        ((TextView) dialogView.findViewById(R.id.tv_dialog_message))
                .setText("Xóa \"" + tk.getTenDangNhap() + "\"? Hành động không thể hoàn tác.");

        Dialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView).create();

        dialogView.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_dialog_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            executor.execute(() -> {
                taiKhoanDAO.delete(tk.getMaTK());
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Đã xóa tài khoản.", Toast.LENGTH_SHORT).show();
                    load();
                });
            });
        });
        dialog.show();
    }

    private void openDetail(int maTK) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, AccountDetailFragment.newInstance(maTK))
                .addToBackStack(null)
                .commit();
    }

    private void openAddEdit(int maTK) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, AccountAddEditFragment.newInstance(maTK))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
        applyPermission();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchWatcher != null)
            etSearch.removeTextChangedListener(searchWatcher);
    }

    @Override
    public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
