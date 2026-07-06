package com.example.ql_homestay.repository;

import android.content.Context;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.DatPhongDAO;
import com.example.ql_homestay.data.dao.KhachHangDAO;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.model.KhachHang;

import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private final KhachHangDAO khachHangDAO;
    private final DatPhongDAO datPhongDAO;

    public CustomerRepository(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        this.khachHangDAO = new KhachHangDAO(dbHelper);
        this.datPhongDAO = new DatPhongDAO(dbHelper);
    }

    public List<KhachHang> getAllCustomers() {
        return khachHangDAO.getAll();
    }

    public List<KhachHang> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return khachHangDAO.getAll();
        return khachHangDAO.search(keyword.trim());
    }

    public KhachHang getCustomerById(int maKH) {
        return khachHangDAO.findById(maKH);
    }

    public long addCustomer(KhachHang kh) {
        return khachHangDAO.insert(kh);
    }

    public int updateCustomer(KhachHang kh) {
        return khachHangDAO.update(kh);
    }

    public int deleteCustomer(int maKH) {
        return khachHangDAO.delete(maKH);
    }

    public void increaseRentalCount(int maKH) {
        khachHangDAO.incrementSoLanThue(maKH);
    }

    public List<DatPhong> getRecentBookings(int maKH) {
        List<DatPhong> all = getAllBookings(maKH);
        return all.size() > 3 ? new ArrayList<>(all.subList(0, 3)) : all;
    }

    public List<DatPhong> getAllBookings(int maKH) {
        List<DatPhong> stayed = new ArrayList<>();
        for (DatPhong dp : datPhongDAO.getByKhachHang(maKH)) {
            if ("DaTraPhong".equals(dp.getTrangThai())) stayed.add(dp);
        }
        return stayed;
    }

    public int getStayCount(int maKH) {
        return getAllBookings(maKH).size();
    }
}
