package com.example.ql_homestay.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.HoaDonDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * StatisticsRepository – cung cấp các chỉ số thống kê cho Dashboard và báo cáo.
 * Tất cả query đều truy vấn trực tiếp DB thông qua DatabaseHelper.
 */
public class StatisticsRepository {

    private final DatabaseHelper dbHelper;
    private final HoaDonDAO hoaDonDAO;

    public StatisticsRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.hoaDonDAO = new HoaDonDAO(dbHelper);
    }

    // ─── PHÒNG ───────────────────────────────────────────────────────────────

    /** Tổng số phòng trong hệ thống. */
    public int getTotalRooms() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM Phong", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /**
     * Số phòng theo trạng thái.
     * @param trangThai "Trong" | "DangThue" | "DaDat"
     */
    public int getRoomsByTrangThai(String trangThai) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM Phong WHERE TrangThai = ?",
                new String[]{trangThai})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    // ─── DOANH THU ───────────────────────────────────────────────────────────

    /**
     * Doanh thu hôm nay (theo NgayTT = today, chỉ HĐ DaThanhToan).
     * @param today "yyyy-MM-dd"
     */
    public double getRevenueToday(String today) {
        return hoaDonDAO.getTotalRevenueByDate(today);
    }

    /**
     * Doanh thu trong khoảng ngày.
     * @param from "yyyy-MM-dd"
     * @param to   "yyyy-MM-dd"
     */
    public double getRevenueByRange(String from, String to) {
        return hoaDonDAO.getTotalRevenueByDateRange(from, to);
    }

    /**
     * Doanh thu theo từng ngày trong khoảng (dùng cho BarChart).
     * @return List of Object[]{String ngay, double tongTien}
     */
    public List<Object[]> getRevenueByDay(String from, String to) {
        return hoaDonDAO.getRevenueByDay(from, to);
    }

    // ─── ĐẶT PHÒNG ──────────────────────────────────────────────────────────

    /**
     * Tổng số đặt phòng trong khoảng ngày (tính theo NgayTao).
     */
    public int getTotalBookingsByRange(String from, String to) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM DatPhong WHERE NgayTao BETWEEN ? AND ?",
                new String[]{from, to})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /**
     * Tổng số lượt khách (SoLuongKhach) trong khoảng ngày.
     */
    public int getTotalGuestsByRange(String from, String to) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(SoLuongKhach), 0) FROM DatPhong " +
                "WHERE NgayTao BETWEEN ? AND ? AND TrangThai != 'DaHuy'",
                new String[]{from, to})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    // ─── CÔNG SUẤT PHÒNG ─────────────────────────────────────────────────────

    /**
     * Tỷ lệ công suất phòng (%) = (tổng đêm phòng đã thuê) / (tổng phòng × số ngày).
     * Chỉ đếm DatPhong trạng thái DangO hoặc DaTraPhong (thực sự đã sử dụng).
     *
     * @param from "yyyy-MM-dd"
     * @param to   "yyyy-MM-dd"
     * @param totalDays số ngày trong kỳ (to - from + 1)
     */
    public double getOccupancyRateByRange(String from, String to, int totalDays) {
        int totalRooms = getTotalRooms();
        if (totalRooms == 0 || totalDays == 0) return 0.0;

        // Tổng đêm phòng đã sử dụng = SUM(SoDem) của các DatPhong thuộc kỳ
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalNightsSold = 0;
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(SoDem), 0) FROM DatPhong " +
                "WHERE TrangThai IN ('DangO','DaTraPhong') " +
                "AND NgayCheckIn <= ? AND NgayCheckOut >= ?",
                new String[]{to, from})) {
            if (c.moveToFirst()) totalNightsSold = c.getDouble(0);
        }

        double totalAvailable = totalRooms * (double) totalDays;
        return (totalNightsSold / totalAvailable) * 100.0;
    }

    /**
     * Tỷ lệ công suất từng ngày trong khoảng (dùng cho LineChart).
     * @return List of Object[]{String ngay, double congSuat%}
     */
    public List<Object[]> getOccupancyByDay(String from, String to) {
        List<Object[]> result = new ArrayList<>();
        int totalRooms = getTotalRooms();
        if (totalRooms == 0) return result;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // SQLite không có date arithmetic tiện lợi; dùng JOIN đơn giản:
        // đếm số phòng có DatPhong DangO/DaTraPhong bao phủ ngày đó
        try (Cursor c = db.rawQuery(
                "WITH RECURSIVE dates(d) AS (" +
                "  SELECT ? " +
                "  UNION ALL SELECT date(d, '+1 day') FROM dates WHERE d < ?" +
                ") " +
                "SELECT d, (" +
                "  SELECT COUNT(DISTINCT MaPhong) FROM DatPhong " +
                "  WHERE TrangThai IN ('DangO','DaTraPhong') " +
                "  AND NgayCheckIn <= d AND NgayCheckOut > d" +
                ") AS SoPhongDung " +
                "FROM dates",
                new String[]{from, to})) {
            while (c.moveToNext()) {
                String ngay = c.getString(0);
                int soPhong = c.getInt(1);
                double pct = (soPhong / (double) totalRooms) * 100.0;
                result.add(new Object[]{ngay, pct});
            }
        }
        return result;
    }

    /**
     * Công suất phòng từng phòng trong khoảng ngày.
     * @return List of Object[]{String tenPhong, int soDemDungThuc, double doanhThu, double congSuat%}
     */
    public List<Object[]> getRoomOccupancyDetail(String from, String to, int totalDays) {
        List<Object[]> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT p.TenPhong, " +
                "  COALESCE(SUM(dp.SoDem), 0) AS SoDemDung, " +
                "  COALESCE(SUM(hd.TongCong), 0) AS DoanhThu " +
                "FROM Phong p " +
                "LEFT JOIN DatPhong dp ON p.MaPhong = dp.MaPhong " +
                "  AND dp.TrangThai IN ('DangO','DaTraPhong') " +
                "  AND dp.NgayCheckIn <= ? AND dp.NgayCheckOut >= ? " +
                "LEFT JOIN HoaDon hd ON dp.MaDatPhong = hd.MaDatPhong " +
                "  AND hd.TrangThai = 'DaThanhToan' " +
                "GROUP BY p.MaPhong, p.TenPhong " +
                "ORDER BY DoanhThu DESC";
        try (Cursor c = db.rawQuery(sql, new String[]{to, from})) {
            while (c.moveToNext()) {
                String tenPhong = c.getString(0);
                int soDem = c.getInt(1);
                double doanhThu = c.getDouble(2);
                double congSuat = totalDays > 0 ? (soDem / (double) totalDays) * 100.0 : 0;
                result.add(new Object[]{tenPhong, soDem, doanhThu, congSuat});
            }
        }
        return result;
    }

    // ─── TOP PHÒNG ────────────────────────────────────────────────────────────

    /**
     * Top N phòng theo doanh thu (tất cả thời gian, chỉ HĐ DaThanhToan).
     * @param limit số lượng phòng trả về
     * @return List of Object[]{String tenPhong, double doanhThu}
     */
    public List<Object[]> getTopRoomsByRevenue(int limit) {
        List<Object[]> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT p.TenPhong, COALESCE(SUM(hd.TongCong), 0) AS DoanhThu " +
                "FROM Phong p " +
                "LEFT JOIN DatPhong dp ON p.MaPhong = dp.MaPhong " +
                "LEFT JOIN HoaDon hd ON dp.MaDatPhong = hd.MaDatPhong " +
                "  AND hd.TrangThai = 'DaThanhToan' " +
                "GROUP BY p.MaPhong, p.TenPhong " +
                "ORDER BY DoanhThu DESC " +
                "LIMIT ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(limit)})) {
            while (c.moveToNext()) {
                result.add(new Object[]{c.getString(0), c.getDouble(1)});
            }
        }
        return result;
    }

    /**
     * Top N phòng theo doanh thu trong khoảng ngày.
     * @return List of Object[]{String tenPhong, double doanhThu}
     */
    public List<Object[]> getTopRoomsByRevenueInRange(String from, String to, int limit) {
        List<Object[]> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT p.TenPhong, COALESCE(SUM(hd.TongCong), 0) AS DoanhThu " +
                "FROM Phong p " +
                "LEFT JOIN DatPhong dp ON p.MaPhong = dp.MaPhong " +
                "LEFT JOIN HoaDon hd ON dp.MaDatPhong = hd.MaDatPhong " +
                "  AND hd.TrangThai = 'DaThanhToan' " +
                "  AND hd.NgayTT BETWEEN ? AND ? " +
                "GROUP BY p.MaPhong, p.TenPhong " +
                "ORDER BY DoanhThu DESC " +
                "LIMIT ?";
        try (Cursor c = db.rawQuery(sql, new String[]{from, to, String.valueOf(limit)})) {
            while (c.moveToNext()) {
                result.add(new Object[]{c.getString(0), c.getDouble(1)});
            }
        }
        return result;
    }
}
