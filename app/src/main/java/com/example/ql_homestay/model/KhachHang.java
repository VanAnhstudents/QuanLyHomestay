package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class KhachHang {
    private int maKH;
    private String hoTen;
    private String sdt;
    private String email;
    private String cccd;
    private String diaChi;
    private String ngaySinh;
    /** "Nam" | "Nu" | "Khac" */
    private String gioiTinh;
    private String avatar;
    private int soLanThue;

    public KhachHang() {}

    public KhachHang(int maKH, String hoTen, String sdt, String email, String cccd,
                     String diaChi, String ngaySinh, String gioiTinh, String avatar, int soLanThue) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.avatar = avatar;
        this.soLanThue = soLanThue;
    }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getSoLanThue() { return soLanThue; }
    public void setSoLanThue(int soLanThue) { this.soLanThue = soLanThue; }

    /** Chữ viết tắt từ HoTen — dùng làm avatar fallback khi khách không có ảnh (xem item_customer_row.xml). */
    public String getInitials() {
        if (hoTen == null || hoTen.trim().isEmpty()) return "?";
        String[] parts = hoTen.trim().split("\\s+");
        return parts[parts.length - 1].substring(0, 1).toUpperCase();
    }

    @NonNull
    @Override
    public String toString() {
        return "KhachHang{maKH=" + maKH + ", hoTen='" + hoTen + "'}";
    }
}