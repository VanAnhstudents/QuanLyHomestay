package com.example.ql_homestay.ui.account;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.ql_homestay.adapter.PermissionAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.model.TaiKhoan;
import com.example.ql_homestay.repository.PermissionRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chi tiết / Phân quyền tài khoản.
 * - 4 tab thủ công để chọn vai trò xem/sửa quyền.
 * - RecyclerView 8 module với dropdown quyền (PermissionAdapter).
 * - Tab Admin: disable tất cả dropdown.
 * - Nút "Lưu thay đổi" → cập nhật PhanQuyen_VaiTro.
 */
public class AccountDetailFragment extends Fragment {
    private static final String ARG_MA_TK = "maTK";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    // Vai trò mapping: tab → string DB
    private static final String[] TAB_VAITRO = {"Admin", "LeTan", "KeToan", "NhanVien"};

    public static AccountDetailFragment newInstance(int maTK) {
        AccountDetailFragment f = new AccountDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_TK, maTK);
        f.setArguments(args);
        return f;
    }

    private int maTK = -1;
    private TaiKhoanDAO taiKhoanDAO;
    private PermissionRepository permissionRepo;
    private DatabaseHelper dbHelper;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private TextView tvInitials, tvTenTK, tvEmail, tvBadgeTrangThai, tvNgayTao;
    private TextView tabAdmin, tabLeTan, tabKeToan, tabNhanVien;
    private RecyclerView rvPermissions;
    private MaterialButton btnLuu;

    private PermissionAdapter permissionAdapter;
    private String selectedVaiTro = "Admin"; // default tab

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maTK = getArguments().getInt(ARG_MA_TK, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        taiKhoanDAO = new TaiKhoanDAO(dbHelper);
        permissionRepo = new PermissionRepository(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        setupTabs();
        setupRecyclerView();

        btnLuu.setOnClickListener(v -> savePermissions());
        loadAccountInfo();
        loadPermissions("Admin"); // default tab Admin
    }

    private void bindViews(View v) {
        tvInitials = v.findViewById(R.id.tv_initials_detail);
        tvTenTK = v.findViewById(R.id.tv_ten_tk_detail);
        tvEmail = v.findViewById(R.id.tv_email_detail);
        tvBadgeTrangThai = v.findViewById(R.id.tv_badge_trang_thai);
        tvNgayTao = v.findViewById(R.id.tv_ngay_tao_detail);
        tabAdmin = v.findViewById(R.id.tab_admin);
        tabLeTan = v.findViewById(R.id.tab_letan);
        tabKeToan = v.findViewById(R.id.tab_ketoan);
        tabNhanVien = v.findViewById(R.id.tab_nhanvien);
        rvPermissions = v.findViewById(R.id.rv_permissions);
        btnLuu = v.findViewById(R.id.btn_luu);
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Tài khoản → Chi tiết");
    }

    private void setupTabs() {
        View.OnClickListener tabClick = v -> {
            String vaiTro;
            if (v.getId() == R.id.tab_admin) vaiTro = "Admin";
            else if (v.getId() == R.id.tab_letan) vaiTro = "LeTan";
            else if (v.getId() == R.id.tab_ketoan) vaiTro = "KeToan";
            else vaiTro = "NhanVien";
            selectedVaiTro = vaiTro;
            updateTabUI(vaiTro);
            loadPermissions(vaiTro);
        };
        tabAdmin.setOnClickListener(tabClick);
        tabLeTan.setOnClickListener(tabClick);
        tabKeToan.setOnClickListener(tabClick);
        tabNhanVien.setOnClickListener(tabClick);
    }

    private void updateTabUI(String vaiTro) {
        int active = R.color.primary_main;
        int inactive = R.color.background_card;
        int textOn = R.color.text_on_primary;
        int textOff = R.color.text_primary;

        tabAdmin.setBackgroundResource("Admin".equals(vaiTro) ? android.R.color.transparent : android.R.color.transparent);
        tabAdmin.setBackgroundColor(requireContext().getResources().getColor("Admin".equals(vaiTro) ? active : inactive));
        tabAdmin.setTextColor(requireContext().getResources().getColor("Admin".equals(vaiTro) ? textOn : textOff));

        tabLeTan.setBackgroundColor(requireContext().getResources().getColor("LeTan".equals(vaiTro) ? active : inactive));
        tabLeTan.setTextColor(requireContext().getResources().getColor("LeTan".equals(vaiTro) ? textOn : textOff));

        tabKeToan.setBackgroundColor(requireContext().getResources().getColor("KeToan".equals(vaiTro) ? active : inactive));
        tabKeToan.setTextColor(requireContext().getResources().getColor("KeToan".equals(vaiTro) ? textOn : textOff));

        tabNhanVien.setBackgroundColor(requireContext().getResources().getColor("NhanVien".equals(vaiTro) ? active : inactive));
        tabNhanVien.setTextColor(requireContext().getResources().getColor("NhanVien".equals(vaiTro) ? textOn : textOff));
    }

    private void setupRecyclerView() {
        permissionAdapter = new PermissionAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        rvPermissions.setLayoutManager(layoutManager);
        rvPermissions.setAdapter(permissionAdapter);
        rvPermissions.setNestedScrollingEnabled(false);
        rvPermissions.setHasFixedSize(false);
    }

    private void loadAccountInfo() {
        executor.execute(() -> {
            TaiKhoan tk = taiKhoanDAO.findById(maTK);
            mainHandler.post(() -> {
                if (!isAdded() || tk == null) return;
                String initials = tk.getTenDangNhap() != null && !tk.getTenDangNhap().isEmpty()
                        ? tk.getTenDangNhap().substring(0, 1).toUpperCase() : "?";
                tvInitials.setText(initials);
                tvTenTK.setText(tk.getTenDangNhap());
                tvEmail.setText(tk.getEmail() != null ? tk.getEmail() : "—");

                boolean active = "HoatDong".equals(tk.getTrangThai());
                tvBadgeTrangThai.setText(active ? "Hoạt động" : "Đã khóa");
                tvBadgeTrangThai.setBackgroundResource(
                        active ? R.drawable.bg_badge_dathanhtoan : R.drawable.bg_badge_dahuy);

                tvNgayTao.setText(tk.getNgayTao() != null ? tk.getNgayTao() : "—");
            });
        });
    }

    private void loadPermissions(String vaiTro) {
        executor.execute(() -> {
            List<PermissionRepository.PermissionRow> rows = permissionRepo.getPermissionMatrix(vaiTro);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                permissionAdapter.setData(rows);
                permissionAdapter.setReadOnly("Admin".equals(vaiTro));
                rvPermissions.scrollToPosition(0);
                
                int itemHeight = (int) (72 * getResources().getDisplayMetrics().density);
                int totalHeight = rows.size() * itemHeight;
                android.view.ViewGroup.LayoutParams params = rvPermissions.getLayoutParams();
                params.height = totalHeight;
                rvPermissions.setLayoutParams(params);
            });
        });
    }

    private void savePermissions() {
        if ("Admin".equals(selectedVaiTro)) {
            Toast.makeText(requireContext(),
                    "Vai trò Admin luôn có toàn quyền, không thể thay đổi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Map<Integer, String> changes = permissionAdapter.getCurrentPermissions();
        btnLuu.setEnabled(false);
        executor.execute(() -> {
            permissionRepo.savePermissions(selectedVaiTro, changes);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                btnLuu.setEnabled(true);
                Toast.makeText(requireContext(), "Phân quyền đã được lưu.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
