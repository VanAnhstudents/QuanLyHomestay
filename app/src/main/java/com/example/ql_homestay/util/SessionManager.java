package com.example.ql_homestay.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — quản lý phiên đăng nhập của người dùng.
 * Lưu trữ thông tin phiên vào SharedPreferences (private, không thể đọc bởi app khác).
 * Cung cấp các phương thức: login(), logout(), isLoggedIn(), getMaTK(), getVaiTro(),
 * getTenDangNhap(), getHoTen().
 */
public class SessionManager {

    private static final String PREF_NAME = "LalaHouseSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_MA_TK = "maTK";
    private static final String KEY_TEN_DANG_NHAP = "tenDangNhap";
    private static final String KEY_HO_TEN = "hoTen";
    private static final String KEY_VAI_TRO = "vaiTro";
    private static final String KEY_MA_NV = "maNV";   // -1 nếu không phải nhân viên
    private static final String KEY_REMEMBER_USER = "rememberUsername";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    // Singleton
    private static SessionManager instance;

    private SessionManager(Context context) {
        pref   = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Lấy instance singleton. Luôn truyền Application context để tránh leak.
     */
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // Đăng nhập
    /**
     * Lưu thông tin phiên khi đăng nhập thành công.
     * @param maTK         Khóa chính tài khoản (TaiKhoan.MaTK)
     * @param tenDangNhap  Tên đăng nhập
     * @param hoTen        Họ tên hiển thị (lấy từ NhanVien.HoTen hoặc "Admin")
     * @param vaiTro       Giá trị VaiTro: "Admin" | "LeTan" | "KeToan" | "NhanVien"
     * @param maNV         Mã nhân viên (NhanVien.MaNV), truyền -1 nếu là Admin thuần
     */
    public void login(int maTK, String tenDangNhap, String hoTen, String vaiTro, int maNV) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_MA_TK, maTK);
        editor.putString(KEY_TEN_DANG_NHAP, tenDangNhap);
        editor.putString(KEY_HO_TEN, hoTen != null ? hoTen : tenDangNhap);
        editor.putString(KEY_VAI_TRO, vaiTro != null ? vaiTro : "NhanVien");
        editor.putInt(KEY_MA_NV, maNV);
        editor.apply();
    }

    // Đăng xuất
    /**
     * Xóa toàn bộ dữ liệu phiên (trừ "remember username" nếu user đã tick).
     */
    public void logout() {
        String remembered = pref.getString(KEY_REMEMBER_USER, null);
        editor.clear();
        if (remembered != null) {
            editor.putString(KEY_REMEMBER_USER, remembered);
        }
        editor.apply();
    }

    // Kiểm tra trạng thái
    /** Trả về true nếu đang có phiên đăng nhập hợp lệ. */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Getter thông tin phiên
    /** Mã tài khoản hiện tại. Trả về -1 nếu chưa đăng nhập. */
    public int getMaTK() {
        return pref.getInt(KEY_MA_TK, -1);
    }

    /** Tên đăng nhập hiện tại. */
    public String getTenDangNhap() {
        return pref.getString(KEY_TEN_DANG_NHAP, "");
    }

    /** Họ tên hiển thị. */
    public String getHoTen() {
        return pref.getString(KEY_HO_TEN, "");
    }

    /**
     * Vai trò của tài khoản hiện tại.
     * Các giá trị hợp lệ: "Admin", "LeTan", "KeToan", "NhanVien"
     * Trả về chuỗi rỗng nếu chưa đăng nhập.
     */
    public String getVaiTro() {
        return pref.getString(KEY_VAI_TRO, "");
    }

    /** Mã nhân viên liên kết. Trả về -1 nếu là Admin không có bản ghi NhanVien. */
    public int getMaNV() {
        return pref.getInt(KEY_MA_NV, -1);
    }

    // "Ghi nhớ đăng nhập"
    /**
     * Lưu username để điền sẵn lần đăng nhập tiếp theo
     * (chỉ gọi khi user tick CheckBox "Ghi nhớ").
     */
    public void saveRememberedUsername(String username) {
        editor.putString(KEY_REMEMBER_USER, username);
        editor.apply();
    }

    /**
     * Lấy username đã được ghi nhớ. Trả về null nếu chưa có.
     */
    public String getRememberedUsername() {
        return pref.getString(KEY_REMEMBER_USER, null);
    }

    /**
     * Xóa username đã ghi nhớ (khi user bỏ tick CheckBox).
     */
    public void clearRememberedUsername() {
        editor.remove(KEY_REMEMBER_USER);
        editor.apply();
    }

    // Tiện ích kiểm tra vai trò

    public boolean isAdmin() {
        return "Admin".equals(getVaiTro());
    }

    public boolean isLeTan() {
        return "LeTan".equals(getVaiTro());
    }

    public boolean isKeToan() {
        return "KeToan".equals(getVaiTro());
    }

    public boolean isNhanVien() {
        return "NhanVien".equals(getVaiTro());
    }
}
