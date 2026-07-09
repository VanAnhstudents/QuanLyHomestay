package com.example.ql_homestay.repository;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.LoaiPhongDAO;
import com.example.ql_homestay.data.dao.PhongDAO;
import com.example.ql_homestay.data.dao.PhongTienNghiDAO;
import com.example.ql_homestay.data.dao.TienNghiDAO;
import com.example.ql_homestay.model.LoaiPhong;
import com.example.ql_homestay.model.Phong;
import com.example.ql_homestay.model.TienNghi;

import java.util.List;

/**
 * Repository tầng trung gian cho module Quản lý Phòng.
 * Tổng hợp các DAO: PhongDAO, LoaiPhongDAO, PhongTienNghiDAO, TienNghiDAO.
 */
public class RoomRepository {
    private final PhongDAO phongDAO;
    private final LoaiPhongDAO loaiPhongDAO;
    private final PhongTienNghiDAO phongTienNghiDAO;
    private final TienNghiDAO tienNghiDAO;

    public RoomRepository(DatabaseHelper dbHelper) {
        this.phongDAO = new PhongDAO(dbHelper);
        this.loaiPhongDAO = new LoaiPhongDAO(dbHelper);
        this.phongTienNghiDAO = new PhongTienNghiDAO(dbHelper);
        this.tienNghiDAO = new TienNghiDAO(dbHelper);
    }

    // Phong
    public List<Phong> getAllPhong() { return phongDAO.getAll(); }

    public List<Phong> filterByTrangThai(String trangThai) {
        return phongDAO.filterByTrangThai(trangThai);
    }

    public List<Phong> searchPhong(String keyword) { return phongDAO.search(keyword); }

    public List<Phong> getAvailablePhong() { return phongDAO.getAvailable(); }

    public Phong findPhongById(int maPhong) { return phongDAO.findById(maPhong); }

    public long insertPhong(Phong phong) { return phongDAO.insert(phong); }

    public int updatePhong(Phong phong) { return phongDAO.update(phong); }

    public int deletePhong(int maPhong) { return phongDAO.delete(maPhong); }

    public int updateTrangThaiPhong(int maPhong, String trangThai) {
        return phongDAO.updateTrangThai(maPhong, trangThai);
    }

    public int countPhongByTrangThai(String trangThai) {
        return phongDAO.countByTrangThai(trangThai);
    }

    public int countAllPhong() { return phongDAO.countAll(); }

    public boolean isTenPhongDuplicate(String tenPhong, int excludeMaPhong) {
        return phongDAO.isTenPhongDuplicate(tenPhong, excludeMaPhong);
    }

    // LoaiPhong
    public List<LoaiPhong> getAllLoaiPhong() { return loaiPhongDAO.getAll(); }

    public LoaiPhong findLoaiPhongById(int maLoaiPhong) {
        return loaiPhongDAO.findById(maLoaiPhong);
    }

    // TienNghi
    public List<TienNghi> getAllTienNghi() { return tienNghiDAO.getAll(); }

    // PhongTienNghi
    public List<TienNghi> getTienNghiByPhong(int maPhong) {
        return phongTienNghiDAO.getByPhong(maPhong);
    }

    public void replaceTienNghi(int maPhong, List<Integer> maTienNghiList) {
        phongTienNghiDAO.replaceAll(maPhong, maTienNghiList);
    }

    /**
     * Lưu phòng mới cùng tiện nghi trong một thao tác.
     * @return MaPhong vừa tạo, -1 nếu lỗi.
     */
    public long saveNewPhong(Phong phong, List<Integer> maTienNghiList) {
        long maPhong = phongDAO.insert(phong);
        if (maPhong > 0) {
            phongTienNghiDAO.replaceAll((int) maPhong, maTienNghiList);
        }
        return maPhong;
    }

    /**
     * Cập nhật phòng cùng tiện nghi trong một thao tác.
     * @return số dòng bị ảnh hưởng.
     */
    public int saveEditPhong(Phong phong, List<Integer> maTienNghiList) {
        int rows = phongDAO.update(phong);
        if (rows > 0) {
            phongTienNghiDAO.replaceAll(phong.getMaPhong(), maTienNghiList);
        }
        return rows;
    }
}
