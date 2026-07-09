package com.example.ql_homestay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.DatPhong;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView danh sách Đặt phòng (C1).
 * Item layout: item_booking_card.xml
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(DatPhong datPhong);
    }

    private final Context context;
    private List<DatPhong> dataList;
    private OnBookingClickListener listener;

    public BookingAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    // Cập nhật toàn bộ danh sách đặt phòng và notify UI.
    public void setData(List<DatPhong> list) {
        this.dataList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_booking_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapter.ViewHolder holder, int position) {
        DatPhong dp = dataList.get(position);

        // Hàng trên: mã + ngày tạo
        holder.tvMaDatPhong.setText("ĐP #" + dp.getMaDatPhong());
        holder.tvNgay.setText(dp.getNgayCheckIn() != null ? dp.getNgayCheckIn() : "");

        // Hàng giữa: tên khách + tên phòng
        holder.tvTenKhach.setText(dp.getTenKhachHang() != null ? dp.getTenKhachHang() : "Khách hàng");
        holder.tvTenPhong.setText(dp.getTenPhong() != null ? dp.getTenPhong() : "");

        // Hàng dưới: CI → CO + badge
        String period = "";
        if (dp.getNgayCheckIn() != null && dp.getNgayCheckOut() != null) {
            period = dp.getNgayCheckIn() + " → " + dp.getNgayCheckOut();
        }
        holder.tvPeriod.setText(period);

        // Badge trạng thái
        holder.tvTrangThai.setText(getTrangThaiLabel(dp.getTrangThai()));
        applyBadgeStyle(holder.tvTrangThai, dp.getTrangThai());

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBookingClick(dp);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    // HELPERS
    private String getTrangThaiLabel(String trangThai) {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "SapDen": return "Sắp đến";
            case "DangO": return "Đang ở";
            case "DaTraPhong": return "Đã trả phòng";
            case "DaHuy": return "Đã hủy";
            default: return trangThai;
        }
    }

    private void applyBadgeStyle(TextView tv, String trangThai) {
        if (trangThai == null) return;
        switch (trangThai) {
            case "SapDen":
                tv.setBackgroundResource(R.drawable.bg_badge_dadat);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dadat_text, null));
                break;
            case "DangO":
                tv.setBackgroundResource(R.drawable.bg_badge_dangthue);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dangthue_text, null));
                break;
            case "DaTraPhong":
                tv.setBackgroundResource(R.drawable.bg_badge_dathanhtoan);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dathanhtoan_text, null));
                break;
            case "DaHuy":
                tv.setBackgroundResource(R.drawable.bg_badge_dahuy);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dahuy_text, null));
                break;
        }
    }

    // VIEW HOLDER
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMaDatPhong;
        final TextView tvNgay;
        final TextView tvTenKhach;
        final TextView tvTenPhong;
        final TextView tvPeriod;
        final TextView tvTrangThai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDatPhong = itemView.findViewById(R.id.tv_booking_id);
            tvNgay = itemView.findViewById(R.id.tv_booking_date);
            tvTenKhach = itemView.findViewById(R.id.tv_booking_customer);
            tvTenPhong = itemView.findViewById(R.id.tv_booking_room);
            tvPeriod = itemView.findViewById(R.id.tv_booking_period);
            tvTrangThai = itemView.findViewById(R.id.tv_booking_status);
        }
    }
}
