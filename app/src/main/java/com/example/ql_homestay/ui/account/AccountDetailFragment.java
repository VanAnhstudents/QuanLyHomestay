package com.example.ql_homestay.ui.account;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.adapter.PermissionAdapter;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.model.TaiKhoan;
import com.example.ql_homestay.repository.PermissionRepository;
import com.example.ql_homestay.util.AvatarHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountDetailFragment extends Fragment {
    private static final String ARG_MA_TK = "maTK";

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
    private SessionManager session;
    private TaiKhoan currentAccount;

    private ImageView ivAvatar;
    private TextView tvInitials, tvTenTK, tvEmail, tvCurrentRole, tvBadgeTrangThai, tvNgayTao;
    private TextView tabAdmin, tabLeTan, tabKeToan, tabNhanVien;
    private RecyclerView rvPermissions;
    private MaterialButton btnLuu, btnToggleLock;

    private PermissionAdapter permissionAdapter;
    private String selectedVaiTro = "NhanVien";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        taiKhoanDAO = new TaiKhoanDAO(dbHelper);
        permissionRepo = new PermissionRepository(requireContext());
        session = SessionManager.getInstance(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        setupTabs();
        setupRecyclerView();

        btnLuu.setOnClickListener(v -> savePermissions());
        btnToggleLock.setOnClickListener(v -> toggleLock());
        loadAccountInfo();
    }

    private void bindViews(View v) {
        ivAvatar = v.findViewById(R.id.iv_avatar_detail);
        tvInitials = v.findViewById(R.id.tv_initials_detail);
        tvTenTK = v.findViewById(R.id.tv_ten_tk_detail);
        tvEmail = v.findViewById(R.id.tv_email_detail);
        tvCurrentRole = v.findViewById(R.id.tv_current_role);
        tvBadgeTrangThai = v.findViewById(R.id.tv_badge_trang_thai);
        tvNgayTao = v.findViewById(R.id.tv_ngay_tao_detail);
        tabAdmin = v.findViewById(R.id.tab_admin);
        tabLeTan = v.findViewById(R.id.tab_letan);
        tabKeToan = v.findViewById(R.id.tab_ketoan);
        tabNhanVien = v.findViewById(R.id.tab_nhanvien);
        rvPermissions = v.findViewById(R.id.rv_permissions);
        btnLuu = v.findViewById(R.id.btn_luu);
        btnToggleLock = v.findViewById(R.id.btn_toggle_lock);
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Tài khoản → Chi tiết");
    }

    private void setupTabs() {
        View.OnClickListener tabClick = v -> {
            if (v.getId() == R.id.tab_admin) selectedVaiTro = "Admin";
            else if (v.getId() == R.id.tab_letan) selectedVaiTro = "LeTan";
            else if (v.getId() == R.id.tab_ketoan) selectedVaiTro = "KeToan";
            else selectedVaiTro = "NhanVien";
            updateTabUI(selectedVaiTro);
            loadPermissions(selectedVaiTro);
        };
        tabAdmin.setOnClickListener(tabClick);
        tabLeTan.setOnClickListener(tabClick);
        tabKeToan.setOnClickListener(tabClick);
        tabNhanVien.setOnClickListener(tabClick);
    }

    private void setupRecyclerView() {
        permissionAdapter = new PermissionAdapter();
        rvPermissions.setLayoutManager(new LinearLayoutManager(requireContext()) {
            @Override public boolean canScrollVertically() { return false; }
        });
        rvPermissions.setAdapter(permissionAdapter);
        rvPermissions.setNestedScrollingEnabled(false);
    }

    private void loadAccountInfo() {
        executor.execute(() -> {
            TaiKhoan tk = taiKhoanDAO.findById(maTK);
            mainHandler.post(() -> {
                if (!isAdded() || tk == null) return;
                currentAccount = tk;
                AvatarHelper.loadAvatar(requireContext(), tk.getAvatar(), tk.getTenDangNhap(), ivAvatar, tvInitials);
                tvTenTK.setText(tk.getTenDangNhap());
                tvEmail.setText(tk.getEmail() != null ? tk.getEmail() : "—");
                tvCurrentRole.setText("Bạn đang có quyền " + roleLabel(tk.getVaiTro()) + " (" + roleShortLabel(tk.getVaiTro()) + ")");
                tvNgayTao.setText(tk.getNgayTao() != null ? tk.getNgayTao() : "—");
                bindLockState(tk);

                selectedVaiTro = tk.getVaiTro() != null ? tk.getVaiTro() : "NhanVien";
                updateTabUI(selectedVaiTro);
                loadPermissions(selectedVaiTro);
            });
        });
    }

    private void bindLockState(TaiKhoan tk) {
        boolean active = "HoatDong".equals(tk.getTrangThai());
        tvBadgeTrangThai.setText(active ? "Hoạt động" : "Đã khóa");
        tvBadgeTrangThai.setBackgroundResource(active ? R.drawable.bg_badge_dathanhtoan : R.drawable.bg_badge_dahuy);
        btnToggleLock.setText(active ? "Khóa" : "Mở khóa");
        btnToggleLock.setEnabled(tk.getMaTK() != session.getMaTK());
    }

    private void toggleLock() {
        if (currentAccount == null) return;
        if (currentAccount.getMaTK() == session.getMaTK()) {
            Toast.makeText(requireContext(), "Không thể khóa tài khoản đang đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }
        String nextStatus = "HoatDong".equals(currentAccount.getTrangThai()) ? "Khoa" : "HoatDong";
        btnToggleLock.setEnabled(false);
        executor.execute(() -> {
            int rows = taiKhoanDAO.updateTrangThai(currentAccount.getMaTK(), nextStatus);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (rows > 0) {
                    currentAccount.setTrangThai(nextStatus);
                    bindLockState(currentAccount);
                    Toast.makeText(requireContext(),
                            "HoatDong".equals(nextStatus) ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    btnToggleLock.setEnabled(true);
                    Toast.makeText(requireContext(), "Cập nhật trạng thái thất bại.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateTabUI(String vaiTro) {
        setTab(tabAdmin, "Admin".equals(vaiTro));
        setTab(tabLeTan, "LeTan".equals(vaiTro));
        setTab(tabKeToan, "KeToan".equals(vaiTro));
        setTab(tabNhanVien, "NhanVien".equals(vaiTro));
    }

    private void setTab(TextView tab, boolean active) {
        tab.setBackgroundColor(ContextCompat.getColor(requireContext(), active ? R.color.primary_main : R.color.background_card));
        tab.setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.text_on_primary : R.color.text_primary));
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
                ViewGroup.LayoutParams params = rvPermissions.getLayoutParams();
                params.height = rows.size() * itemHeight;
                rvPermissions.setLayoutParams(params);
            });
        });
    }

    private void savePermissions() {
        if ("Admin".equals(selectedVaiTro)) {
            Toast.makeText(requireContext(), "Vai trò Admin luôn có toàn quyền, không thể thay đổi.", Toast.LENGTH_SHORT).show();
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

    private static String roleLabel(String vaiTro) {
        if ("Admin".equals(vaiTro)) return "Admin";
        if ("LeTan".equals(vaiTro)) return "Lễ tân";
        if ("KeToan".equals(vaiTro)) return "Kế toán";
        if ("NhanVien".equals(vaiTro)) return "Nhân viên";
        return vaiTro != null ? vaiTro : "Nhân viên";
    }

    private static String roleShortLabel(String vaiTro) {
        if ("LeTan".equals(vaiTro)) return "Lễ tân";
        if ("KeToan".equals(vaiTro)) return "Kế toán";
        if ("NhanVien".equals(vaiTro)) return "NV";
        return "Admin";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
