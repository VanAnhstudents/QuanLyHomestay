package com.example.ql_homestay.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.repository.PermissionRepository.PermissionRow;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * PermissionAdapter — hiển thị 8 dòng phân quyền module tại E2.
 * Mỗi dòng có icon module, tên module, mô tả và dropdown quyền.
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.VH> {

    private static final String[] QUYEN_DISPLAY = {"Toàn quyền", "Chỉ xem", "Không truy cập"};
    private static final String[] QUYEN_VALUE   = {"ToanQuyen", "ChiXem", "KhongTruyCap"};

    private List<PermissionRow> data = new ArrayList<>();
    private boolean isReadOnly = false; // true khi tab Admin được chọn

    public void setData(List<PermissionRow> list) {
        data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        notifyDataSetChanged();
    }

    /** Trả về map maModule → maQuyen từ dữ liệu hiện tại (dùng khi lưu). */
    public java.util.Map<Integer, Integer> getCurrentPermissions() {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        for (PermissionRow row : data) {
            map.put(row.module.getMaModule(), row.maQuyen);
        }
        return map;
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
        h.ivIcon.setImageResource(mapModuleIcon(row.module.getTenModule()));

        // Setup dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                h.itemView.getContext(),
                android.R.layout.simple_dropdown_item_1line,
                QUYEN_DISPLAY);
        h.dropdown.setAdapter(adapter);

        // Set current value
        String display = quyenValueToDisplay(row.tenQuyen);
        h.dropdown.setText(display, false);
        h.dropdown.setEnabled(!isReadOnly);

        h.dropdown.setOnItemClickListener((parent, v, position, id) -> {
            row.tenQuyen = QUYEN_DISPLAY[position];
            // Tìm maQuyen tương ứng — giả định PermissionRepository sẽ resolve lại khi lưu
            // Ở đây chỉ update tenQuyen local; Fragment sẽ gọi getCurrentPermissions()
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    private static String quyenValueToDisplay(String tenQuyen) {
        if (tenQuyen == null) return "Không truy cập";
        switch (tenQuyen) {
            case "ToanQuyen":       return "Toàn quyền";
            case "XemVaTao":        return "Toàn quyền"; // gộp về Toàn quyền cho UI đơn giản
            case "ChiXem":          return "Chỉ xem";
            case "KhongTruyCap":    return "Không truy cập";
            default:                return "Không truy cập";
        }
    }

    private static String mapModuleName(String tenModule) {
        if (tenModule == null) return "";
        switch (tenModule) {
            case "TrangChu":            return "Trang chủ / Dashboard";
            case "QuanLyPhong":         return "Quản lý phòng";
            case "QuanLyDatPhong":      return "Quản lý đặt phòng";
            case "QuanLyKhachHang":     return "Quản lý khách hàng";
            case "HoaDonThanhToan":     return "Hóa đơn & Thanh toán";
            case "QuanLyNhanVien":      return "Quản lý nhân viên";
            case "BaoCaoThongKe":       return "Báo cáo & Thống kê";
            case "CaiDatHeThong":       return "Cài đặt hệ thống";
            default:                    return tenModule;
        }
    }

    private static String mapModuleDesc(String tenModule) {
        if (tenModule == null) return "";
        switch (tenModule) {
            case "TrangChu":        return "Xem tổng quan & KPI";
            case "QuanLyPhong":     return "Thêm/sửa/xóa phòng";
            case "QuanLyDatPhong":  return "Đặt phòng, check-in/out";
            case "QuanLyKhachHang": return "Thông tin khách hàng";
            case "HoaDonThanhToan": return "Lập và xác nhận hóa đơn";
            case "QuanLyNhanVien":  return "Nhân viên & ca làm việc";
            case "BaoCaoThongKe":   return "Thống kê doanh thu";
            case "CaiDatHeThong":   return "Tài khoản & phân quyền";
            default:                return "";
        }
    }

    private static int mapModuleIcon(String tenModule) {
        if (tenModule == null) return R.drawable.ic_settings;
        switch (tenModule) {
            case "TrangChu":        return R.drawable.ic_home;
            case "QuanLyPhong":     return R.drawable.ic_bed;
            case "QuanLyDatPhong":  return R.drawable.ic_calendar;
            case "QuanLyKhachHang": return R.drawable.ic_person;
            case "HoaDonThanhToan": return R.drawable.ic_invoice;
            case "QuanLyNhanVien":  return R.drawable.ic_staff;
            case "BaoCaoThongKe":   return R.drawable.ic_chart;
            case "CaiDatHeThong":   return R.drawable.ic_settings;
            default:                return R.drawable.ic_settings;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName, tvDesc;
        final MaterialAutoCompleteTextView dropdown;
        VH(View v) {
            super(v);
            ivIcon   = v.findViewById(R.id.iv_module_icon);
            tvName   = v.findViewById(R.id.tv_module_name);
            tvDesc   = v.findViewById(R.id.tv_module_desc);
            dropdown = v.findViewById(R.id.dropdown_quyen);
        }
    }
}
