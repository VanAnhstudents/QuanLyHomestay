package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class CaLamViec {
    private int maCa;
    /** "Sang" | "Chieu" | "Toi" */
    private String tenCa;
    private String gioBatDau;
    private String gioKetThuc;

    public CaLamViec() {}

    public CaLamViec(int maCa, String tenCa, String gioBatDau, String gioKetThuc) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public int getMaCa() { return maCa; }
    public void setMaCa(int maCa) { this.maCa = maCa; }

    public String getTenCa() { return tenCa; }
    public void setTenCa(String tenCa) { this.tenCa = tenCa; }

    public String getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(String gioBatDau) { this.gioBatDau = gioBatDau; }

    public String getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(String gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    @NonNull
    @Override
    public String toString() {
        return "CaLamViec{maCa=" + maCa + ", tenCa='" + tenCa + "'}";
    }
}