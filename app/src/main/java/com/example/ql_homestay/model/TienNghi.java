package com.example.ql_homestay.model;

public class TienNghi {
    private int maTienNghi;
    private String tenTienNghi;

    public TienNghi() {}

    public TienNghi(int maTienNghi, String tenTienNghi) {
        this.maTienNghi = maTienNghi;
        this.tenTienNghi = tenTienNghi;
    }

    public int getMaTienNghi() { return maTienNghi; }
    public void setMaTienNghi(int maTienNghi) { this.maTienNghi = maTienNghi; }

    public String getTenTienNghi() { return tenTienNghi; }
    public void setTenTienNghi(String tenTienNghi) { this.tenTienNghi = tenTienNghi; }

    @Override
    public String toString() {
        return tenTienNghi;
    }
}
