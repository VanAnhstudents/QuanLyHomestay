package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.ChiTietPhuThu;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác CRUD bảng ChiTietPhuThu.
 * Mỗi bản ghi là một dòng phụ thu thuộc về một hóa đơn (MaHD).
 */
public class ChiTietPhuThuDAO {

    private final DatabaseHelper dbHelper;

    public ChiTietPhuThuDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ChiTietPhuThu mapCursor(Cursor c) {
        ChiTietPhuThu ct = new ChiTietPhuThu();
        ct.setMaChiTiet(c.getInt(c.getColumnIndexOrThrow("MaChiTiet")));
        ct.setMaHD(c.getInt(c.getColumnIndexOrThrow("MaHD")));
        ct.setTenPhuThu(c.getString(c.getColumnIndexOrThrow("TenPhuThu")));
        ct.setSoTien(c.getDouble(c.getColumnIndexOrThrow("SoTien")));
        return ct;
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    /**
     * Lấy danh sách tất cả dòng phụ thu của một hóa đơn.
     * @param maHD mã hóa đơn
     */
    public List<ChiTietPhuThu> getByHoaDon(int maHD) {
        List<ChiTietPhuThu> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM ChiTietPhuThu WHERE MaHD = ? ORDER BY MaChiTiet ASC",
                new String[]{String.valueOf(maHD)})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /**
     * Tính tổng phụ thu của một hóa đơn.
     * @param maHD mã hóa đơn
     */
    public double sumByHoaDon(int maHD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(SoTien), 0) FROM ChiTietPhuThu WHERE MaHD = ?",
                new String[]{String.valueOf(maHD)})) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    // ─── WRITE ────────────────────────────────────────────────────────────────

    /**
     * Thêm một dòng phụ thu.
     * @return rowId vừa insert, -1 nếu lỗi.
     */
    public long insert(ChiTietPhuThu ct) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("MaHD",      ct.getMaHD());
        cv.put("TenPhuThu", ct.getTenPhuThu());
        cv.put("SoTien",    ct.getSoTien());
        return db.insert("ChiTietPhuThu", null, cv);
    }

    /**
     * Xóa một dòng phụ thu theo MaChiTiet.
     * @return số dòng bị ảnh hưởng
     */
    public int delete(int maChiTiet) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("ChiTietPhuThu", "MaChiTiet = ?",
                new String[]{String.valueOf(maChiTiet)});
    }

    /**
     * Xóa toàn bộ dòng phụ thu của một hóa đơn (dùng trước khi insert lại).
     * @return số dòng bị ảnh hưởng
     */
    public int deleteByHoaDon(int maHD) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("ChiTietPhuThu", "MaHD = ?",
                new String[]{String.valueOf(maHD)});
    }
}
