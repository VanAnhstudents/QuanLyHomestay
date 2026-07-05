package com.example.ql_homestay.repository;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.CheckInOutDAO;
import com.example.ql_homestay.data.dao.DatPhongDAO;
import com.example.ql_homestay.data.dao.KhachHangDAO;
import com.example.ql_homestay.data.dao.PhongDAO;
import com.example.ql_homestay.model.CheckInOut;
import com.example.ql_homestay.model.DatPhong;

import java.util.List;

/**
 * Repository tầng trung gian cho module Đặt phòng.
 * Tổng hợp DatPhongDAO + CheckInOutDAO + PhongDAO (cập nhật trạng thái).
 */
public class BookingRepository {
    private final DatPhongDAO datPhongDAO;
    private final CheckInOutDAO checkInOutDAO;
    private final PhongDAO phongDAO;
    private final KhachHangDAO khachHangDAO;

    public BookingRepository(DatabaseHelper dbHelper) {
        this.datPhongDAO   = new DatPhongDAO(dbHelper);
        this.checkInOutDAO = new CheckInOutDAO(dbHelper);
        this.phongDAO      = new PhongDAO(dbHelper);
        this.khachHangDAO  = new KhachHangDAO(dbHelper);
    }

    // -------- DatPhong --------
    public List<DatPhong> getAllDatPhong() { return datPhongDAO.getAll(); }

    public List<DatPhong> filterByTrangThai(String trangThai) {
        return datPhongDAO.filterByTrangThai(trangThai);
    }

    public List<DatPhong> searchDatPhong(String keyword) { return datPhongDAO.search(keyword); }

    public DatPhong findDatPhongById(int maDatPhong) { return datPhongDAO.findById(maDatPhong); }

    public long insertDatPhong(DatPhong dp) { return datPhongDAO.insert(dp); }

    public int updateDatPhong(DatPhong dp) { return datPhongDAO.update(dp); }

    public int updateTrangThaiDatPhong(int maDatPhong, String trangThai) {
        return datPhongDAO.updateTrangThai(maDatPhong, trangThai);
    }

    public int deleteDatPhong(int maDatPhong) { return datPhongDAO.delete(maDatPhong); }

    public List<DatPhong> getDatPhongByPhong(int maPhong) {
        return datPhongDAO.getByPhong(maPhong);
    }

    public List<DatPhong> getDatPhongByKhachHang(int maKH) {
        return datPhongDAO.getByKhachHang(maKH);
    }

    public List<DatPhong> getTodayAndActive(String today) {
        return datPhongDAO.getTodayAndActive(today);
    }

    // -------- CheckInOut --------
    public List<CheckInOut> getCheckInOutByDatPhong(int maDatPhong) {
        return checkInOutDAO.getByDatPhong(maDatPhong);
    }

    public List<CheckInOut> getRecentActivity(int limit) {
        return checkInOutDAO.getRecent(limit);
    }

    /**
     * Thực hiện Check-in:
     * 1. Cập nhật TrangThai DatPhong -> "DangO"
     * 2. Cập nhật TrangThai Phong -> "DangThue"
     * 3. Ghi log CheckInOut (loai = "CheckIn")
     *
     * @param maDatPhong  ID đặt phòng.
     * @param maPhong     ID phòng.
     * @param maNV        ID nhân viên đang đăng nhập.
     * @return true nếu tất cả thao tác thành công.
     */
    public boolean doCheckIn(int maDatPhong, int maPhong, int maNV) {
        int r1 = datPhongDAO.updateTrangThai(maDatPhong, "DangO");
        int r2 = phongDAO.updateTrangThai(maPhong, "DangThue");
        long r3 = checkInOutDAO.insertLog(maDatPhong, maNV, "CheckIn");
        return r1 > 0 && r2 > 0 && r3 > 0;
    }

    /**
     * Thực hiện Check-out:
     * 1. Cập nhật TrangThai DatPhong -> "DaTraPhong"
     * 2. Cập nhật TrangThai Phong -> "Trong"
     * 3. Ghi log CheckInOut (loai = "CheckOut")
     * 4. Tăng SoLanThue của KhachHang lên 1
     *
     * @param maDatPhong  ID đặt phòng.
     * @param maPhong     ID phòng.
     * @param maKH        ID khách hàng.
     * @param maNV        ID nhân viên đang đăng nhập.
     * @return true nếu tất cả thao tác thành công.
     */
    public boolean doCheckOut(int maDatPhong, int maPhong, int maKH, int maNV) {
        int r1 = datPhongDAO.updateTrangThai(maDatPhong, "DaTraPhong");
        int r2 = phongDAO.updateTrangThai(maPhong, "Trong");
        long r3 = checkInOutDAO.insertLog(maDatPhong, maNV, "CheckOut");
        khachHangDAO.incrementSoLanThue(maKH);
        return r1 > 0 && r2 > 0 && r3 > 0;
    }

    /**
     * Đặt phòng mới:
     * insert DatPhong + cập nhật TrangThai Phong -> "DaDat"
     *
     * @return MaDatPhong vừa tạo, -1 nếu lỗi.
     */
    public long createBooking(DatPhong dp) {
        long maDatPhong = datPhongDAO.insert(dp);
        if (maDatPhong > 0) {
            phongDAO.updateTrangThai(dp.getMaPhong(), "DaDat");
        }
        return maDatPhong;
    }
}
