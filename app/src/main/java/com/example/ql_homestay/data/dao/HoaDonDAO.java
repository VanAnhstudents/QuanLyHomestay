package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.HoaDon;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác CRUD bảng HoaDon.
 * Tất cả query JOIN thêm TenPhong, TenKhachHang, NgayCheckIn, NgayCheckOut, SoDem.
 */
public class HoaDonDAO {

    private final DatabaseHelper dbHelper;

    public HoaDonDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * SQL JOIN chuẩn: HoaDon ← DatPhong ← Phong, KhachHang, NhanVien
     */
    private static final String SQL_SELECT_JOIN =
            "SELECT hd.*, " +
            "  dp.NgayCheckIn, dp.NgayCheckOut, dp.SoDem, " +
            "  p.TenPhong, " +
            "  k.HoTen AS TenKhachHang, " +
            "  nv.HoTen AS TenNhanVien " +
            "FROM HoaDon hd " +
            "LEFT JOIN DatPhong dp ON hd.MaDatPhong = dp.MaDatPhong " +
            "LEFT JOIN Phong p    ON dp.MaPhong = p.MaPhong " +
            "LEFT JOIN KhachHang k ON dp.MaKH   = k.MaKH " +
            "LEFT JOIN NhanVien nv ON hd.MaNV    = nv.MaNV ";

