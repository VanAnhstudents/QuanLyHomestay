package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.DatPhong;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác CRUD bảng DatPhong.
 * Tất cả truy vấn JOIN thêm TenPhong từ Phong và TenKhachHang từ KhachHang.
 */
public class DatPhongDAO {
    private final DatabaseHelper dbHelper;

    public DatPhongDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // -------------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------------
    private DatPhong mapCursor(Cursor c) {
        DatPhong dp = new DatPhong();
        dp.setMaDatPhong(c.getInt(c.getColumnIndexOrThrow("MaDatPhong")));
        dp.setMaKH(c.getInt(c.getColumnIndexOrThrow("MaKH")));
        dp.setMaPhong(c.getInt(c.getColumnIndexOrThrow("MaPhong")));
        dp.setNgayCheckIn(c.getString(c.getColumnIndexOrThrow("NgayCheckIn")));
        dp.setNgayCheckOut(c.getString(c.getColumnIndexOrThrow("NgayCheckOut")));
        dp.setSoDem(c.getInt(c.getColumnIndexOrThrow("SoDem")));
        dp.setTrangThai(c.getString(c.getColumnIndexOrThrow("TrangThai")));
        int tenPhongIdx = c.getColumnIndex("TenPhong");
        if (tenPhongIdx != -1) dp.setTenPhong(c.getString(tenPhongIdx));
        int tenKHIdx = c.getColumnIndex("TenKhachHang");
        if (tenKHIdx != -1) dp.setTenKhachHang(c.getString(tenKHIdx));
        int phuongThucIdx = c.getColumnIndex("PhuongThucThanhToan");
        if (phuongThucIdx != -1) dp.setPhuongThucThanhToan(c.getString(phuongThucIdx));
        int ghiChuIdx = c.getColumnIndex("GhiChu");
        if (ghiChuIdx != -1) dp.setGhiChu(c.getString(ghiChuIdx));
        int soKhachIdx = c.getColumnIndex("SoLuongKhach");
        if (soKhachIdx != -1) dp.setSoLuongKhach(c.getInt(soKhachIdx));
        int maNVIdx = c.getColumnIndex("MaNV");
        if (maNVIdx != -1) dp.setMaNV(c.getInt(maNVIdx));
        int ngayTaoIdx = c.getColumnIndex("NgayTao");
        if (ngayTaoIdx != -1) dp.setNgayTao(c.getString(ngayTaoIdx));
        return dp;
    }

    private static final String SQL_SELECT_JOIN =
            "SELECT dp.*, p.TenPhong, k.HoTen AS TenKhachHang " +
            "FROM DatPhong dp " +
            "LEFT JOIN Phong p ON dp.MaPhong = p.MaPhong " +
            "LEFT JOIN KhachHang k ON dp.MaKH = k.MaKH ";

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    /** Lấy tất cả đặt phòng, mới nhất trên đầu. */
    public List<DatPhong> getAll() {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "ORDER BY dp.NgayCheckIn DESC", null)) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Lọc theo TrangThai ("SapDen" | "DangO" | "DaTraPhong" | "DaHuy"). */
    public List<DatPhong> filterByTrangThai(String trangThai) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE dp.TrangThai = ? ORDER BY dp.NgayCheckIn DESC",
                new String[]{trangThai})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Tìm kiếm theo tên khách hoặc mã đặt phòng. */
    public List<DatPhong> search(String keyword) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SQL_SELECT_JOIN +
                "WHERE k.HoTen LIKE ? OR CAST(dp.MaDatPhong AS TEXT) LIKE ? " +
                "ORDER BY dp.NgayCheckIn DESC";
        String like = "%" + keyword + "%";
        try (Cursor c = db.rawQuery(sql, new String[]{like, like})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Tìm đặt phòng theo MaDatPhong. */
    public DatPhong findById(int maDatPhong) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE dp.MaDatPhong = ?",
                new String[]{String.valueOf(maDatPhong)})) {
            if (c.moveToFirst()) return mapCursor(c);
        }
        return null;
    }

    /** Lấy lịch sử đặt phòng của một phòng cụ thể. */
    public List<DatPhong> getByPhong(int maPhong) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE dp.MaPhong = ? ORDER BY dp.NgayCheckIn DESC",
                new String[]{String.valueOf(maPhong)})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Lấy lịch sử đặt phòng của một khách hàng. */
    public List<DatPhong> getByKhachHang(int maKH) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE dp.MaKH = ? ORDER BY dp.NgayCheckIn DESC",
                new String[]{String.valueOf(maKH)})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
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

    /** Lấy đặt phòng trong ngày (NgayCheckIn = today hoặc TrangThai = DangO). */
    public List<DatPhong> getTodayAndActive(String today) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SQL_SELECT_JOIN +
                "WHERE dp.NgayCheckIn = ? OR dp.TrangThai = 'DangO' " +
                "ORDER BY dp.NgayCheckIn";
        try (Cursor c = db.rawQuery(sql, new String[]{today})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // WRITE
    // -------------------------------------------------------------------------

    /**
     * Thêm đặt phòng mới.
     * @return rowId của bản ghi vừa insert, -1 nếu lỗi.
     */
    public long insert(DatPhong dp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(dp);
        return db.insert("DatPhong", null, cv);
    }

    /**
     * Cập nhật đặt phòng.
     * @return số dòng bị ảnh hưởng.
     */
    public int update(DatPhong dp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(dp);
        return db.update("DatPhong", cv, "MaDatPhong = ?",
                new String[]{String.valueOf(dp.getMaDatPhong())});
    }

    /**
     * Cập nhật trạng thái đặt phòng.
     * @param trangThai "SapDen" | "DangO" | "DaTraPhong" | "DaHuy"
     * @return số dòng bị ảnh hưởng.
     */
    public int updateTrangThai(int maDatPhong, String trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TrangThai", trangThai);
        return db.update("DatPhong", cv, "MaDatPhong = ?",
                new String[]{String.valueOf(maDatPhong)});
    }

    /**
     * Xóa đặt phòng theo MaDatPhong.
     * @return số dòng bị ảnh hưởng.
     */
    public int delete(int maDatPhong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("DatPhong", "MaDatPhong = ?",
                new String[]{String.valueOf(maDatPhong)});
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------
    private ContentValues toContentValues(DatPhong dp) {
        ContentValues cv = new ContentValues();
        cv.put("MaKH",                  dp.getMaKH());
        cv.put("MaPhong",               dp.getMaPhong());
        cv.put("MaNV",                  dp.getMaNV());
        cv.put("NgayCheckIn",           dp.getNgayCheckIn());
        cv.put("NgayCheckOut",          dp.getNgayCheckOut());
        cv.put("SoLuongKhach",          dp.getSoLuongKhach());
        cv.put("SoDem",                 dp.getSoDem());
        cv.put("TrangThai",             dp.getTrangThai());
        cv.put("PhuongThucThanhToan",   dp.getPhuongThucThanhToan());
        cv.put("GhiChu",                dp.getGhiChu());
        cv.put("NgayTao",               dp.getNgayTao());
        return cv;
    }
}
