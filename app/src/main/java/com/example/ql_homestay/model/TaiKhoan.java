package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

/**
 * POJO ánh xạ bảng TaiKhoan trong SQLite.
 * Các trường khớp 1-1 với schema định nghĩa trong DatabaseHelper.
 */
public class TaiKhoan {

    private int maTK;
    private String tenDangNhap;
    private String email;
    private String matKhau;
    /** "Admin" | "LeTan" | "KeToan" | "NhanVien" */
    private String vaiTro;
    /** "HoatDong" | "Khoa" */
    private String trangThai;
    private String ngayTao;
    private String avatar;

    public TaiKhoan() {}

    public TaiKhoan(int maTK, String tenDangNhap, String email, String matKhau, String vaiTro, String trangThai, String ngayTao, String avatar) {
        this.maTK  = maTK;
        this.tenDangNhap = tenDangNhap;
        this.email = email;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.avatar = avatar;
    }

    public int getMaTK() {
        return maTK;
    }

    public void setMaTK(int maTK) {
        this.maTK = maTK;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // Helpers
    /** Trả về true nếu tài khoản đang hoạt động (không bị khóa). */
    public boolean isActive() {
        return "HoatDong".equals(trangThai);
    }

    @NonNull
    @Override
    public String toString() {
        return "TaiKhoan{maTK=" + maTK
                + ", tenDangNhap='" + tenDangNhap + '\''
                + ", vaiTro='" + vaiTro + '\''
                + ", trangThai='" + trangThai + '\'' + '}';
    }
}