    private HoaDon mapCursor(Cursor c) {
        HoaDon hd = new HoaDon();
        hd.setMaHD(c.getInt(c.getColumnIndexOrThrow("MaHD")));
        hd.setNgayLap(c.getString(c.getColumnIndexOrThrow("NgayLap")));
        hd.setTienPhong(c.getDouble(c.getColumnIndexOrThrow("TienPhong")));
        hd.setPhuThuDichVu(c.getDouble(c.getColumnIndexOrThrow("PhuThuDichVu")));
        hd.setGiamGia(c.getDouble(c.getColumnIndexOrThrow("GiamGia")));
        hd.setTongCong(c.getDouble(c.getColumnIndexOrThrow("TongCong")));
        hd.setTrangThai(c.getString(c.getColumnIndexOrThrow("TrangThai")));

        int maDatPhongIdx = c.getColumnIndex("MaDatPhong");
        if (maDatPhongIdx != -1 && !c.isNull(maDatPhongIdx))
            hd.setMaDatPhong(c.getInt(maDatPhongIdx));

        int phuongThucIdx = c.getColumnIndex("PhuongThucTT");
        if (phuongThucIdx != -1) hd.setPhuongThucTT(c.getString(phuongThucIdx));

        int ngayTTIdx = c.getColumnIndex("NgayTT");
        if (ngayTTIdx != -1) hd.setNgayTT(c.getString(ngayTTIdx));

        int maNVIdx = c.getColumnIndex("MaNV");
        if (maNVIdx != -1 && !c.isNull(maNVIdx)) hd.setMaNV(c.getInt(maNVIdx));

        // Joined fields
        int tenPhongIdx = c.getColumnIndex("TenPhong");
        if (tenPhongIdx != -1) hd.setTenPhong(c.getString(tenPhongIdx));

        int tenKHIdx = c.getColumnIndex("TenKhachHang");
        if (tenKHIdx != -1) hd.setTenKhachHang(c.getString(tenKHIdx));

        int ciIdx = c.getColumnIndex("NgayCheckIn");
        if (ciIdx != -1) hd.setNgayCheckIn(c.getString(ciIdx));

        int coIdx = c.getColumnIndex("NgayCheckOut");
        if (coIdx != -1) hd.setNgayCheckOut(c.getString(coIdx));

        int soDemIdx = c.getColumnIndex("SoDem");
        if (soDemIdx != -1) hd.setSoDem(c.getInt(soDemIdx));

        int tenNVIdx = c.getColumnIndex("TenNhanVien");
        if (tenNVIdx != -1) hd.setTenNhanVien(c.getString(tenNVIdx));

        return hd;
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    /** Lấy tất cả hóa đơn, mới nhất trước. */
    public List<HoaDon> getAll() {
        List<HoaDon> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(SQL_SELECT_JOIN + "ORDER BY hd.NgayLap DESC", null)) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /**
     * Lọc theo trạng thái.
     * @param trangThai "DaThanhToan" | "ChuaThanhToan" | "HoanTien"
     */
    public List<HoaDon> filterByTrangThai(String trangThai) {
        List<HoaDon> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE hd.TrangThai = ? ORDER BY hd.NgayLap DESC",
                new String[]{trangThai})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Tìm hóa đơn theo MaHD. */
    public HoaDon findById(int maHD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE hd.MaHD = ?",
                new String[]{String.valueOf(maHD)})) {
            if (c.moveToFirst()) return mapCursor(c);
        }
        return null;
    }

    /** Tìm hóa đơn theo MaDatPhong (1 đặt phòng chỉ có tối đa 1 hóa đơn). */
    public HoaDon findByDatPhong(int maDatPhong) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE hd.MaDatPhong = ?",
                new String[]{String.valueOf(maDatPhong)})) {
            if (c.moveToFirst()) return mapCursor(c);
        }
        return null;
    }

    /**
     * Tổng doanh thu trong một ngày (chỉ tính HĐ DaThanhToan, lọc theo NgayTT).
     * @param date định dạng "yyyy-MM-dd"
     */
    public double getTotalRevenueByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(TongCong), 0) FROM HoaDon " +
                "WHERE TrangThai = 'DaThanhToan' AND NgayTT = ?",
                new String[]{date})) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    /**
     * Tổng doanh thu trong khoảng ngày (tính theo NgayTT).
     * @param from "yyyy-MM-dd"
     * @param to   "yyyy-MM-dd"
     */
    public double getTotalRevenueByDateRange(String from, String to) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(TongCong), 0) FROM HoaDon " +
                "WHERE TrangThai = 'DaThanhToan' AND NgayTT BETWEEN ? AND ?",
                new String[]{from, to})) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    /**
     * Doanh thu theo từng ngày trong khoảng (dùng cho BarChart).
     * @return List of Object[]{String ngay, double tongTien}
     */
    public List<Object[]> getRevenueByDay(String from, String to) {
        List<Object[]> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT NgayTT, COALESCE(SUM(TongCong), 0) AS TongTien " +
                "FROM HoaDon " +
                "WHERE TrangThai = 'DaThanhToan' AND NgayTT BETWEEN ? AND ? " +
                "GROUP BY NgayTT ORDER BY NgayTT ASC",
                new String[]{from, to})) {
            while (c.moveToNext()) {
                result.add(new Object[]{
                    c.getString(0),   // ngay
                    c.getDouble(1)    // tongTien
                });
            }
        }
        return result;
    }

    /**
     * Đếm số hóa đơn trong khoảng ngày lập.
     */
    public int countByDateRange(String from, String to) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM HoaDon WHERE NgayLap BETWEEN ? AND ?",
                new String[]{from, to})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /**
     * Lấy danh sách hóa đơn trong khoảng ngày lập.
     */
    public List<HoaDon> getByDateRange(String from, String to) {
        List<HoaDon> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_SELECT_JOIN + "WHERE hd.NgayLap BETWEEN ? AND ? ORDER BY hd.NgayLap DESC",
                new String[]{from, to})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    // ─── WRITE ────────────────────────────────────────────────────────────────

    /**
     * Thêm hóa đơn mới.
     * @return rowId vừa insert, -1 nếu lỗi.
     */
    public long insert(HoaDon hd) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(hd);
        return db.insert("HoaDon", null, cv);
    }

    /**
     * Cập nhật trạng thái hóa đơn (xác nhận đã thanh toán, hoàn tiền…).
     * @param trangThai "DaThanhToan" | "ChuaThanhToan" | "HoanTien"
     * @return số dòng bị ảnh hưởng
     */
    public int updateTrangThai(int maHD, String trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TrangThai", trangThai);
        return db.update("HoaDon", cv, "MaHD = ?", new String[]{String.valueOf(maHD)});
    }

    /**
     * Cập nhật trạng thái hóa đơn kèm ngày thanh toán và phương thức.
     */
    public int confirmPayment(int maHD, String ngayTT, String phuongThucTT) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TrangThai",   "DaThanhToan");
        cv.put("NgayTT",      ngayTT);
        cv.put("PhuongThucTT", phuongThucTT);
        return db.update("HoaDon", cv, "MaHD = ?", new String[]{String.valueOf(maHD)});
    }

    /**
     * Xóa hóa đơn (cascade xóa ChiTietPhuThu liên quan theo FK).
     * @return số dòng bị ảnh hưởng
     */
    public int delete(int maHD) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("HoaDon", "MaHD = ?", new String[]{String.valueOf(maHD)});
    }

    // ─── PRIVATE ─────────────────────────────────────────────────────────────

    private ContentValues toContentValues(HoaDon hd) {
        ContentValues cv = new ContentValues();
        if (hd.getMaDatPhong() > 0) cv.put("MaDatPhong", hd.getMaDatPhong());
        else                         cv.putNull("MaDatPhong");
        cv.put("NgayLap",       hd.getNgayLap());
        cv.put("TienPhong",     hd.getTienPhong());
        cv.put("PhuThuDichVu",  hd.getPhuThuDichVu());
        cv.put("GiamGia",       hd.getGiamGia());
        cv.put("TongCong",      hd.getTongCong());
        cv.put("TrangThai",     hd.getTrangThai());
        if (hd.getPhuongThucTT() != null) cv.put("PhuongThucTT", hd.getPhuongThucTT());
        else                               cv.putNull("PhuongThucTT");
        if (hd.getNgayTT() != null) cv.put("NgayTT", hd.getNgayTT());
        else                         cv.putNull("NgayTT");
        if (hd.getMaNV() > 0) cv.put("MaNV", hd.getMaNV());
        else                   cv.putNull("MaNV");
        return cv;
    }
}
