package com.example.ql_homestay;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.ui.auth.LoginActivity;
import com.example.ql_homestay.ui.booking.BookingListFragment;
import com.example.ql_homestay.ui.customer.CustomerListFragment;
import com.example.ql_homestay.ui.main.home.HomeFragment;
import com.example.ql_homestay.ui.room.RoomListFragment;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity — Activity chính sau khi đăng nhập.
 * Chức năng:
 * 1. Kiểm tra phiên đăng nhập — redirect về LoginActivity nếu chưa đăng nhập.
 * 2. Khởi tạo AppBar (tên user, badge thông báo).
 * 3. Điều hướng Fragment theo tab BottomNavigationView.
 * 4. Tab "Hơn nữa" → mở MoreBottomSheet (Thanh toán / Nhân viên / Thống kê / Tài khoản)
 *    với các item bị ẩn theo role (RBAC runtime).
 * 5. Back stack: nhấn Back từ Fragment con → pop stack, nếu stack rỗng → minimizeApp.
 * Theo lo_trinh.md Bước 8 — Bước nền tảng chung.
 */
public class MainActivity extends AppCompatActivity {

    // Views
    private BottomNavigationView bottomNav;
    private TextView tvUserName;
    private TextView tvBadgeCount;

    // Utilities
    private SessionManager session;
    private DatabaseHelper dbHelper;

    // Fragment cache (tránh recreate mỗi lần chuyển tab)
    private HomeFragment homeFragment;
    private RoomListFragment roomFragment;
    private BookingListFragment bookingFragment;
    private CustomerListFragment customerFragment;

    // Fragment đang hiển thị
    private Fragment activeFragment;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo session & DB
        session = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);

        // 2. Kiểm tra đăng nhập
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // 3. Bind views
        bottomNav = findViewById(R.id.bottom_navigation);
        tvUserName = findViewById(R.id.tv_user_name);
        tvBadgeCount = findViewById(R.id.tv_badge_count);

        // 4. Cập nhật AppBar
        setupAppBar();

        // 5. Khởi tạo Fragment lần đầu
        if (savedInstanceState == null) {
            initFragments();
        }

        // 6. Lắng nghe tab BottomNav
        setupBottomNav();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AppBar setup
    // ─────────────────────────────────────────────────────────────────────────

    private void setupAppBar() {
        if (tvUserName != null) {
            String hoTen = session.getHoTen();
            tvUserName.setText(hoTen.isEmpty() ? session.getTenDangNhap() : hoTen);
        }
        refreshNotificationBadge();
    }

    /**
     * Cập nhật badge số thông báo chưa đọc trên icon chuông.
     * Gọi lại từ Fragment con khi có thông báo mới (qua ((MainActivity) getActivity()).refreshNotificationBadge()).
     */
    public void refreshNotificationBadge() {
        if (tvBadgeCount == null) return;
        // TODO: thay bằng ThongBaoDAO.countUnread(maTK) khi DAO sẵn sàng
        int unread = 0;
        if (unread > 0) {
            tvBadgeCount.setText(unread > 99 ? "99+" : String.valueOf(unread));
            tvBadgeCount.setVisibility(View.VISIBLE);
        } else {
            tvBadgeCount.setVisibility(View.GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fragment init & navigation
    // ─────────────────────────────────────────────────────────────────────────

    private void initFragments() {
        homeFragment = new HomeFragment();
        roomFragment = new RoomListFragment();
        bookingFragment = new BookingListFragment();
        customerFragment = new CustomerListFragment();

        // Thêm tất cả Fragment vào back-stack nhưng ẩn hết, chỉ show Home
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .add(R.id.fragment_container, roomFragment, "room")
                .add(R.id.fragment_container, bookingFragment, "booking")
                .add(R.id.fragment_container, customerFragment, "customer")
                .hide(roomFragment)
                .hide(bookingFragment)
                .hide(customerFragment)
                .commit();

        activeFragment = homeFragment;
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_room) {
                showFragment(roomFragment);
                return true;
            } else if (id == R.id.nav_booking) {
                showFragment(bookingFragment);
                return true;
            } else if (id == R.id.nav_customer) {
                showFragment(customerFragment);
                return true;
            } else if (id == R.id.nav_more) {
                openMoreMenu();
                return true;
            }
            return false;
        });

        // Chọn Home làm tab mặc định
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    /**
     * Chuyển đổi Fragment bằng hide/show (tránh recreate, giữ state).
     */
    private void showFragment(Fragment target) {
        if (target == activeFragment) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(activeFragment);
        ft.show(target);
        ft.commit();
        activeFragment = target;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu "Hơn nữa" — mở BottomSheet với các module phụ
    // ─────────────────────────────────────────────────────────────────────────

    private void openMoreMenu() {
        // Tránh mở nhiều lần
        if (getSupportFragmentManager().findFragmentByTag("more_sheet") != null) return;

        MoreBottomSheetFragment sheet = MoreBottomSheetFragment.newInstance();
        sheet.show(getSupportFragmentManager(), "more_sheet");
    }

    /**
     * Được gọi từ MoreBottomSheetFragment khi user chọn 1 module phụ.
     * Sẽ mở Fragment tương ứng (Payment, Staff, Statistics, Account).
     */
    public void navigateToModule(String moduleKey) {
        // Dismiss bottom sheet nếu còn đang hiện
        Fragment sheet = getSupportFragmentManager().findFragmentByTag("more_sheet");
        if (sheet != null) {
            getSupportFragmentManager().beginTransaction().remove(sheet).commit();
        }

        Fragment target = null;
        switch (moduleKey) {
            case "payment":
                target = new com.example.ql_homestay.ui.payment.InvoiceListFragment();
                break;
            case "staff":
                target = new com.example.ql_homestay.ui.staff.StaffListFragment();
                break;
            case "statistics":
                target = new com.example.ql_homestay.ui.statistics.StatisticsDashboardFragment();
                break;
            case "account":
                target = new com.example.ql_homestay.ui.account.AccountListFragment();
                break;
        }

        if (target != null) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .add(R.id.fragment_container, target, moduleKey)
                    .addToBackStack(moduleKey)
                    .commit();
            activeFragment = target;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Back stack handling
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            // Pop fragment từ "Hơn nữa" → khôi phục activeFragment
            getSupportFragmentManager().popBackStack();
            // Khôi phục activeFragment về Home sau khi pop
            activeFragment = homeFragment;
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            // Không còn back stack → minimize app (không thoát hẳn)
            moveTaskToBack(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Đăng xuất
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gọi từ màn hình Profile hoặc menu đăng xuất.
     */
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

    // ─────────────────────────────────────────────────────────────────────────
    // Getter (cho Fragment con dùng)
    // ─────────────────────────────────────────────────────────────────────────

    public DatabaseHelper getDbHelper() { return dbHelper; }
    public SessionManager getSession() { return session; }
}
