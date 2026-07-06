package com.example.ql_homestay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.data.dao.ThongBaoDAO;
import com.example.ql_homestay.model.TaiKhoan;
import com.example.ql_homestay.model.ThongBao;
import com.example.ql_homestay.ui.auth.LoginActivity;
import com.example.ql_homestay.ui.booking.BookingListFragment;
import com.example.ql_homestay.ui.customer.CustomerListFragment;
import com.example.ql_homestay.ui.main.HomeFragment;
import com.example.ql_homestay.ui.room.RoomListFragment;
import com.example.ql_homestay.util.AvatarHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private TextView tvUserName;
    private TextView tvBadgeCount;
    private ImageView ivBell;

    private SessionManager session;
    private DatabaseHelper dbHelper;
    private ThongBaoDAO thongBaoDAO;

    private HomeFragment homeFragment;
    private CustomerListFragment customerFragment;
    private RoomListFragment roomFragment;
    private BookingListFragment bookingFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
        thongBaoDAO = new ThongBaoDAO(dbHelper);

        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav    = findViewById(R.id.bottom_navigation);
        tvUserName   = findViewById(R.id.tv_user_name);
        tvBadgeCount = findViewById(R.id.tv_badge_count);
        ivBell       = findViewById(R.id.iv_bell);

        setupAppBar();

        if (savedInstanceState == null) {
            initFragments();
        }

        setupBottomNav();
        setupBackHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && session.isLoggedIn() && thongBaoDAO != null) {
            refreshNotificationBadge();
        }
    }

    private void setupAppBar() {
        if (tvUserName != null) {
            String hoTen = session.getHoTen();
            tvUserName.setText(hoTen.isEmpty() ? session.getTenDangNhap() : hoTen);
        }
        
        // Load avatar từ database
        ImageView ivAvatar = findViewById(R.id.iv_avatar);
        if (ivAvatar != null) {
            TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO(dbHelper);
            TaiKhoan taiKhoan = taiKhoanDAO.findById(session.getMaTK());
            if (taiKhoan != null) {
                AvatarHelper.loadAvatarOnly(this, taiKhoan.getAvatar(), ivAvatar);
            }
        }
        
        if (ivBell != null) {
            ivBell.setOnClickListener(v -> showNotificationsDialog());
        }
        refreshNotificationBadge();
    }

    private void refreshNotificationBadge() {
        if (tvBadgeCount == null) return;

        int unread = thongBaoDAO.countUnread(session.getMaTK());
        if (unread <= 0) {
            tvBadgeCount.setVisibility(View.GONE);
            return;
        }

        tvBadgeCount.setText(unread > 99 ? "99+" : String.valueOf(unread));
        tvBadgeCount.setVisibility(View.VISIBLE);
    }

    private void showNotificationsDialog() {
        List<ThongBao> notifications = thongBaoDAO.getAllByTaiKhoan(session.getMaTK());
        if (notifications.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Không có thông báo.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        String[] items = new String[notifications.size()];
        for (int i = 0; i < notifications.size(); i++) {
            ThongBao tb = notifications.get(i);
            String prefix = tb.isDaDoc() ? "" : "Mới - ";
            String time = tb.getThoiGian() == null ? "" : "\n" + tb.getThoiGian();
            items[i] = prefix + tb.getNoiDung() + time;
        }

        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setItems(items, (dialog, which) -> {
                    thongBaoDAO.markAsRead(notifications.get(which).getMaTB());
                    refreshNotificationBadge();
                })
                .setPositiveButton("Đánh dấu đã đọc", (dialog, which) -> {
                    thongBaoDAO.markAllAsRead(session.getMaTK());
                    refreshNotificationBadge();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void initFragments() {
        homeFragment    = new HomeFragment();
        customerFragment = new CustomerListFragment();
        roomFragment    = new RoomListFragment();
        bookingFragment = new BookingListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment,     "home")
                .add(R.id.fragment_container, customerFragment, "customer").hide(customerFragment)
                .add(R.id.fragment_container, roomFragment,     "room").hide(roomFragment)
                .add(R.id.fragment_container, bookingFragment,  "booking").hide(bookingFragment)
                .commit();
        activeFragment = homeFragment;
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            }
            if (id == R.id.nav_room) {
                showFragment(roomFragment);
                return true;
            }
            if (id == R.id.nav_booking) {
                showFragment(bookingFragment);
                return true;
            }
            if (id == R.id.nav_customer) {
                showFragment(customerFragment);
                return true;
            }
            if (id == R.id.nav_more) {
                // Mở MoreBottomSheetFragment
                if (getSupportFragmentManager().findFragmentByTag("more_sheet") == null) {
                    MoreBottomSheetFragment.newInstance()
                            .show(getSupportFragmentManager(), "more_sheet");
                }
                return true;
            }

            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void showFragment(Fragment target) {
        if (target == activeFragment
                && getSupportFragmentManager().getBackStackEntryCount() == 0) return;
        // Pop tất cả back stack (Staff, Account, Detail...) trước khi chuyển tab
        getSupportFragmentManager().popBackStackImmediate(
                null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(activeFragment);
        ft.show(target);
        ft.commit();
        activeFragment = target;
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    moveTaskToBack(true);
                }
            }
        });
    }

    /**
     * Stub cho MoreBottomSheetFragment khi implement sau.
     * Giữ lại để không phải sửa MainActivity khi thêm sheet.
     */
    public void navigateToModule(String moduleKey) {
        Toast.makeText(this, "Module " + moduleKey + " đang phát triển", Toast.LENGTH_SHORT).show();
    }

    /**
     * Set bottom navigation selection programmatically
     */
    public void setBottomNavSelection(int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    public void logout() {
        session.logout();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Cập nhật tiêu đề hiển thị trên AppBar dùng chung.
     * Các Fragment detail/add-edit gọi method này trong onViewCreated.
     */
    public void setAppBarTitle(String title) {
        TextView tvTitle = findViewById(R.id.tv_app_title);
        if (tvTitle != null) tvTitle.setText(title != null ? title : "Lala House");
    }

    /** Khôi phục tiêu đề mặc định "Lala House" */
    public void resetAppBarTitle() {
        setAppBarTitle("Lala House");
    }

    public DatabaseHelper getDbHelper() { return dbHelper; }
    public SessionManager getSession()  { return session;  }
}
