package com.example.ql_homestay.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.CaLamViecDAO;
import com.example.ql_homestay.data.dao.NhanVienDAO;
import com.example.ql_homestay.data.dao.PhanCongCaDAO;
import com.example.ql_homestay.data.dao.TaiKhoanDAO;
import com.example.ql_homestay.model.CaLamViec;
import com.example.ql_homestay.model.NhanVien;
import com.example.ql_homestay.model.PhanCongCa;
import com.example.ql_homestay.model.TaiKhoan;

import java.util.List;

public class StaffRepository {
    private final DatabaseHelper dbHelper;
    private final NhanVienDAO nhanVienDAO;
    private final PhanCongCaDAO phanCongCaDAO;
    private final CaLamViecDAO caLamViecDAO;
    private final TaiKhoanDAO taiKhoanDAO;

    public StaffRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.nhanVienDAO = new NhanVienDAO(dbHelper);
        this.phanCongCaDAO = new PhanCongCaDAO(dbHelper);
        this.caLamViecDAO = new CaLamViecDAO(dbHelper);
        this.taiKhoanDAO = new TaiKhoanDAO(dbHelper);
    }

    // ----- Nhân viên -----

    public List<NhanVien> getAllStaff() {
        return nhanVienDAO.getAll();
    }

    public List<NhanVien> searchStaff(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return nhanVienDAO.getAll();
        return nhanVienDAO.search(keyword.trim());
    }

    public NhanVien getStaffById(int maNV) {
        return nhanVienDAO.findById(maNV);
    }

    public int updateStaff(NhanVien nv) {
        return nhanVienDAO.update(nv);
    }

    public int deleteStaff(int maNV) {
        return nhanVienDAO.delete(maNV);
    }

    /**
     * Tạo NhanVien mới kèm TaiKhoan đăng nhập liên kết, trong 1 transaction
     * (theo lo_trinh.md B2). Lưu ý: tk.getNgayTao() phải được set trước khi
     * gọi (cột NgayTao là NOT NULL trong schema TaiKhoan).
     * @return MaNV vừa tạo, hoặc -1 nếu thất bại
     */
    public long createStaffWithAccount(NhanVien nv, TaiKhoan tk) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long maTK = taiKhoanDAO.insert(tk);
            if (maTK == -1) return -1;

            nv.setMaTK((int) maTK);
            long maNV = nhanVienDAO.insert(nv);
            if (maNV == -1) return -1;

            db.setTransactionSuccessful();
            return maNV;
        } finally {
            db.endTransaction();
        }
    }

    // ----- Ca làm việc -----

    public List<CaLamViec> getAllShifts() {
        return caLamViecDAO.getAll();
    }

    public List<PhanCongCa> getShiftAssignments(int maNV) {
        return phanCongCaDAO.getByNhanVien(maNV);
    }

    public void saveShiftAssignments(int maNV, List<PhanCongCa> danhSach) {
        phanCongCaDAO.replaceAll(maNV, danhSach);
    }
}