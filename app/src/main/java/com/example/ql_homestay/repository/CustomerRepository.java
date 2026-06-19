package com.example.ql_homestay.repository;

import android.content.Context;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.KhachHangDAO;
import com.example.ql_homestay.model.KhachHang;

import java.util.List;

public class CustomerRepository {
    private final KhachHangDAO khachHangDAO;

    public CustomerRepository(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        this.khachHangDAO = new KhachHangDAO(dbHelper);
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

    /** Gọi khi 1 DatPhong của khách chuyển sang DaTraPhong (sẽ nối với module Đặt phòng sau). */
    public void increaseRentalCount(int maKH) {
        khachHangDAO.incrementSoLanThue(maKH);
    }
}