package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.Quyen;

import java.util.ArrayList;
import java.util.List;

public class QuyenDAO {
    private final DatabaseHelper dbHelper;

    public QuyenDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Quyen> getAll() {
        List<Quyen> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("Quyen", null, null, null, null, null, "MaQuyen ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public Quyen findById(int maQuyen) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("Quyen", null,
                "MaQuyen = ?", new String[]{String.valueOf(maQuyen)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    public Quyen findByTenQuyen(String tenQuyen) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("Quyen", null,
                "TenQuyen = ?", new String[]{tenQuyen},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    private Quyen cursorToModel(Cursor c) {
        Quyen q = new Quyen();
        q.setMaQuyen(c.getInt(c.getColumnIndexOrThrow("MaQuyen")));
        q.setTenQuyen(c.getString(c.getColumnIndexOrThrow("TenQuyen")));
        return q;
    }
}