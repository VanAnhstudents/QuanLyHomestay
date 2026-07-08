package com.example.ql_homestay.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.repository.PermissionRepository.PermissionRow;

import java.util.ArrayList;
import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.VH> {
    private List<PermissionRow> data = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<PermissionRow> list) {
        data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_permission_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PermissionRow row = data.get(pos);
        h.tvName.setText(mapModuleName(row.module.getTenModule()));
        h.tvDesc.setText(mapModuleDesc(row.module.getTenModule()));
        h.tvQuyen.setText(quyenValueToDisplay(row.tenQuyen));
        h.ivIcon.setImageResource(mapModuleIcon(row.module.getTenModule()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static String quyenValueToDisplay(String tenQuyen) {
        if (tenQuyen == null) return "Không truy cập";
        switch (tenQuyen) {
            case "ToanQuyen": return "To\u00e0n quy\u1ec1n";
            case "XemVaTao": return "Xem v\u00e0 T\u1ea1o";
            case "ChiXem": return "Ch\u1ec9 xem";
            case "KhongTruyCap": return "Kh\u00f4ng truy c\u1eadp";
            default: return "Kh\u00f4ng truy c\u1eadp";
        }
    }

    private static String mapModuleName(String tenModule) {
        if (tenModule == null) return "";
        switch (tenModule) {
            case "TrangChu": return "Trang ch\u1ee7 / Dashboard";
            case "QuanLyPhong": return "Qu\u1ea3n l\u00fd ph\u00f2ng";
            case "QuanLyDatPhong": return "Qu\u1ea3n l\u00fd \u0111\u1eb7t ph\u00f2ng";
            case "QuanLyKhachHang": return "Qu\u1ea3n l\u00fd kh\u00e1ch h\u00e0ng";
            case "HoaDonThanhToan": return "H\u00f3a \u0111\u01a1n & Thanh to\u00e1n";
            case "QuanLyNhanVien": return "Qu\u1ea3n l\u00fd nh\u00e2n vi\u00ean";
            case "BaoCaoThongKe": return "B\u00e1o c\u00e1o & Th\u1ed1ng k\u00ea";
            case "CaiDatHeThong": return "C\u00e0i \u0111\u1eb7t h\u1ec7 th\u1ed1ng";
            default: return tenModule;
        }
    }

    private static String mapModuleDesc(String tenModule) {
        if (tenModule == null) return "";
        switch (tenModule) {
            case "TrangChu": return "Xem t\u1ed5ng quan & KPI";
            case "QuanLyPhong": return "Th\u00eam/s\u1eeda/x\u00f3a ph\u00f2ng";
            case "QuanLyDatPhong": return "\u0110\u1eb7t ph\u00f2ng, check-in/out";
            case "QuanLyKhachHang": return "Th\u00f4ng tin kh\u00e1ch h\u00e0ng";
            case "HoaDonThanhToan": return "L\u1eadp v\u00e0 x\u00e1c nh\u1eadn h\u00f3a \u0111\u01a1n";
            case "QuanLyNhanVien": return "Nh\u00e2n vi\u00ean & ca l\u00e0m vi\u1ec7c";
            case "BaoCaoThongKe": return "Th\u1ed1ng k\u00ea doanh thu";
            case "CaiDatHeThong": return "T\u00e0i kho\u1ea3n & ph\u00e2n quy\u1ec1n";
            default: return "";
        }
    }

    private static int mapModuleIcon(String tenModule) {
        if (tenModule == null) return R.drawable.ic_settings;
        switch (tenModule) {
            case "TrangChu": return R.drawable.ic_home;
            case "QuanLyPhong": return R.drawable.ic_bed;
            case "QuanLyDatPhong": return R.drawable.ic_calendar;
            case "QuanLyKhachHang": return R.drawable.ic_person;
            case "HoaDonThanhToan": return R.drawable.ic_invoice;
            case "QuanLyNhanVien": return R.drawable.ic_staff;
            case "BaoCaoThongKe": return R.drawable.ic_chart;
            case "CaiDatHeThong": return R.drawable.ic_settings;
            default: return R.drawable.ic_settings;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName, tvDesc, tvQuyen;

        VH(View v) {
            super(v);
            ivIcon = v.findViewById(R.id.iv_module_icon);
            tvName = v.findViewById(R.id.tv_module_name);
            tvDesc = v.findViewById(R.id.tv_module_desc);
            tvQuyen = v.findViewById(R.id.tv_quyen);
        }
    }
}
