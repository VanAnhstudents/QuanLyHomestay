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

        btnLuu.setOnClickListener(v -> saveRole());
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
        if (tv != null) tv.setText("Trang ch\u1ee7 \u2192 T\u00e0i kho\u1ea3n \u2192 Chi ti\u1ebft");
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
                tvEmail.setText(tk.getEmail() != null ? tk.getEmail() : "-");
                bindRoleText(tk.getVaiTro());
                tvNgayTao.setText(tk.getNgayTao() != null ? tk.getNgayTao() : "-");
                bindLockState(tk);

                selectedVaiTro = tk.getVaiTro() != null ? tk.getVaiTro() : "NhanVien";
                updateTabUI(selectedVaiTro);
                loadPermissions(selectedVaiTro);
            });
        });
    }

    private void bindLockState(TaiKhoan tk) {
        boolean active = "HoatDong".equals(tk.getTrangThai());
        tvBadgeTrangThai.setText(active ? "Ho\u1ea1t \u0111\u1ed9ng" : "\u0110\u00e3 kh\u00f3a");
        tvBadgeTrangThai.setBackgroundResource(active ? R.drawable.bg_badge_dathanhtoan : R.drawable.bg_badge_dahuy);
        btnToggleLock.setText(active ? "Kh\u00f3a" : "M\u1edf kh\u00f3a");
        btnToggleLock.setEnabled(tk.getMaTK() != session.getMaTK());
    }

    private void toggleLock() {
        if (currentAccount == null) return;
        if (currentAccount.getMaTK() == session.getMaTK()) {
            Toast.makeText(requireContext(), "Kh\u00f4ng th\u1ec3 kh\u00f3a t\u00e0i kho\u1ea3n \u0111ang \u0111\u0103ng nh\u1eadp.", Toast.LENGTH_SHORT).show();
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
                            "HoatDong".equals(nextStatus) ? "\u0110\u00e3 m\u1edf kh\u00f3a t\u00e0i kho\u1ea3n." : "\u0110\u00e3 kh\u00f3a t\u00e0i kho\u1ea3n.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    btnToggleLock.setEnabled(true);
                    Toast.makeText(requireContext(), "C\u1eadp nh\u1eadt tr\u1ea1ng th\u00e1i th\u1ea5t b\u1ea1i.", Toast.LENGTH_SHORT).show();
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
                rvPermissions.scrollToPosition(0);
            });
        });
    }

    private void saveRole() {
        if (currentAccount == null) return;
        btnLuu.setEnabled(false);
        executor.execute(() -> {
            int rows = taiKhoanDAO.updateVaiTro(currentAccount.getMaTK(), selectedVaiTro);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                btnLuu.setEnabled(true);
                if (rows > 0) {
                    currentAccount.setVaiTro(selectedVaiTro);
                    bindRoleText(selectedVaiTro);
                    if (currentAccount.getMaTK() == session.getMaTK()) {
                        session.login(currentAccount.getMaTK(), currentAccount.getTenDangNhap(), session.getHoTen(), selectedVaiTro, currentAccount.getAvatar());
                    }
                    Toast.makeText(requireContext(), "Vai tr\u00f2 t\u00e0i kho\u1ea3n \u0111\u00e3 \u0111\u01b0\u1ee3c l\u01b0u.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "C\u1eadp nh\u1eadt vai tr\u00f2 th\u1ea5t b\u1ea1i.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void bindRoleText(String vaiTro) {
        tvCurrentRole.setText("\u0110ang c\u00f3 vai tr\u00f2 " + roleLabel(vaiTro) + " (" + roleShortLabel(vaiTro) + ")");
    }

    private static String roleLabel(String vaiTro) {
        if ("Admin".equals(vaiTro)) return "Admin";
        if ("LeTan".equals(vaiTro)) return "L\u1ec5 t\u00e2n";
        if ("KeToan".equals(vaiTro)) return "K\u1ebf to\u00e1n";
        if ("NhanVien".equals(vaiTro)) return "Nh\u00e2n vi\u00ean";
        return vaiTro != null ? vaiTro : "Nh\u00e2n vi\u00ean";
    }

    private static String roleShortLabel(String vaiTro) {
        if ("LeTan".equals(vaiTro)) return "L\u1ec5 t\u00e2n";
        if ("KeToan".equals(vaiTro)) return "K\u1ebf to\u00e1n";
        if ("NhanVien".equals(vaiTro)) return "NV";
        return "Admin";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
