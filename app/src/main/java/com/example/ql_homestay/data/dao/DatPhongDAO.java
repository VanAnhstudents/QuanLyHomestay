package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.DatPhong;

import java.util.ArrayList;
import java.util.List;

public class DatPhongDAO {
    private final DatabaseHelper dbHelper;

    public DatPhongDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** JOIN DatPhong + Phong, lấy tối đa {@code limit} đặt phòng gần nhất của khách. */
    public List<DatPhong> getRecentByKhachHang(int maKH, int limit) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT dp.MaDatPhong, dp.MaKH, dp.MaPhong, dp.NgayCheckIn, dp.NgayCheckOut, " +
                "dp.SoDem, dp.TrangThai, p.TenPhong " +
                "FROM DatPhong dp " +
                "LEFT JOIN Phong p ON dp.MaPhong = p.MaPhong " +
                "WHERE dp.MaKH = ? " +
                "ORDER BY dp.NgayCheckIn DESC " +
                "LIMIT ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(maKH), String.valueOf(limit)})) {
            while (c.moveToNext()) {
                DatPhong dp = new DatPhong();
                dp.setMaDatPhong(c.getInt(c.getColumnIndexOrThrow("MaDatPhong")));
                dp.setMaKH(c.getInt(c.getColumnIndexOrThrow("MaKH")));
                dp.setMaPhong(c.getInt(c.getColumnIndexOrThrow("MaPhong")));
                dp.setNgayCheckIn(c.getString(c.getColumnIndexOrThrow("NgayCheckIn")));
                dp.setNgayCheckOut(c.getString(c.getColumnIndexOrThrow("NgayCheckOut")));
                dp.setSoDem(c.getInt(c.getColumnIndexOrThrow("SoDem")));
                dp.setTrangThai(c.getString(c.getColumnIndexOrThrow("TrangThai")));
                dp.setTenPhong(c.getString(c.getColumnIndexOrThrow("TenPhong")));
                list.add(dp);
            }
        }
        return list;
    }
}
