package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.ThongBao;

import java.util.ArrayList;
import java.util.List;

/**
 * ThongBaoDAO — Data Access Object cho bảng ThongBao.
 * Các nghiệp vụ chính:
 *   - Đếm số thông báo chưa đọc (badge chuông App Bar)
 *   - Lấy danh sách thông báo của một tài khoản
 *   - Đánh dấu đã đọc (DaDoc = 1)
 */
public class ThongBaoDAO {

    private final DatabaseHelper dbHelper;

    public ThongBaoDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Read
    /**
     * Đếm số thông báo CHƯA ĐỌC của tài khoản maTK.
     * Dùng cho badge số đỏ trên icon chuông ở App Bar.
     * @param maTK khóa tài khoản đang đăng nhập
     * @return số thông báo chưa đọc (≥ 0)
     */
    public int countUnread(int maTK) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Dùng rawQuery với COUNT(*) để tránh tải toàn bộ hàng về bộ nhớ.
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM ThongBao WHERE MaTK = ? AND DaDoc = 0",
                new String[]{String.valueOf(maTK)})) {

            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /**
     * Lấy tất cả thông báo của maTK, sắp xếp mới nhất trước.
     * @param maTK khóa tài khoản
     * @return danh sách ThongBao (có thể rỗng, không null)
     */
    public List<ThongBao> getAllByTaiKhoan(int maTK) {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                "ThongBao",
                null,
                "MaTK = ?",
                new String[]{String.valueOf(maTK)},
                null, null,
                "ThoiGian DESC")) {

            while (c.moveToNext()) {
                list.add(cursorToModel(c));
            }
        }
        return list;
    }

    /**
     * Lấy N thông báo gần nhất (chưa đọc + đã đọc) của maTK.
     * Dùng cho dropdown thông báo nhỏ trên App Bar.
     * @param maTK  khóa tài khoản
     * @param limit số lượng tối đa cần lấy
     * @return danh sách ThongBao (có thể rỗng)
     */
    public List<ThongBao> getRecent(int maTK, int limit) {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(
                "ThongBao",
                null,
                "MaTK = ?",
                new String[]{String.valueOf(maTK)},
                null, null,
                "ThoiGian DESC",
                String.valueOf(limit))) {

            while (c.moveToNext()) {
                list.add(cursorToModel(c));
            }
        }
        return list;
    }

    // Write
    /**
     * Chèn thông báo mới.
     * @param tb đối tượng ThongBao (maTB bỏ qua — AUTOINCREMENT)
     * @return rowId mới (≥ 1); -1 nếu thất bại
     */
    public long insert(ThongBao tb) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("MaTK", tb.getMaTK());
        cv.put("NoiDung", tb.getNoiDung());
        cv.put("DaDoc", tb.isDaDoc() ? 1 : 0);
        cv.put("ThoiGian", tb.getThoiGian());
        return db.insert("ThongBao", null, cv);
    }

    /**
     * Đánh dấu một thông báo đã đọc.
     * @param maTB khóa thông báo
     * @return số hàng bị ảnh hưởng
     */
    public int markAsRead(int maTB) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("DaDoc", 1);
        return db.update("ThongBao", cv,
                "MaTB = ?", new String[]{String.valueOf(maTB)});
    }

    /**
     * Đánh dấu TẤT CẢ thông báo của tài khoản là đã đọc.
     * Gọi khi người dùng mở màn hình Thông báo.
     * @param maTK khóa tài khoản
     * @return số hàng bị ảnh hưởng
     */
    public int markAllAsRead(int maTK) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("DaDoc", 1);
        return db.update("ThongBao", cv,
                "MaTK = ? AND DaDoc = 0", new String[]{String.valueOf(maTK)});
    }

    /**
     * Xóa thông báo theo khóa chính.
     * @param maTB khóa thông báo
     * @return số hàng bị ảnh hưởng
     */
    public int delete(int maTB) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("ThongBao", "MaTB = ?",
                new String[]{String.valueOf(maTB)});
    }

    // Helper private
    private ThongBao cursorToModel(Cursor c) {
        ThongBao tb = new ThongBao();
        tb.setMaTB( c.getInt( c.getColumnIndexOrThrow("MaTB")));
        tb.setMaTK( c.getInt( c.getColumnIndexOrThrow("MaTK")));
        tb.setNoiDung( c.getString(c.getColumnIndexOrThrow("NoiDung")));
        tb.setDaDoc( c.getInt( c.getColumnIndexOrThrow("DaDoc")) == 1);
        tb.setThoiGian(c.getString(c.getColumnIndexOrThrow("ThoiGian")));
        return tb;
    }
}