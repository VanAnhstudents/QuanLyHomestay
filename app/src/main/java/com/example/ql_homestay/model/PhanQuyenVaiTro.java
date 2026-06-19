package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class PhanQuyenVaiTro {
    private int maPhanQuyen;
    /** "Admin" | "LeTan" | "KeToan" | "NhanVien" */
    private String maVaiTro;
    private int maModule;
    private int maQuyen;

    public PhanQuyenVaiTro() {}

    public PhanQuyenVaiTro(int maPhanQuyen, String maVaiTro, int maModule, int maQuyen) {
        this.maPhanQuyen = maPhanQuyen;
        this.maVaiTro = maVaiTro;
        this.maModule = maModule;
        this.maQuyen = maQuyen;
    }

    public int getMaPhanQuyen() { return maPhanQuyen; }
    public void setMaPhanQuyen(int maPhanQuyen) { this.maPhanQuyen = maPhanQuyen; }

    public String getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(String maVaiTro) { this.maVaiTro = maVaiTro; }

    public int getMaModule() { return maModule; }
    public void setMaModule(int maModule) { this.maModule = maModule; }

    public int getMaQuyen() { return maQuyen; }
    public void setMaQuyen(int maQuyen) { this.maQuyen = maQuyen; }

    @NonNull
    @Override
    public String toString() {
        return "PhanQuyenVaiTro{vaiTro='" + maVaiTro + "', module=" + maModule + ", quyen=" + maQuyen + "}";
    }
}