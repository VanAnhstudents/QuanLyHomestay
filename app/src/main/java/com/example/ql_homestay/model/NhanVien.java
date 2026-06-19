package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class NhanVien {
    private int maNV;
    /** Nullable — FK TaiKhoan, ON DELETE SET NULL */
    private Integer maTK;
    private String hoTen;
    /** "QuanLy" | "LeTan" | "KeToan" | "DonPhong" | "BaoVe" */
    private String chucVu;
    private String sdt;
    private String email;
    private String cccd;
    private String diaChi;
    private String ngayVaoLam;
    private String avatar;

    public NhanVien() {}

    public NhanVien(int maNV, Integer maTK, String hoTen, String chucVu, String sdt,
                    String email, String cccd, String diaChi, String ngayVaoLam, String avatar) {
        this.maNV = maNV;
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
        this.sdt = sdt;
        this.email = email;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.ngayVaoLam = ngayVaoLam;
        this.avatar = avatar;
    }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public Integer getMaTK() { return maTK; }
    public void setMaTK(Integer maTK) { this.maTK = maTK; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getNgayVaoLam() { return ngayVaoLam; }
    public void setNgayVaoLam(String ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    @NonNull
    @Override
    public String toString() {
        return "NhanVien{maNV=" + maNV + ", hoTen='" + hoTen + "', chucVu='" + chucVu + "'}";
    }
}