package com.example.ql_homestay.model;

/**
 * Model POJO cho bảng DatPhong.
 * TrangThai: "SapDen" | "DangO" | "DaTraPhong" | "DaHuy"
 */
public class DatPhong {
    private int maDatPhong;
    private int maKH;
    private int maPhong;
    private int maNV;
    private String ngayCheckIn;
    private String ngayCheckOut;
    private int soLuongKhach;
    private int soDem;
    /** "SapDen" | "DangO" | "DaTraPhong" | "DaHuy" */
    private String trangThai;
    private String phuongThucThanhToan;
    private String ghiChu;
    private String ngayTao;

    // Joined fields (không trong bảng, được populate bởi JOIN query)
    private String tenPhong;
    private String tenKhachHang;
    private double giaMoiDem;  // từ Phong, để tính tiền

    public DatPhong() {}

    public int getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(int maDatPhong) { this.maDatPhong = maDatPhong; }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getNgayCheckIn() { return ngayCheckIn; }
    public void setNgayCheckIn(String ngayCheckIn) { this.ngayCheckIn = ngayCheckIn; }

    public String getNgayCheckOut() { return ngayCheckOut; }
    public void setNgayCheckOut(String ngayCheckOut) { this.ngayCheckOut = ngayCheckOut; }

    public int getSoLuongKhach() { return soLuongKhach; }
    public void setSoLuongKhach(int soLuongKhach) { this.soLuongKhach = soLuongKhach; }

    public int getSoDem() { return soDem; }
    public void setSoDem(int soDem) { this.soDem = soDem; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public double getGiaMoiDem() { return giaMoiDem; }
    public void setGiaMoiDem(double giaMoiDem) { this.giaMoiDem = giaMoiDem; }

    /** Tính tổng tiền phòng = GiaMoiDem * SoDem */
    public double getTienPhong() {
        return giaMoiDem * soDem;
    }
}
