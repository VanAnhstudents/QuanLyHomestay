package com.example.ql_homestay.model;

/**
 * Model POJO cho bảng Phong.
 * TrangThai: "Trong" | "DangThue" | "DaDat"
 */
public class Phong {
    private int maPhong;
    private int maLoaiPhong;
    private String tenPhong;
    private double giaMoiDem;
    private int sucChua;
    private double dienTich;
    private int tang;
    private String trangThai;
    private String hinhAnh;
    private String moTa;

    // Joined fields (không có trong bảng, dùng trong JOIN)
    private String tenLoaiPhong;

    public Phong() {}

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }

    public int getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(int maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public double getGiaMoiDem() { return giaMoiDem; }
    public void setGiaMoiDem(double giaMoiDem) { this.giaMoiDem = giaMoiDem; }

    public int getSucChua() { return sucChua; }
    public void setSucChua(int sucChua) { this.sucChua = sucChua; }

    public double getDienTich() { return dienTich; }
    public void setDienTich(double dienTich) { this.dienTich = dienTich; }

    public int getTang() { return tang; }
    public void setTang(int tang) { this.tang = tang; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }
}
