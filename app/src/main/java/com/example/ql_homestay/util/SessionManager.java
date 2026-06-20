package com.example.ql_homestay.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — lưu trữ thông tin phiên đăng nhập vào SharedPreferences.
 * Dữ liệu được persisted qua các lần khởi động lại app:
 *   - MaTK, TenDangNhap, HoTen, VaiTro, Avatar
 *   - isLoggedIn (boolean guard)
 * Cách dùng:
 *   SessionManager sm = SessionManager.getInstance(context);
 *   sm.login(taiKhoan, hoTen);        // sau khi xác thực thành công
 *   sm.getMaTK()                       // lấy MaTK của user đang đăng nhập
 *   sm.logout()                        // xóa toàn bộ session
 */
public class SessionManager {
    private static final String PREF_NAME = "LalaHouseSession";
    private static final String KEY_LOGGED = "isLoggedIn";
    private static final String KEY_MA_TK = "maTK";
    private static final String KEY_USERNAME = "tenDangNhap";
    private static final String KEY_HO_TEN = "hoTen";
    private static final String KEY_VAI_TRO = "vaiTro";
    private static final String KEY_AVATAR = "avatar";

    // ─── Singleton ──────────────────────────────────────────────────────────

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        prefs  = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // ─── Login / Logout ─────────────────────────────────────────────────────

    /**
     * Lưu phiên đăng nhập.
     * Gọi ngay sau khi {@code AuthRepository.login()} trả về kết quả thành công.
     *
     * @param maTK khóa tài khoản
     * @param tenDangNhap tên đăng nhập
     * @param hoTen họ tên hiển thị (lấy từ bảng NhanVien hoặc fallback = tenDangNhap)
     * @param vaiTro "Admin" | "LeTan" | "KeToan" | "NhanVien"
     * @param avatar tên resource drawable avatar (nullable)
     */
    public void login(int maTK, String tenDangNhap, String hoTen,
                      String vaiTro, String avatar) {
        editor.putBoolean(KEY_LOGGED, true);
        editor.putInt(KEY_MA_TK, maTK);
        editor.putString(KEY_USERNAME, tenDangNhap);
        editor.putString(KEY_HO_TEN, hoTen != null ? hoTen : tenDangNhap);
        editor.putString(KEY_VAI_TRO, vaiTro != null ? vaiTro : "NhanVien");
        editor.putString(KEY_AVATAR, avatar != null ? avatar : "avatar_admin");
        editor.apply();
    }

    /**
     * Xóa toàn bộ session (đăng xuất).
     */
    public void logout() {
        editor.clear().apply();
    }

    // ─── Getters ────────────────────────────────────────────────────────────

    /** Kiểm tra người dùng đã đăng nhập chưa. */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED, false);
    }

    /** MaTK của tài khoản đang đăng nhập. -1 nếu chưa đăng nhập. */
    public int getMaTK() {
        return prefs.getInt(KEY_MA_TK, -1);
    }

    /** TenDangNhap. */
    public String getTenDangNhap() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /** Họ tên hiển thị (lấy từ bảng NhanVien, không phải TenDangNhap). */
    public String getHoTen() {
        return prefs.getString(KEY_HO_TEN, "");
    }

    /** Vai trò của tài khoản hiện tại. */
    public String getVaiTro() {
        return prefs.getString(KEY_VAI_TRO, "NhanVien");
    }

    /** Tên resource drawable của avatar. */
    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, "avatar_admin");
    }

    // ─── Role helpers ────────────────────────────────────────────────────────

    public boolean isAdmin()   { return "Admin".equals(getVaiTro()); }
    public boolean isLeTan()   { return "LeTan".equals(getVaiTro()); }
    public boolean isKeToan()  { return "KeToan".equals(getVaiTro()); }
    public boolean isNhanVien(){ return "NhanVien".equals(getVaiTro()); }
}