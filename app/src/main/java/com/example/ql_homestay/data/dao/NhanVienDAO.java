package com.example.ql_homestay.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.NhanVien;

import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    private final DatabaseHelper dbHelper;

    public NhanVienDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<NhanVien> getAll() {
        List<NhanVien> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("NhanVien", null, null, null, null, null, "HoTen ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public List<NhanVien> search(String keyword) {
        List<NhanVien> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String like = "%" + keyword + "%";
        try (Cursor c = db.query("NhanVien", null,
                "HoTen LIKE ? OR SDT LIKE ? OR CCCD LIKE ?",
                new String[]{like, like, like},
                null, null, "HoTen ASC")) {
            while (c.moveToNext()) list.add(cursorToModel(c));
        }
        return list;
    }

    public NhanVien findById(int maNV) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("NhanVien", null,
                "MaNV = ?", new String[]{String.valueOf(maNV)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    public NhanVien findByMaTK(int maTK) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query("NhanVien", null,
                "MaTK = ?", new String[]{String.valueOf(maTK)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return cursorToModel(c);
        }
        return null;
    }

    public long insert(NhanVien nv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert("NhanVien", null, buildContentValues(nv));
    }

    public int update(NhanVien nv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update("NhanVien", buildContentValues(nv),
                "MaNV = ?", new String[]{String.valueOf(nv.getMaNV())});
    }

    public int delete(int maNV) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("NhanVien", "MaNV = ?", new String[]{String.valueOf(maNV)});
    }

    private NhanVien cursorToModel(Cursor c) {
        NhanVien nv = new NhanVien();
        nv.setMaNV(c.getInt(c.getColumnIndexOrThrow("MaNV")));
        int idxMaTK = c.getColumnIndexOrThrow("MaTK");
        nv.setMaTK(c.isNull(idxMaTK) ? null : c.getInt(idxMaTK));
        nv.setHoTen(c.getString(c.getColumnIndexOrThrow("HoTen")));
        nv.setChucVu(c.getString(c.getColumnIndexOrThrow("ChucVu")));
        nv.setSdt(c.getString(c.getColumnIndexOrThrow("SDT")));
        nv.setEmail(c.getString(c.getColumnIndexOrThrow("Email")));
        nv.setCccd(c.getString(c.getColumnIndexOrThrow("CCCD")));
        nv.setDiaChi(c.getString(c.getColumnIndexOrThrow("DiaChi")));
        nv.setNgayVaoLam(c.getString(c.getColumnIndexOrThrow("NgayVaoLam")));
        nv.setAvatar(c.getString(c.getColumnIndexOrThrow("Avatar")));
        return nv;
    }

    private ContentValues buildContentValues(NhanVien nv) {
        ContentValues cv = new ContentValues();
        if (nv.getMaTK() != null) cv.put("MaTK", nv.getMaTK());
        cv.put("HoTen", nv.getHoTen());
        cv.put("ChucVu", nv.getChucVu());
        if (nv.getSdt() != null) cv.put("SDT", nv.getSdt());
        if (nv.getEmail() != null) cv.put("Email", nv.getEmail());
        if (nv.getCccd() != null) cv.put("CCCD", nv.getCccd());
        if (nv.getDiaChi() != null) cv.put("DiaChi", nv.getDiaChi());
        if (nv.getNgayVaoLam() != null) cv.put("NgayVaoLam", nv.getNgayVaoLam());
        if (nv.getAvatar() != null) cv.put("Avatar", nv.getAvatar());
        return cv;
    }
}