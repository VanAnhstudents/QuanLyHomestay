package com.example.ql_homestay.model;

/**
 * Model POJO cho bảng HoaDon.
 * TrangThai: "DaThanhToan" | "ChuaThanhToan" | "HoanTien"
 */
public class HoaDon {
    private int maHD;
    private int maDatPhong;
    private String ngayLap;
    private double tienPhong;
    private double phuThuDichVu;
    private double giamGia;
    private double tongCong;
    /** "DaThanhToan" | "ChuaThanhToan" | "HoanTien" */
    private String trangThai;
    private String phuongThucTT;
    private String ngayTT;
    private int maNV;

    // Joined fields (populate bởi JOIN query – không lưu trong bảng HoaDon)
    private String tenPhong;
    private String tenKhachHang;
    private String ngayCheckIn;
    private String ngayCheckOut;
    private int soDem;
    private String tenNhanVien;

    public HoaDon() {}

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }

    public int getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(int maDatPhong) { this.maDatPhong = maDatPhong; }

    public String getNgayLap() { return ngayLap; }
    public void setNgayLap(String ngayLap) { this.ngayLap = ngayLap; }

    public double getTienPhong() { return tienPhong; }
    public void setTienPhong(double tienPhong) { this.tienPhong = tienPhong; }

    public double getPhuThuDichVu() { return phuThuDichVu; }
    public void setPhuThuDichVu(double phuThuDichVu) { this.phuThuDichVu = phuThuDichVu; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public double getTongCong() { return tongCong; }
    public void setTongCong(double tongCong) { this.tongCong = tongCong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }

    public String getNgayTT() { return ngayTT; }
    public void setNgayTT(String ngayTT) { this.ngayTT = ngayTT; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    // Joined fields
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getNgayCheckIn() { return ngayCheckIn; }
    public void setNgayCheckIn(String ngayCheckIn) { this.ngayCheckIn = ngayCheckIn; }

    public String getNgayCheckOut() { return ngayCheckOut; }
    public void setNgayCheckOut(String ngayCheckOut) { this.ngayCheckOut = ngayCheckOut; }

    public int getSoDem() { return soDem; }
    public void setSoDem(int soDem) { this.soDem = soDem; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    /** Label hiển thị trạng thái tiếng Việt */
    public String getTrangThaiLabel() {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "DaThanhToan":   return "Đã thanh toán";
            case "ChuaThanhToan": return "Chưa thanh toán";
            case "HoanTien":      return "Hoàn tiền";
            default:              return trangThai;
        }
    }
}
