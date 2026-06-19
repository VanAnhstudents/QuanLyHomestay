package com.example.ql_homestay.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.model.TaiKhoan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AuthRepository — tầng trung gian giữa UI và DAO cho nghiệp vụ Xác thực.
 * Trách nhiệm:
 *   · login() — xác thực tên đăng nhập + mật khẩu, trả về TaiKhoan hoặc null
 *   · register() — kiểm tra trùng username, tạo TaiKhoan mới với vai trò mặc định "NhanVien", trả về true/false
 *   · getHoTenByMaTK() — tra cứu HoTen từ bảng NhanVien để SessionManager lưu
 * Mật khẩu trong seed data ở dạng plain-text (demo offline).
 * Trong production: hash với BCrypt / Argon2 trước khi lưu, so sánh bằng thư viện hash tương ứng.
 */
public class AuthRepository {
    private final TaiKhoanDAO taiKhoanDAO;
    private final DatabaseHelper dbHelper;

    public AuthRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        taiKhoanDAO = new TaiKhoanDAO(dbHelper);
    }

    // Login()
    /**
     * Xác thực đăng nhập.
     * Logic:
     *   1. Tìm TaiKhoan theo TenDangNhap.
     *   2. Nếu không tìm thấy → null.
     *   3. So sánh MatKhau (plain-text, demo).
     *   4. Kiểm tra TrangThai == "HoatDong" (tài khoản không bị khóa).
     * @param username tên đăng nhập nhập từ UI
     * @param password mật khẩu nhập từ UI
     * @return TaiKhoan nếu thành công; null nếu sai thông tin hoặc bị khóa
     */
    public TaiKhoan login(String username, String password) {
        if (username == null || username.trim().isEmpty()) return null;
        if (password == null || password.isEmpty()) return null;

        TaiKhoan tk = taiKhoanDAO.findByUsername(username.trim());
        if (tk == null) return null;
        if (!tk.getMatKhau().equals(password)) return null;
        if (!tk.isActive()) return null;

        return tk;
    }

    // Register()
    /**
     * Tạo tài khoản mới.
     * Logic:
     *   1. Kiểm tra username chưa bị chiếm (isUsernameTaken).
     *   2. Build TaiKhoan với VaiTro = "NhanVien", TrangThai = "HoatDong".
     *   3. Gọi TaiKhoanDAO.insert().
     * @param tenDangNhap tên đăng nhập mới (đã validate ở UI: không rỗng)
     * @param email email (có thể null nếu không bắt buộc)
     * @param matKhau mật khẩu đã được UI kiểm tra độ dài tối thiểu
     * @return true nếu tạo thành công; false nếu username đã tồn tại hoặc lỗi DB
     */
    public boolean register(String tenDangNhap, String email, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) return false;
        if (matKhau == null || matKhau.isEmpty()) return false;

        String username = tenDangNhap.trim();
        if (taiKhoanDAO.isUsernameTaken(username)) return false;

        // Build model
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(username);
        tk.setEmail(email);
        tk.setMatKhau(matKhau);
        tk.setVaiTro("NhanVien");
        tk.setTrangThai("HoatDong");
        tk.setNgayTao(todayString());
        tk.setAvatar("avatar_admin");

        long rowId = taiKhoanDAO.insert(tk);
        return rowId != -1;
    }

    // Lấy HoTen từ bảng NhanVien để dùng trong SessionManager
    /**
     * Tra cứu HoTen của nhân viên liên kết với tài khoản maTK.
     * Nếu tài khoản không có hồ sơ NhanVien (ví dụ tài khoản admin tạo thủ công), trả về tenDangNhap làm fallback.
     * @param maTK khóa tài khoản vừa đăng nhập
     * @param tenDangNhap fallback nếu không có NhanVien
     * @return Họ tên để hiển thị trong greeting
     */
    public String getHoTenByMaTK(int maTK, String tenDangNhap) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT HoTen FROM NhanVien WHERE MaTK = ? LIMIT 1",
                new String[]{String.valueOf(maTK)})) {

            if (c.moveToFirst()) {
                String hoTen = c.getString(0);
                if (hoTen != null && !hoTen.trim().isEmpty()) return hoTen.trim();
            }
        }
        return tenDangNhap;
    }

    // Private helpers
    private String todayString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }
}