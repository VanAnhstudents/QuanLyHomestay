package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

/**
 * TaiKhoanDAO — Data Access Object cho bảng TaiKhoan.
 * Quy ước dùng trong project:
 *   - Mọi truy vấn đọc dùng try-with-resources (Cursor) để tránh leak.
 *   - Không dùng raw SQL string nối tham số — luôn dùng selectionArgs[].
 *   - Không throw RuntimeException ra ngoài; lỗi trả qua return value (null / -1 / false) để tầng Repository xử lý.
 */
public class TaiKhoanDAO {
    private final DatabaseHelper dbHelper;

    public TaiKhoanDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // READ
    /**
     * Tìm tài khoản theo username (case-sensitive, theo đúng dữ liệu seed).
     * @param username giá trị TenDangNhap cần tìm
     * @return TaiKhoan nếu tồn tại; null nếu không tìm thấy
     */
    public TaiKhoan findByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                "TaiKhoan",
                null,                            // tất cả cột
                "TenDangNhap = ?",
                new String[]{username},
                null, null, null, "1")) {        // LIMIT 1

            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    /**
     * Tìm tài khoản theo khóa chính MaTK.
     * @param maTK khóa chính
     * @return TaiKhoan nếu tồn tại; null nếu không tìm thấy
     */
    public TaiKhoan findById(int maTK) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                "TaiKhoan",
                null,
                "MaTK = ?",
                new String[]{String.valueOf(maTK)},
                null, null, null, "1")) {

            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    /**
     * Kiểm tra username đã tồn tại trong DB chưa (dùng khi đăng ký).
     * @param username tên đăng nhập cần kiểm tra
     * @return true nếu đã bị chiếm; false nếu còn trống
     */
    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                "TaiKhoan",
                new String[]{"MaTK"},
                "TenDangNhap = ?",
                new String[]{username},
                null, null, null, "1")) {

            return c.moveToFirst();
        }
    }

    /**
     * Lấy toàn bộ danh sách tài khoản (dùng cho AccountListFragment).
     */
    public List<TaiKhoan> getAll() {
        List<TaiKhoan> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("TaiKhoan", null, null, null, null, null, "NgayTao DESC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    /**
     * Lọc tài khoản theo vai trò.
     * @param vaiTro "Admin" | "LeTan" | "KeToan" | "NhanVien"
     */
    public List<TaiKhoan> filterByVaiTro(String vaiTro) {
        List<TaiKhoan> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("TaiKhoan", null,
                "VaiTro = ?", new String[]{vaiTro}, null, null, "NgayTao DESC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    /**
     * Khoá / Mở khoá tài khoản.
     * @param maTK  tài khoản cần đổi trạng thái
     * @param trangThai "HoatDong" | "Khoa"
     * @return số hàng bị ảnh hưởng (1 = thành công)
     */
    public int updateTrangThai(int maTK, String trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TrangThai", trangThai);
        return db.update("TaiKhoan", cv, "MaTK = ?", new String[]{String.valueOf(maTK)});
    }

    // WRITE
    /**
     * Chèn một TaiKhoan mới vào DB.
     * MaTK được tự tăng (AUTOINCREMENT) nên không cần set trước.
     * @param tk đối tượng cần lưu (bỏ qua trường maTK)
     * @return rowId của bản ghi mới (≥ 1); -1 nếu thất bại
     */
    public long insert(TaiKhoan tk) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = buildContentValues(tk);
        return db.insert("TaiKhoan", null, cv);
    }

    /**
     * Cập nhật thông tin tài khoản (theo MaTK).
     * @param tk đối tượng đã sửa, phải có maTK hợp lệ
     * @return số hàng bị ảnh hưởng (1 = thành công; 0 = không tìm thấy)
     */
    public int update(TaiKhoan tk) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = buildContentValues(tk);
        return db.update("TaiKhoan", cv,
                "MaTK = ?", new String[]{String.valueOf(tk.getMaTK())});
    }

    /**
     * Xóa tài khoản theo MaTK.
     * Bảng con ThongBao sẽ tự xóa theo (ON DELETE CASCADE trong schema).
     * @param maTK khóa chính cần xóa
     * @return số hàng bị ảnh hưởng
     */
    public int delete(int maTK) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("TaiKhoan", "MaTK = ?",
                new String[]{String.valueOf(maTK)});
    }

    // HELPER
    /**
     * Ánh xạ hàng Cursor hiện tại → TaiKhoan POJO.
     * Cursor phải đang ở vị trí hợp lệ (moveToFirst/moveToNext đã trả true).
     */
    private TaiKhoan cursorToModel(Cursor c) {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK( c.getInt( c.getColumnIndexOrThrow("MaTK")));
        tk.setTenDangNhap( c.getString(c.getColumnIndexOrThrow("TenDangNhap")));
        tk.setEmail( c.getString(c.getColumnIndexOrThrow("Email")));
        tk.setMatKhau( c.getString(c.getColumnIndexOrThrow("MatKhau")));
        tk.setVaiTro( c.getString(c.getColumnIndexOrThrow("VaiTro")));
        tk.setTrangThai( c.getString(c.getColumnIndexOrThrow("TrangThai")));
        tk.setNgayTao( c.getString(c.getColumnIndexOrThrow("NgayTao")));
        tk.setAvatar( c.getString(c.getColumnIndexOrThrow("Avatar")));
        return tk;
    }

    /**
     * Build ContentValues từ TaiKhoan POJO.
     * Không đưa MaTK vào (AUTOINCREMENT); dùng chung cho insert() và update().
     * Trường hợp update(): caller tự thêm WHERE MaTK = ?.
     */
    private ContentValues buildContentValues(TaiKhoan tk) {
        ContentValues cv = new ContentValues();
        cv.put("TenDangNhap", tk.getTenDangNhap());
        if (tk.getEmail() != null) cv.put("Email",tk.getEmail());
        cv.put("MatKhau", tk.getMatKhau());
        cv.put("VaiTro", tk.getVaiTro());
        cv.put("TrangThai", tk.getTrangThai() != null ? tk.getTrangThai() : "HoatDong");
        cv.put("NgayTao", tk.getNgayTao());
        if (tk.getAvatar() != null) cv.put("Avatar", tk.getAvatar());
        return cv;
    }
}