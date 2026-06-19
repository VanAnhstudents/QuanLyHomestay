package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.CaLamViec;

import java.util.ArrayList;
import java.util.List;

/** Bảng CaLamViec chỉ có đúng 3 dòng cố định (Sáng/Chiều/Tối), không có insert/update/delete. */
public class CaLamViecDAO {
    private final DatabaseHelper dbHelper;

    public CaLamViecDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<CaLamViec> getAll() {
        List<CaLamViec> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("CaLamViec", null, null, null, null, null, "MaCa ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public CaLamViec findById(int maCa) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("CaLamViec", null,
                "MaCa = ?", new String[]{String.valueOf(maCa)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    private CaLamViec cursorToModel(Cursor c) {
        CaLamViec ca = new CaLamViec();
        ca.setMaCa(c.getInt(c.getColumnIndexOrThrow("MaCa")));
        ca.setTenCa(c.getString(c.getColumnIndexOrThrow("TenCa")));
        ca.setGioBatDau(c.getString(c.getColumnIndexOrThrow("GioBatDau")));
        ca.setGioKetThuc(c.getString(c.getColumnIndexOrThrow("GioKetThuc")));
        return ca;
    }
}