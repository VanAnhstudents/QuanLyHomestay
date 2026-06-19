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
        try (Cursor c = db.query("PhanCongCa", null,
                "MaNV = ?", new String[]{String.valueOf(maNV)},
                null, null, "ThuTrongTuan ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    /**
     * Xóa hết phân công cũ của 1 nhân viên rồi insert lại danh sách mới bọc trong transaction để tránh mất dữ liệu nếu insert giữa đường lỗi.
     */
    public void replaceAll(int maNV, List<PhanCongCa> danhSach) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("PhanCongCa", "MaNV = ?", new String[]{String.valueOf(maNV)});
            for (PhanCongCa pc : danhSach) {
                ContentValues cv = new ContentValues();
                cv.put("MaNV", maNV);
                cv.put("MaCa", pc.getMaCa());
                cv.put("ThuTrongTuan", pc.getThuTrongTuan());
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
        return pc;
    }
}