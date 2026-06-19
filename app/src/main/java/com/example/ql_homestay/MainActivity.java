package com.example.ql_homestay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.ui.auth.LoginActivity;
import com.example.ql_homestay.ui.main.HomeFragment;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private TextView tvUserName;
    private TextView tvBadgeCount;

    private SessionManager session;
    private DatabaseHelper dbHelper;

    // Giai đoạn này chỉ dùng HomeFragment.
    // Các fragment khác sẽ được thêm vào khi từng thành viên hoàn thành module.
    private HomeFragment homeFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);

        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav    = findViewById(R.id.bottom_navigation);
        tvUserName   = findViewById(R.id.tv_user_name);
        tvBadgeCount = findViewById(R.id.tv_badge_count);

        setupAppBar();

        if (savedInstanceState == null) {
            initFragments();
        }

        setupBottomNav();
    }

    private void setupAppBar() {
        if (tvUserName != null) {
            String hoTen = session.getHoTen();
            tvUserName.setText(hoTen.isEmpty() ? session.getTenDangNhap() : hoTen);
        }
        if (tvBadgeCount != null) {
            tvBadgeCount.setVisibility(View.GONE); // TODO: ThongBaoDAO.countUnread()
        }
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
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

            // Các tab Room / Booking / Customer / More:
            // hiện Toast cho đến khi từng thành viên implement xong fragment tương ứng
            // và thêm vào initFragments() + case này.
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void showFragment(Fragment target) {
        if (target == activeFragment) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(activeFragment);
        ft.show(target);
        ft.commit();
        activeFragment = target;
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
            activeFragment = homeFragment;
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            moveTaskToBack(true);
        }
    }

    /**
     * Stub cho MoreBottomSheetFragment khi implement sau.
     * Giữ lại để không phải sửa MainActivity khi thêm sheet.
     */
    public void navigateToModule(String moduleKey) {
        Toast.makeText(this, "Module " + moduleKey + " đang phát triển", Toast.LENGTH_SHORT).show();
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

    public DatabaseHelper getDbHelper() { return dbHelper; }
    public SessionManager getSession()  { return session;  }
}