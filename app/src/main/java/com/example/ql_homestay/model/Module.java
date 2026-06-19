package com.example.ql_homestay.model;

import androidx.annotation.NonNull;

public class Module {
    private int maModule;
    private String tenModule;
    private String icon;

    public Module() {}

    public Module(int maModule, String tenModule, String icon) {
        this.maModule = maModule;
        this.tenModule = tenModule;
        this.icon = icon;
    }

    public int getMaModule() { return maModule; }
    public void setMaModule(int maModule) { this.maModule = maModule; }

    public String getTenModule() { return tenModule; }
    public void setTenModule(String tenModule) { this.tenModule = tenModule; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @NonNull
    @Override
    public String toString() {
        return "Module{maModule=" + maModule + ", tenModule='" + tenModule + "'}";
    }
}