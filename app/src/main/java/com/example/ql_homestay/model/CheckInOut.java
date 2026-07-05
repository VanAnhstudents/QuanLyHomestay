package com.example.ql_homestay.model;

/**
 * Model POJO cho bảng CheckInOut.
 * Loai: "CheckIn" | "CheckOut"
 */
public class CheckInOut {
    private int maCheckLog;
    private int maDatPhong;
    private int maNV;
    /** "CheckIn" | "CheckOut" */
    private String loai;
    private String thoiGian;
    private String ghiChuDacBiet;

    // Joined fields
    private String tenNhanVien;
    private String tenPhong;
    private String tenKhachHang;

    public CheckInOut() {}

    public int getMaCheckLog() { return maCheckLog; }
    public void setMaCheckLog(int maCheckLog) { this.maCheckLog = maCheckLog; }

    public int getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(int maDatPhong) { this.maDatPhong = maDatPhong; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public String getGhiChuDacBiet() { return ghiChuDacBiet; }
    public void setGhiChuDacBiet(String ghiChuDacBiet) { this.ghiChuDacBiet = ghiChuDacBiet; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
}
