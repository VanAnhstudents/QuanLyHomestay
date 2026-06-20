package com.example.ql_homestay.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.VH> {
    public interface OnActionListener {
        void onItemClick(TaiKhoan tk);
        void onEdit(TaiKhoan tk);
        void onToggleLock(TaiKhoan tk);
        void onDelete(TaiKhoan tk);
    }

    private List<TaiKhoan> data = new ArrayList<>();
    private final OnActionListener listener;

    public AccountAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<TaiKhoan> list) {
        data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TaiKhoan tk = data.get(pos);
        h.tvTen.setText(tk.getTenDangNhap());
        h.tvEmail.setText(tk.getEmail() != null ? tk.getEmail() : "");
        h.tvBadgeRole.setText(mapVaiTro(tk.getVaiTro()));

        // Avatar initials
        String initials = "?";
        if (tk.getTenDangNhap() != null && !tk.getTenDangNhap().trim().isEmpty()) {
            initials = tk.getTenDangNhap().substring(0, 1).toUpperCase();
        }
        h.tvInitials.setText(initials);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(tk);
        });

        h.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 1, 0, "Sửa");
            String lockLabel = "HoatDong".equals(tk.getTrangThai()) ? "Khóa tài khoản" : "Mở khóa";
            popup.getMenu().add(0, 2, 1, lockLabel);
            popup.getMenu().add(0, 3, 2, "Xóa");
            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;
                switch (item.getItemId()) {
                    case 1: listener.onEdit(tk); return true;
                    case 2: listener.onToggleLock(tk); return true;
                    case 3: listener.onDelete(tk); return true;
                    default: return false;
                }
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    private static String mapVaiTro(String vaiTro) {
        if (vaiTro == null) return "";
        switch (vaiTro) {
            case "Admin": return "Admin";
            case "LeTan": return "Lễ tân";
            case "KeToan": return "Kế toán";
            case "NhanVien": return "Nhân viên";
            default: return vaiTro;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvInitials, tvTen, tvEmail, tvBadgeRole;
        final ImageButton btnMore;
        VH(View v) {
            super(v);
            tvInitials = v.findViewById(R.id.tv_initials);
            tvTen = v.findViewById(R.id.tv_ten_tai_khoan);
            tvEmail = v.findViewById(R.id.tv_email);
            tvBadgeRole = v.findViewById(R.id.tv_badge_role);
            btnMore = v.findViewById(R.id.btn_more);
        }
    }
}
