package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class Quyen {
    private int maQuyen;
    /** "ToanQuyen" | "ChiXem" | "XemVaTao" | "KhongTruyCap" */
    private String tenQuyen;

    public Quyen() {}

    public Quyen(int maQuyen, String tenQuyen) {
        this.maQuyen = maQuyen;
        this.tenQuyen = tenQuyen;
    }

    public int getMaQuyen() { return maQuyen; }
    public void setMaQuyen(int maQuyen) { this.maQuyen = maQuyen; }

    public String getTenQuyen() { return tenQuyen; }
    public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }

    @NonNull
    @Override
    public String toString() {
        return "Quyen{maQuyen=" + maQuyen + ", tenQuyen='" + tenQuyen + "'}";
    }
}