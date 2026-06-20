package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.PhanQuyenVaiTro;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhanQuyenVaiTroDAO {
    private final DatabaseHelper dbHelper;

    public PhanQuyenVaiTroDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** 8 dòng phân quyền của 1 vai trò, sắp theo MaModule. */
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

    /**
     * Cập nhật quyền cho 1 (vaiTro, module). Vì PhanQuyen_VaiTro đã seed đủ
     * 32 dòng (4 vai trò x 8 module) nên bình thường chỉ UPDATE; chỉ INSERT
     * thêm nếu lỡ thiếu dòng (trường hợp hiếm).
     */
    public int updateQuyen(String maVaiTro, int maModule, int maQuyen) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("MaQuyen", maQuyen);
        int rows = db.update("PhanQuyen_VaiTro", cv,
                "MaVaiTro = ? AND MaModule = ?",
                new String[]{maVaiTro, String.valueOf(maModule)});
        if (rows == 0) {
            cv.put("MaVaiTro", maVaiTro);
            cv.put("MaModule", maModule);
            db.insert("PhanQuyen_VaiTro", null, cv);
            rows = 1;
        }
        return rows;
    }

    /** Lưu đồng loạt 8 module của 1 vai trò trong 1 transaction (nút "Lưu thay đổi" trong màn phân quyền). */
    public void updateAllForVaiTro(String maVaiTro, Map<Integer, Integer> maQuyenByModule) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<Integer, Integer> entry : maQuyenByModule.entrySet()) {
                ContentValues cv = new ContentValues();
                cv.put("MaQuyen", entry.getValue());
                db.update("PhanQuyen_VaiTro", cv,
                        "MaVaiTro = ? AND MaModule = ?",
                        new String[]{maVaiTro, String.valueOf(entry.getKey())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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