package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.KhachHang;

import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {
    private final DatabaseHelper dbHelper;

    public KhachHangDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<KhachHang> getAll() {
        List<KhachHang> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("KhachHang", null, null, null, null, null, "HoTen ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    /** Tìm theo HoTen / SDT / CCCD (LIKE, không phân biệt vị trí khớp). */
    public List<KhachHang> search(String keyword) {
        List<KhachHang> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String like = "%" + keyword + "%";
        try (Cursor c = db.query("KhachHang", null,
                "HoTen LIKE ? OR SDT LIKE ? OR CCCD LIKE ?",
                new String[]{like, like, like},
                null, null, "HoTen ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public KhachHang findById(int maKH) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("KhachHang", null,
                "MaKH = ?", new String[]{String.valueOf(maKH)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    public long insert(KhachHang kh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert("KhachHang", null, buildContentValues(kh));
    }

    public int update(KhachHang kh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update("KhachHang", buildUpdateValues(kh),
                "MaKH = ?", new String[]{String.valueOf(kh.getMaKH())});
    }

    public int delete(int maKH) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.delete("KhachHang", "MaKH = ?", new String[]{String.valueOf(maKH)});
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            return -1; // bị chặn bởi FK — khách hàng đã có DatPhong liên quan
        }
    }

    /** Gọi khi 1 DatPhong của khách chuyển sang DaTraPhong. */
    public void incrementSoLanThue(int maKH) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE KhachHang SET SoLanThue = SoLanThue + 1 WHERE MaKH = ?",
                new Object[]{maKH});
    }

    private KhachHang cursorToModel(Cursor c) {
        KhachHang kh = new KhachHang();
        kh.setMaKH(c.getInt(c.getColumnIndexOrThrow("MaKH")));
        kh.setHoTen(c.getString(c.getColumnIndexOrThrow("HoTen")));
        kh.setSdt(c.getString(c.getColumnIndexOrThrow("SDT")));
        kh.setEmail(c.getString(c.getColumnIndexOrThrow("Email")));
        kh.setCccd(c.getString(c.getColumnIndexOrThrow("CCCD")));
        kh.setDiaChi(c.getString(c.getColumnIndexOrThrow("DiaChi")));
        kh.setNgaySinh(c.getString(c.getColumnIndexOrThrow("NgaySinh")));
        kh.setGioiTinh(c.getString(c.getColumnIndexOrThrow("GioiTinh")));
        kh.setAvatar(c.getString(c.getColumnIndexOrThrow("Avatar")));
        kh.setSoLanThue(c.getInt(c.getColumnIndexOrThrow("SoLanThue")));
        return kh;
    }

    private ContentValues buildContentValues(KhachHang kh) {
        ContentValues cv = buildUpdateValues(kh);
        cv.put("SoLanThue", kh.getSoLanThue());
        return cv;
    }

    /** For UPDATE: excludes SoLanThue so rental count is never overwritten by the edit form. */
    private ContentValues buildUpdateValues(KhachHang kh) {
        ContentValues cv = new ContentValues();
        cv.put("HoTen", kh.getHoTen());
        cv.put("SDT", kh.getSdt());
        cv.put("Email", kh.getEmail());
        cv.put("CCCD", kh.getCccd());
        cv.put("DiaChi", kh.getDiaChi());
        cv.put("NgaySinh", kh.getNgaySinh());
        cv.put("GioiTinh", kh.getGioiTinh());
        cv.put("Avatar", kh.getAvatar());
        return cv;
    }
}