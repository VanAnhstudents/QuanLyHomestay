package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class PhanCongCa {
    private int maPhanCong;
    private int maNV;
    private int maCa;
    /** 1 = Thứ 2 ... 7 = Chủ nhật */
    private int thuTrongTuan;

    public PhanCongCa() {}

    public PhanCongCa(int maPhanCong, int maNV, int maCa, int thuTrongTuan) {
        this.maPhanCong = maPhanCong;
        this.maNV = maNV;
        this.maCa = maCa;
        this.thuTrongTuan = thuTrongTuan;
    }

    /** Constructor tiện dùng khi tạo dòng mới (chưa có MaPhanCong, để DB tự sinh). */
    public PhanCongCa(int maNV, int maCa, int thuTrongTuan) {
        this(0, maNV, maCa, thuTrongTuan);
    }

    public int getMaPhanCong() { return maPhanCong; }
    public void setMaPhanCong(int maPhanCong) { this.maPhanCong = maPhanCong; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public int getMaCa() { return maCa; }
    public void setMaCa(int maCa) { this.maCa = maCa; }

    public int getThuTrongTuan() { return thuTrongTuan; }
    public void setThuTrongTuan(int thuTrongTuan) { this.thuTrongTuan = thuTrongTuan; }

    @NonNull
    @Override
    public String toString() {
        return "PhanCongCa{maNV=" + maNV + ", maCa=" + maCa + ", thu=" + thuTrongTuan + "}";
    }
}