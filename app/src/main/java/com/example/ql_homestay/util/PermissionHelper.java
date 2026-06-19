package com.example.ql_homestay.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;

/**
 * PermissionHelper — kiểm tra quyền truy cập module theo RBAC.
 * Toàn bộ logic ẩn/hiện View trong app phải gọi vào class này,
 * KHÔNG hardcode quyền trực tiếp trong XML.
 * Bảng PhanQuyen_VaiTro có cấu trúc:
 *   MaVaiTro | MaModule | MaQuyen
 * Bảng Quyen có: MaQuyen | TenQuyen ("ToanQuyen" | "XemVaTao" | "ChiXem" | "KhongTruyCap")
 * Bảng Module có: MaModule | TenModule
 */

public class PermissionHelper {
    // Tên module (khớp cột TenModule trong bảng Module)
    public static final String MODULE_TRANG_CHU = "TrangChu";
    public static final String MODULE_QUAN_LY_PHONG = "QuanLyPhong";
    public static final String MODULE_QUAN_LY_DAT_PHONG = "QuanLyDatPhong";
    public static final String MODULE_QUAN_LY_KHACH = "QuanLyKhachHang";
    public static final String MODULE_HOA_DON = "HoaDonThanhToan";
    public static final String MODULE_NHAN_VIEN = "QuanLyNhanVien";
    public static final String MODULE_BAO_CAO = "BaoCaoThongKe";
    public static final String MODULE_CAI_DAT = "CaiDatHeThong";

    // Tên quyền (khớp cột TenQuyen trong bảng Quyen)
    public static final String QUYEN_TOAN_QUYEN = "ToanQuyen";
    public static final String QUYEN_XEM_VA_TAO = "XemVaTao";
    public static final String QUYEN_CHI_XEM = "ChiXem";
    public static final String QUYEN_KHONG_TRUY_CAP = "KhongTruyCap";

    // Thứ tự ưu tiên quyền (cao hơn = nhiều quyền hơn)
    private static int getQuyenLevel(String tenQuyen) {
        if (tenQuyen == null) return 0;
        switch (tenQuyen) {
            case QUYEN_TOAN_QUYEN: return 4;
            case QUYEN_XEM_VA_TAO: return 3;
            case QUYEN_CHI_XEM: return 2;
            case QUYEN_KHONG_TRUY_CAP: return 1;
            default: return 0;
        }
    }

    // API chính
    /**
     * Kiểm tra xem vai trò có quyền tối thiểu {@code minQuyen} trên module không.
     * @param db SQLiteDatabase (dùng instance từ DatabaseHelper)
     * @param vaiTro Tên vai trò: "Admin" | "LeTan" | "KeToan" | "NhanVien"
     * @param tenModule Tên module (dùng hằng số MODULE_* ở trên)
     * @param minQuyen Quyền tối thiểu cần có (dùng hằng số QUYEN_* ở trên)
     * @return true nếu vai trò có quyền >= minQuyen trên module
     */
    public static boolean hasAccess(SQLiteDatabase db, String vaiTro, String tenModule, String minQuyen) {
        if (db == null || vaiTro == null || tenModule == null) return false;

        // Admin luôn có toàn quyền — không cần truy vấn DB
        if ("Admin".equals(vaiTro)) return true;

        String actualQuyen = getQuyenForRole(db, vaiTro, tenModule);
        return getQuyenLevel(actualQuyen) >= getQuyenLevel(minQuyen);
    }

    /**
     * Lấy tên quyền thực tế của một vai trò trên một module.
     * Trả về {@link #QUYEN_KHONG_TRUY_CAP} nếu không tìm thấy bản ghi.
     */
    public static String getQuyenForRole(SQLiteDatabase db, String vaiTro, String tenModule) {
        if (db == null) return QUYEN_KHONG_TRUY_CAP;

        String sql =
                "SELECT q.TenQuyen " +
                        "FROM PhanQuyen_VaiTro pqvt " +
                        "JOIN Quyen q ON pqvt.MaQuyen = q.MaQuyen " +
                        "JOIN Module m ON pqvt.MaModule = m.MaModule " +
                        "WHERE pqvt.MaVaiTro = ? AND m.TenModule = ? " +
                        "LIMIT 1";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{vaiTro, tenModule});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return QUYEN_KHONG_TRUY_CAP;
    }

    /**
     * Kiểm tra nhanh: vai trò có thể truy cập module không (quyền > KhongTruyCap).
     */
    public static boolean canAccess(SQLiteDatabase db, String vaiTro, String tenModule) {
        return hasAccess(db, vaiTro, tenModule, QUYEN_CHI_XEM);
    }

    /**
     * Kiểm tra vai trò có thể tạo/sửa không (quyền >= XemVaTao).
     */
    public static boolean canCreate(SQLiteDatabase db, String vaiTro, String tenModule) {
        return hasAccess(db, vaiTro, tenModule, QUYEN_XEM_VA_TAO);
    }

    /**
     * Kiểm tra vai trò có toàn quyền (thêm/sửa/xóa) không.
     */
    public static boolean hasFullAccess(SQLiteDatabase db, String vaiTro, String tenModule) {
        return hasAccess(db, vaiTro, tenModule, QUYEN_TOAN_QUYEN);
    }

    // Overload tiện lợi với DatabaseHelper

    /**
     * Overload nhận DatabaseHelper thay vì SQLiteDatabase trực tiếp.
     * Tiện dùng trong Activity/Fragment khi đã có dbHelper.
     */
    public static boolean hasAccess(DatabaseHelper dbHelper, String vaiTro, String tenModule, String minQuyen) {
        if (dbHelper == null) return false;
        return hasAccess(dbHelper.getReadableDatabase(), vaiTro, tenModule, minQuyen);
    }

    public static boolean canCreate(DatabaseHelper dbHelper, String vaiTro, String tenModule) {
        if (dbHelper == null) return false;
        return canCreate(dbHelper.getReadableDatabase(), vaiTro, tenModule);
    }

    public static boolean hasFullAccess(DatabaseHelper dbHelper, String vaiTro, String tenModule) {
        if (dbHelper == null) return false;
        return hasFullAccess(dbHelper.getReadableDatabase(), vaiTro, tenModule);
    }
}
