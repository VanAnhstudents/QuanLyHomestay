package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.CheckInOut;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DAO thao tác bảng CheckInOut.
 */
public class CheckInOutDAO {
    private final DatabaseHelper dbHelper;

    public CheckInOutDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // HELPER
    private CheckInOut mapCursor(Cursor c) {
        CheckInOut log = new CheckInOut();
        log.setMaCheckLog(c.getInt(c.getColumnIndexOrThrow("MaCheckLog")));
        log.setMaDatPhong(c.getInt(c.getColumnIndexOrThrow("MaDatPhong")));
        int maNVIdx = c.getColumnIndex("MaNV");
        if (maNVIdx != -1 && !c.isNull(maNVIdx)) log.setMaNV(c.getInt(maNVIdx));
        log.setLoai(c.getString(c.getColumnIndexOrThrow("Loai")));
        log.setThoiGian(c.getString(c.getColumnIndexOrThrow("ThoiGian")));
        int ghiChuIdx = c.getColumnIndex("GhiChuDacBiet");
        if (ghiChuIdx != -1) log.setGhiChuDacBiet(c.getString(ghiChuIdx));
        int tenNVIdx = c.getColumnIndex("TenNhanVien");
        if (tenNVIdx != -1) log.setTenNhanVien(c.getString(tenNVIdx));
        int tenPhongIdx = c.getColumnIndex("TenPhong");
        if (tenPhongIdx != -1) log.setTenPhong(c.getString(tenPhongIdx));
        int tenKHIdx = c.getColumnIndex("TenKhachHang");
        if (tenKHIdx != -1) log.setTenKhachHang(c.getString(tenKHIdx));
        return log;
    }

    // WRITE
    /**
     * Ghi log Check-in hoặc Check-out. ThoiGian tự lấy thời điểm hiện tại.
     *
     * @param maDatPhong ID đặt phòng.
     * @param maNV ID nhân viên đang thực hiện (0 nếu không xác định).
     * @param loai "CheckIn" hoặc "CheckOut".
     * @return rowId bản ghi vừa insert, -1 nếu lỗi.
     */
    public long insertLog(int maDatPhong, int maNV, String loai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        ContentValues cv = new ContentValues();
        cv.put("MaDatPhong", maDatPhong);
        if (maNV > 0) cv.put("MaNV", maNV);
        cv.put("Loai",       loai);
        cv.put("ThoiGian",   now);
        return db.insert("CheckInOut", null, cv);
    }

    // READ
    /** Lấy tất cả log của một đặt phòng, mới nhất trên đầu. */
    public List<CheckInOut> getByDatPhong(int maDatPhong) {
        List<CheckInOut> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT c.*, nv.HoTen AS TenNhanVien " +
                "FROM CheckInOut c " +
                "LEFT JOIN NhanVien nv ON c.MaNV = nv.MaNV " +
                "WHERE c.MaDatPhong = ? " +
                "ORDER BY c.ThoiGian DESC";
        try (Cursor cur = db.rawQuery(sql, new String[]{String.valueOf(maDatPhong)})) {
            while (cur.moveToNext()) list.add(mapCursor(cur));
        }
        return list;
    }

    /**
     * Lấy {@code limit} hoạt động gần đây nhất – dùng cho widget "Hoạt động gần đây" ở Home.
     * JOIN thêm TenPhong và TenKhachHang để hiển thị trực tiếp.
     */
    public List<CheckInOut> getRecent(int limit) {
        List<CheckInOut> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT c.MaCheckLog, c.MaDatPhong, c.MaNV, c.Loai, c.ThoiGian, " +
                "nv.HoTen AS TenNhanVien, " +
                "p.TenPhong, " +
                "k.HoTen AS TenKhachHang " +
                "FROM CheckInOut c " +
                "LEFT JOIN NhanVien nv ON c.MaNV = nv.MaNV " +
                "LEFT JOIN DatPhong dp ON c.MaDatPhong = dp.MaDatPhong " +
                "LEFT JOIN Phong p ON dp.MaPhong = p.MaPhong " +
                "LEFT JOIN KhachHang k ON dp.MaKH = k.MaKH " +
                "ORDER BY c.ThoiGian DESC " +
                "LIMIT ?";
        try (Cursor cur = db.rawQuery(sql, new String[]{String.valueOf(limit)})) {
            while (cur.moveToNext()) list.add(mapCursor(cur));
        }
        return list;
    }
}
