package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.LoaiPhong;

import java.util.ArrayList;
import java.util.List;

public class LoaiPhongDAO {
    private final DatabaseHelper dbHelper;

    public LoaiPhongDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Lấy tất cả loại phòng – dùng để populate Spinner trong form thêm phòng.
    public List<LoaiPhong> getAll() {
        List<LoaiPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT MaLoaiPhong, TenLoai, GiaCoBan FROM LoaiPhong ORDER BY MaLoaiPhong",
                null)) {
            while (c.moveToNext()) {
                LoaiPhong lp = new LoaiPhong();
                lp.setMaLoaiPhong(c.getInt(c.getColumnIndexOrThrow("MaLoaiPhong")));
                lp.setTenLoai(c.getString(c.getColumnIndexOrThrow("TenLoai")));
                lp.setGiaCoBan(c.getDouble(c.getColumnIndexOrThrow("GiaCoBan")));
                list.add(lp);
            }
        }
        return list;
    }

    // Tìm LoaiPhong theo MaLoaiPhong. Trả null nếu không tìm thấy.
    public LoaiPhong findById(int maLoaiPhong) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT MaLoaiPhong, TenLoai, GiaCoBan FROM LoaiPhong WHERE MaLoaiPhong = ?",
                new String[]{String.valueOf(maLoaiPhong)})) {
            if (c.moveToFirst()) {
                LoaiPhong lp = new LoaiPhong();
                lp.setMaLoaiPhong(c.getInt(0));
                lp.setTenLoai(c.getString(1));
                lp.setGiaCoBan(c.getDouble(2));
                return lp;
            }
        }
        return null;
    }
}
