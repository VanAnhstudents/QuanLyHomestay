package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.PhanQuyenVaiTro;

import java.util.ArrayList;
import java.util.List;

public class PhanQuyenVaiTroDAO {
    private final DatabaseHelper dbHelper;

    public PhanQuyenVaiTroDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<PhanQuyenVaiTro> getByVaiTro(String maVaiTro) {
        List<PhanQuyenVaiTro> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("PhanQuyen_VaiTro", null,
                "MaVaiTro = ?", new String[]{maVaiTro},
                null, null, "MaModule ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public PhanQuyenVaiTro findByVaiTroAndModule(String maVaiTro, int maModule) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("PhanQuyen_VaiTro", null,
                "MaVaiTro = ? AND MaModule = ?",
                new String[]{maVaiTro, String.valueOf(maModule)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    private PhanQuyenVaiTro cursorToModel(Cursor c) {
        PhanQuyenVaiTro pq = new PhanQuyenVaiTro();
        pq.setMaPhanQuyen(c.getInt(c.getColumnIndexOrThrow("MaPhanQuyen")));
        pq.setMaVaiTro(c.getString(c.getColumnIndexOrThrow("MaVaiTro")));
        pq.setMaModule(c.getInt(c.getColumnIndexOrThrow("MaModule")));
        pq.setMaQuyen(c.getInt(c.getColumnIndexOrThrow("MaQuyen")));
        return pq;
    }
}
