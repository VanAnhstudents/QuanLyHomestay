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
import com.example.ql_homestay.model.KhachHang;
import com.example.ql_homestay.util.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomerAdapter — RecyclerView.Adapter cho danh sách KhachHang ở CustomerListFragment.
 * Item layout: item_customer_row.xml (avatar initials + tên + SĐT + email + chevron).*/
public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    /** Callback khi người dùng nhấn vào 1 dòng khách hàng. */
    public interface OnCustomerClickListener {
        void onCustomerClick(KhachHang khachHang);
    }

    private final List<KhachHang> danhSach = new ArrayList<>();
    private final OnCustomerClickListener listener;

    public CustomerAdapter(OnCustomerClickListener listener) {
        this.listener = listener;
    }

    /** Thay toàn bộ dữ liệu hiển thị (gọi sau khi load/search xong, trên Main Thread). */
    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<KhachHang> newData) {
        danhSach.clear();
        if (newData != null) danhSach.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_row, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        KhachHang kh = danhSach.get(position);
        AvatarHelper.loadAvatar(holder.itemView.getContext(), kh.getAvatar(), kh.getHoTen(), 
                               holder.ivAvatar, holder.tvAvatarInitials);
        holder.tvHoTen.setText(kh.getHoTen());
        holder.tvSdt.setText(kh.getSdt() != null && !kh.getSdt().isEmpty() ? kh.getSdt() : "—");
        holder.tvEmail.setText(kh.getEmail() != null && !kh.getEmail().isEmpty() ? kh.getEmail() : "—");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCustomerClick(kh);
        });
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvAvatarInitials;
        TextView tvHoTen;
        TextView tvSdt;
        TextView tvEmail;

        CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAvatarInitials = itemView.findViewById(R.id.tv_avatar_initials);
            tvHoTen = itemView.findViewById(R.id.tv_ho_ten);
            tvSdt = itemView.findViewById(R.id.tv_sdt);
            tvEmail = itemView.findViewById(R.id.tv_email);
        }
    }
}