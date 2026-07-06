package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.PhanCongCa;

import java.util.ArrayList;
import java.util.List;

public class PhanCongCaDAO {
    private final DatabaseHelper dbHelper;

    public PhanCongCaDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<PhanCongCa> getByNhanVien(int maNV) {
        List<PhanCongCa> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM PhanCongCa WHERE MaNV = ? " +
                        "AND TuanBatDau = (SELECT MAX(TuanBatDau) FROM PhanCongCa WHERE MaNV = ?) " +
                        "ORDER BY ThuTrongTuan ASC",
                new String[]{String.valueOf(maNV), String.valueOf(maNV)})) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public List<PhanCongCa> getByNhanVienAndWeek(int maNV, String tuanBatDau) {
        List<PhanCongCa> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("PhanCongCa", null,
                "MaNV = ? AND TuanBatDau = ?", new String[]{String.valueOf(maNV), tuanBatDau},
                null, null, "ThuTrongTuan ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    /**
     * Xóa hết phân công cũ của 1 nhân viên rồi insert lại danh sách mới bọc trong transaction để tránh mất dữ liệu nếu insert giữa đường lỗi.
     */
    public void replaceAll(int maNV, List<PhanCongCa> danhSach) {
        replaceWeek(maNV, "1970-01-05", danhSach);
    }

    public void replaceWeek(int maNV, String tuanBatDau, List<PhanCongCa> danhSach) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("PhanCongCa", "MaNV = ? AND TuanBatDau = ?",
                    new String[]{String.valueOf(maNV), tuanBatDau});
            for (PhanCongCa pc : danhSach) {
                ContentValues cv = new ContentValues();
                cv.put("MaNV", maNV);
                cv.put("MaCa", pc.getMaCa());
                cv.put("ThuTrongTuan", pc.getThuTrongTuan());
                cv.put("TuanBatDau", tuanBatDau);
                db.insert("PhanCongCa", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private PhanCongCa cursorToModel(Cursor c) {
        PhanCongCa pc = new PhanCongCa();
        pc.setMaPhanCong(c.getInt(c.getColumnIndexOrThrow("MaPhanCong")));
        pc.setMaNV(c.getInt(c.getColumnIndexOrThrow("MaNV")));
        pc.setMaCa(c.getInt(c.getColumnIndexOrThrow("MaCa")));
        pc.setThuTrongTuan(c.getInt(c.getColumnIndexOrThrow("ThuTrongTuan")));
        pc.setTuanBatDau(c.getString(c.getColumnIndexOrThrow("TuanBatDau")));
        return pc;
    }
}
