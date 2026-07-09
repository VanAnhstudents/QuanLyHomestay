package com.example.ql_homestay.model;

/**
 * Model POJO cho bảng LoaiPhong.
 * TenLoai: "Standard" | "Deluxe" | "Suite"
 */
public class LoaiPhong {
    private int maLoaiPhong;
    private String tenLoai;
    private double giaCoBan;

    public LoaiPhong() {}

    public LoaiPhong(int maLoaiPhong, String tenLoai, double giaCoBan) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoai = tenLoai;
        this.giaCoBan = giaCoBan;
    }

    public int getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(int maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }

    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }

    public double getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(double giaCoBan) { this.giaCoBan = giaCoBan; }

    @Override
    public String toString() {
        return tenLoai; // Dùng cho Spinner adapter
    }
}
