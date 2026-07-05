package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.TienNghi;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác bảng TienNghi.
 */
public class TienNghiDAO {
    private final DatabaseHelper dbHelper;

    public TienNghiDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** Lấy tất cả tiện nghi – dùng để populate CheckBox trong form thêm phòng. */
    public List<TienNghi> getAll() {
        List<TienNghi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT MaTienNghi, TenTienNghi FROM TienNghi ORDER BY MaTienNghi",
                null)) {
            while (c.moveToNext()) {
                TienNghi t = new TienNghi();
                t.setMaTienNghi(c.getInt(c.getColumnIndexOrThrow("MaTienNghi")));
                t.setTenTienNghi(c.getString(c.getColumnIndexOrThrow("TenTienNghi")));
                list.add(t);
            }
        }
        return list;
    }
}
