package com.example.ql_homestay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.Phong;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView danh sách Phòng (B1).
 * Item layout: item_room_card.xml
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(Phong phong);
    }

    private final Context context;
    private List<Phong> dataList;
    private OnRoomClickListener listener;

    public RoomAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

    // Cập nhật toàn bộ danh sách phòng và notify UI.
    public void setData(List<Phong> list) {
        this.dataList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_room_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Phong phong = dataList.get(position);

        // Tên phòng
        holder.tvTenPhong.setText(phong.getTenPhong());

        // Loại phòng
        String loai = phong.getTenLoaiPhong() != null ? phong.getTenLoaiPhong() : "";
        holder.tvLoaiPhong.setText(loai);

        // Giá/đêm
        String gia = NumberFormat.getNumberInstance(Locale.getDefault())
                .format((long) phong.getGiaMoiDem()) + " đ/đêm";
        holder.tvGia.setText(gia);

        // Badge trạng thái
        holder.tvTrangThai.setText(getTrangThaiLabel(phong.getTrangThai()));
        applyBadgeStyle(holder.tvTrangThai, phong.getTrangThai());

        // Hình ảnh phòng
        setRoomImage(holder.ivHinhAnh, phong.getHinhAnh());

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRoomClick(phong);
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
            case "Trong": return "Trống";
            case "DangThue": return "Đang thuê";
            case "DaDat": return "Đã đặt";
            default: return trangThai;
        }
    }

    private void applyBadgeStyle(TextView tv, String trangThai) {
        if (trangThai == null) return;
        switch (trangThai) {
            case "Trong":
                tv.setBackgroundResource(R.drawable.bg_badge_trong);
                tv.setTextColor(context.getResources().getColor(R.color.badge_trong_text, null));
                break;
            case "DangThue":
                tv.setBackgroundResource(R.drawable.bg_badge_dangthue);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dangthue_text, null));
                break;
            case "DaDat":
                tv.setBackgroundResource(R.drawable.bg_badge_dadat);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dadat_text, null));
                break;
        }
    }

    private void setRoomImage(ImageView iv, String hinhAnh) {
        if (hinhAnh == null || hinhAnh.isEmpty()) {
            iv.setImageResource(R.drawable.room_standard);
            return;
        }
        // Thử load từ URI người dùng chọn (content:// hoặc file://)
        if (hinhAnh.startsWith("content://") || hinhAnh.startsWith("file://")) {
            try {
                iv.setImageURI(android.net.Uri.parse(hinhAnh));
                if (iv.getDrawable() != null) return;
            } catch (Exception ignored) {}
        }
        // Fallback: map tên drawable tĩnh
        switch (hinhAnh) {
            case "room_deluxe": iv.setImageResource(R.drawable.room_deluxe);
            break;
            case "room_deluxe_top": iv.setImageResource(R.drawable.room_deluxe_top);
            break;
            case "room_suite": iv.setImageResource(R.drawable.room_suite);
            break;
            default: iv.setImageResource(R.drawable.room_standard);
            break;
        }
    }


    // VIEW HOLDER
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivHinhAnh;
        final TextView tvTenPhong;
        final TextView tvLoaiPhong;
        final TextView tvGia;
        final TextView tvTrangThai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHinhAnh = itemView.findViewById(R.id.iv_room_image);
            tvTenPhong = itemView.findViewById(R.id.tv_room_name);
            tvLoaiPhong = itemView.findViewById(R.id.tv_room_type);
            tvGia = itemView.findViewById(R.id.tv_room_price);
            tvTrangThai = itemView.findViewById(R.id.tv_room_status);
        }
    }
}
