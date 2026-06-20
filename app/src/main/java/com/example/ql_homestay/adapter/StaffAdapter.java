package com.example.ql_homestay.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.NhanVien;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(NhanVien nhanVien);
    }

    private List<NhanVien> data = new ArrayList<>();
    private final OnItemClickListener listener;

    public StaffAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<NhanVien> list) {
        data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        NhanVien nv = data.get(pos);
        h.tvTen.setText(nv.getHoTen());
        h.tvChucVu.setText(mapChucVu(nv.getChucVu()));
        h.tvSdt.setText(nv.getSdt() != null ? nv.getSdt() : "");
        // Avatar initials: lấy ký tự cuối cùng trong HoTen
        String initials = "?";
        if (nv.getHoTen() != null && !nv.getHoTen().trim().isEmpty()) {
            String[] parts = nv.getHoTen().trim().split("\\s+");
            initials = parts[parts.length - 1].substring(0, 1).toUpperCase();
        }
        h.tvInitials.setText(initials);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(nv);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    /** Ánh xạ giá trị DB → hiển thị tiếng Việt. */
    private static String mapChucVu(String chucVu) {
        if (chucVu == null) return "";
        switch (chucVu) {
            case "QuanLy": return "Quản lý";
            case "LeTan": return "Lễ tân";
            case "KeToan": return "Kế toán";
            case "DonPhong": return "Dọn phòng";
            case "BaoVe": return "Bảo vệ";
            default: return chucVu;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvInitials, tvTen, tvChucVu, tvSdt;
        VH(View v) {
            super(v);
            tvInitials = v.findViewById(R.id.tv_initials);
            tvTen = v.findViewById(R.id.tv_ten_nhan_vien);
            tvChucVu = v.findViewById(R.id.tv_chuc_vu);
            tvSdt = v.findViewById(R.id.tv_sdt);
        }
    }
}
