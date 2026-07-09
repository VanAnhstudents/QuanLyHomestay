package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.TienNghi;

import java.util.ArrayList;
import java.util.List;

public class PhongTienNghiDAO {
    private final DatabaseHelper dbHelper;

    public PhongTienNghiDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Lấy danh sách tiện nghi của một phòng.
     * @param maPhong ID phòng cần lấy tiện nghi.
     * @return Danh sách TienNghi thuộc phòng đó.
     */
    public List<TienNghi> getByPhong(int maPhong) {
        List<TienNghi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t.MaTienNghi, t.TenTienNghi " +
                "FROM TienNghi t " +
                "INNER JOIN Phong_TienNghi pt ON t.MaTienNghi = pt.MaTienNghi " +
                "WHERE pt.MaPhong = ? " +
                "ORDER BY t.MaTienNghi";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(maPhong)})) {
            while (c.moveToNext()) {
                TienNghi t = new TienNghi();
                t.setMaTienNghi(c.getInt(c.getColumnIndexOrThrow("MaTienNghi")));
                t.setTenTienNghi(c.getString(c.getColumnIndexOrThrow("TenTienNghi")));
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Xóa toàn bộ tiện nghi của phòng rồi insert lại theo danh sách mới.
     * Dùng khi lưu form Thêm/Sửa phòng.
     *
     * @param maPhong         ID phòng.
     * @param maTienNghiList  Danh sách MaTienNghi cần lưu (có thể rỗng).
     */
    public void replaceAll(int maPhong, List<Integer> maTienNghiList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Xóa tất cả tiện nghi cũ của phòng
            db.delete("Phong_TienNghi", "MaPhong = ?", new String[]{String.valueOf(maPhong)});

            // Insert lại từng tiện nghi mới
            for (int maTN : maTienNghiList) {
                ContentValues cv = new ContentValues();
                cv.put("MaPhong", maPhong);
                cv.put("MaTienNghi", maTN);
                db.insert("Phong_TienNghi", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
