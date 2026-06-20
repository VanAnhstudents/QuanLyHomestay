package com.example.ql_homestay.model;

public class DatPhong {
    private int maDatPhong;
    private int maKH;
    private int maPhong;
    private String ngayCheckIn;
    private String ngayCheckOut;
    private int soDem;
    /** "SapDen" | "DangO" | "DaTraPhong" | "DaHuy" */
    private String trangThai;
    // Joined fields (not in table, populated by JOIN queries)
    private String tenPhong;

    public DatPhong() {}

    public int getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(int maDatPhong) { this.maDatPhong = maDatPhong; }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }

    public String getNgayCheckIn() { return ngayCheckIn; }
    public void setNgayCheckIn(String ngayCheckIn) { this.ngayCheckIn = ngayCheckIn; }

    public String getNgayCheckOut() { return ngayCheckOut; }
    public void setNgayCheckOut(String ngayCheckOut) { this.ngayCheckOut = ngayCheckOut; }

    public int getSoDem() { return soDem; }
    public void setSoDem(int soDem) { this.soDem = soDem; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
}
