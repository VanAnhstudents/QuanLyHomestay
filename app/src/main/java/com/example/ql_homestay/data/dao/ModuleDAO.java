package com.example.ql_homestay.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.Module;

import java.util.ArrayList;
import java.util.List;

/** Cần cho E2 (RecyclerView 8 dòng module trong màn Phân quyền tài khoản). */
public class ModuleDAO {
    private final DatabaseHelper dbHelper;

    public ModuleDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Module> getAll() {
        List<Module> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("Module", null, null, null, null, null, "MaModule ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public Module findById(int maModule) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("Module", null,
                "MaModule = ?", new String[]{String.valueOf(maModule)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    public Module findByTenModule(String tenModule) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("Module", null,
                "TenModule = ?", new String[]{tenModule},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    private Module cursorToModel(Cursor c) {
        Module m = new Module();
        m.setMaModule(c.getInt(c.getColumnIndexOrThrow("MaModule")));
        m.setTenModule(c.getString(c.getColumnIndexOrThrow("TenModule")));
        m.setIcon(c.getString(c.getColumnIndexOrThrow("Icon")));
        return m;
    }
}