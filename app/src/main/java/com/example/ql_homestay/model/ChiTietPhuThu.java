package com.example.ql_homestay.model;

/**
 * Model POJO cho bảng ChiTietPhuThu.
 * Mỗi bản ghi là một dòng phụ thu của một hóa đơn.
 */
public class ChiTietPhuThu {
    private int maChiTiet;
    private int maHD;
    private String tenPhuThu;
    private double soTien;

    public ChiTietPhuThu() {}

    public ChiTietPhuThu(int maHD, String tenPhuThu, double soTien) {
        this.maHD = maHD;
        this.tenPhuThu = tenPhuThu;
        this.soTien = soTien;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(int maChiTiet) { this.maChiTiet = maChiTiet; }

    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }

    public String getTenPhuThu() { return tenPhuThu; }
    public void setTenPhuThu(String tenPhuThu) { this.tenPhuThu = tenPhuThu; }

    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }
}
