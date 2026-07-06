package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.Phong;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác CRUD bảng Phong.
 * Mọi truy vấn JOIN thêm TenLoaiPhong từ bảng LoaiPhong.
 */
public class PhongDAO {
    private final DatabaseHelper dbHelper;

    public PhongDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // -------------------------------------------------------------------------
    // HELPER: map Cursor -> Phong (JOIN với LoaiPhong)
    // -------------------------------------------------------------------------
    private Phong mapCursor(Cursor c) {
        Phong p = new Phong();
        p.setMaPhong(c.getInt(c.getColumnIndexOrThrow("MaPhong")));
        p.setMaLoaiPhong(c.getInt(c.getColumnIndexOrThrow("MaLoaiPhong")));
        p.setTenPhong(c.getString(c.getColumnIndexOrThrow("TenPhong")));
        p.setGiaMoiDem(c.getDouble(c.getColumnIndexOrThrow("GiaMoiDem")));
        p.setSucChua(c.getInt(c.getColumnIndexOrThrow("SucChua")));
        p.setDienTich(c.getDouble(c.getColumnIndexOrThrow("DienTich")));
        p.setTang(c.getInt(c.getColumnIndexOrThrow("Tang")));
        p.setTrangThai(c.getString(c.getColumnIndexOrThrow("TrangThai")));
        p.setHinhAnh(c.getString(c.getColumnIndexOrThrow("HinhAnh")));
        p.setMoTa(c.getString(c.getColumnIndexOrThrow("MoTa")));
        int loaiIdx = c.getColumnIndex("TenLoai");
        if (loaiIdx != -1) p.setTenLoaiPhong(c.getString(loaiIdx));
        return p;
    }

    /** SQL JOIN chuẩn dùng chung cho tất cả query có TenLoai */
    private static final String SQL_SELECT_JOIN =
            "SELECT p.*, lp.TenLoai " +
            "FROM Phong p " +
            "LEFT JOIN LoaiPhong lp ON p.MaLoaiPhong = lp.MaLoaiPhong ";

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    /** Lấy tất cả phòng, JOIN TenLoaiPhong. */
    public List<Phong> getAll() {
        List<Phong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(SQL_SELECT_JOIN + "ORDER BY p.Tang, p.TenPhong", null)) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Lọc theo TrangThai ("Trong" | "DangThue" | "DaDat"). */
    public List<Phong> filterByTrangThai(String trangThai) {
        List<Phong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SQL_SELECT_JOIN + "WHERE p.TrangThai = ? ORDER BY p.Tang, p.TenPhong";
        try (Cursor c = db.rawQuery(sql, new String[]{trangThai})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Tìm kiếm theo TenPhong (LIKE). */
    public List<Phong> search(String keyword) {
        List<Phong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SQL_SELECT_JOIN + "WHERE p.TenPhong LIKE ? ORDER BY p.Tang, p.TenPhong";
        try (Cursor c = db.rawQuery(sql, new String[]{"%" + keyword + "%"})) {
            while (c.moveToNext()) list.add(mapCursor(c));
        }
        return list;
    }

    /** Chỉ lấy phòng TrangThai = 'Trong' – dùng cho Spinner đặt phòng. */
    public List<Phong> getAvailable() {
        return filterByTrangThai("Trong");
    }

    /** Tìm phòng theo MaPhong. Trả null nếu không tìm thấy. */
    public Phong findById(int maPhong) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = SQL_SELECT_JOIN + "WHERE p.MaPhong = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(maPhong)})) {
            if (c.moveToFirst()) return mapCursor(c);
        }
        return null;
    }

    /** Đếm số phòng theo trạng thái – dùng cho KPI card Home. */
    public int countByTrangThai(String trangThai) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM Phong WHERE TrangThai = ?",
                new String[]{trangThai})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Tổng số phòng – dùng cho KPI card Home. */
    public int countAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM Phong", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /**
     * Kiểm tra tên phòng đã tồn tại chưa.
     * @param tenPhong  Tên cần kiểm tra (so sánh không phân biệt hoa thường).
     * @param excludeMaPhong  MaPhong cần bỏ qua (khi edit, bỏ qua chính nó). Truyền -1 khi thêm mới.
     * @return true nếu đã tồn tại phòng khác có tên này.
     */
    public boolean isTenPhongDuplicate(String tenPhong, int excludeMaPhong) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = excludeMaPhong > 0
                ? "SELECT COUNT(*) FROM Phong WHERE LOWER(TenPhong) = LOWER(?) AND MaPhong != ?"
                : "SELECT COUNT(*) FROM Phong WHERE LOWER(TenPhong) = LOWER(?)";
        String[] args = excludeMaPhong > 0
                ? new String[]{tenPhong, String.valueOf(excludeMaPhong)}
                : new String[]{tenPhong};
        try (Cursor c = db.rawQuery(sql, args)) {
            if (c.moveToFirst()) return c.getInt(0) > 0;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // WRITE
    // -------------------------------------------------------------------------

    /**
     * Thêm phòng mới.
     * @return rowId của bản ghi vừa insert, -1 nếu lỗi.
     */
    public long insert(Phong phong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(phong);
        return db.insert("Phong", null, cv);
    }

    /**
     * Cập nhật phòng.
     * @return số dòng bị ảnh hưởng.
     */
    public int update(Phong phong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(phong);
        return db.update("Phong", cv, "MaPhong = ?",
                new String[]{String.valueOf(phong.getMaPhong())});
    }

    /**
     * Xóa phòng theo MaPhong.
     * @return số dòng bị ảnh hưởng.
     */
    public int delete(int maPhong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("Phong", "MaPhong = ?", new String[]{String.valueOf(maPhong)});
    }

    /**
     * Cập nhật trạng thái phòng (gọi sau Check-in/Check-out).
     * @param trangThai "Trong" | "DangThue" | "DaDat"
     * @return số dòng bị ảnh hưởng.
     */
    public int updateTrangThai(int maPhong, String trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TrangThai", trangThai);
        return db.update("Phong", cv, "MaPhong = ?", new String[]{String.valueOf(maPhong)});
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------
    private ContentValues toContentValues(Phong p) {
        ContentValues cv = new ContentValues();
        cv.put("MaLoaiPhong", p.getMaLoaiPhong());
        cv.put("TenPhong",    p.getTenPhong());
        cv.put("GiaMoiDem",   p.getGiaMoiDem());
        cv.put("SucChua",     p.getSucChua());
        cv.put("DienTich",    p.getDienTich());
        cv.put("Tang",        p.getTang());
        cv.put("TrangThai",   p.getTrangThai());
        cv.put("HinhAnh",     p.getHinhAnh());
        cv.put("MoTa",        p.getMoTa());
        return cv;
    }
}
