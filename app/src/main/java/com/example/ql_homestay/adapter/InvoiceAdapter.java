package com.example.ql_homestay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.HoaDon;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView danh sách Hóa đơn (G1).
 * Item layout: item_invoice_row.xml
 */
public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {

    public interface OnInvoiceClickListener {
        void onInvoiceClick(HoaDon hoaDon);
    }

    private final Context context;
    private List<HoaDon> dataList;
    private OnInvoiceClickListener listener;

    public InvoiceAdapter(Context context) {
        this.context  = context;
        this.dataList = new ArrayList<>();
    }

    public void setOnInvoiceClickListener(OnInvoiceClickListener listener) {
        this.listener = listener;
    }

    /** Cập nhật toàn bộ danh sách và notify UI. */
    public void setData(List<HoaDon> list) {
        this.dataList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_invoice_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HoaDon hd = dataList.get(position);

        // Mã HĐ
        holder.tvMaHD.setText("HĐ #" + String.format(Locale.getDefault(), "%03d", hd.getMaHD()));

        // Ngày lập
        holder.tvNgayLap.setText(formatDate(hd.getNgayLap()));

        // Tên khách hàng
        String tenKhach = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "—";
        holder.tvTenKhach.setText(tenKhach);

        // Tên phòng
        String tenPhong = hd.getTenPhong() != null ? hd.getTenPhong() : "—";
        holder.tvTenPhong.setText(tenPhong);

        // Tổng cộng
        String tongCong = NumberFormat.getNumberInstance(Locale.getDefault())
                .format((long) hd.getTongCong()) + " đ";
        holder.tvTongCong.setText(tongCong);

        // Badge trạng thái
        holder.tvTrangThai.setText(getTrangThaiLabel(hd.getTrangThai()));
        applyBadgeStyle(holder.tvTrangThai, hd.getTrangThai());

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onInvoiceClick(hd);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String getTrangThaiLabel(String trangThai) {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "DaThanhToan":   return "Đã TT";
            case "ChuaThanhToan": return "Chưa TT";
            case "HoanTien":      return "Hoàn tiền";
            default:              return trangThai;
        }
    }

    private void applyBadgeStyle(TextView tv, String trangThai) {
        if (trangThai == null) return;
        switch (trangThai) {
            case "DaThanhToan":
                tv.setBackgroundResource(R.drawable.bg_badge_dathanhtoan);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dathanhtoan_text, null));
                break;
            case "ChuaThanhToan":
                tv.setBackgroundResource(R.drawable.bg_badge_chuathanhtoan);
                tv.setTextColor(context.getResources().getColor(R.color.badge_chuathanhtoan_text, null));
                break;
            case "HoanTien":
                tv.setBackgroundResource(R.drawable.bg_badge_dahuy);
                tv.setTextColor(context.getResources().getColor(R.color.badge_dahuy_text, null));
                break;
        }
    }

    /** Chuyển định dạng yyyy-MM-dd sang dd/MM/yyyy */
    private String formatDate(String date) {
        if (date == null || date.length() < 10) return "";
        try {
            String[] parts = date.substring(0, 10).split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return date;
        }
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMaHD;
        final TextView tvNgayLap;
        final TextView tvTenKhach;
        final TextView tvTenPhong;
        final TextView tvTongCong;
        final TextView tvTrangThai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHD      = itemView.findViewById(R.id.tv_ma_hd);
            tvNgayLap   = itemView.findViewById(R.id.tv_ngay_lap);
            tvTenKhach  = itemView.findViewById(R.id.tv_ten_khach);
            tvTenPhong  = itemView.findViewById(R.id.tv_ten_phong);
            tvTongCong  = itemView.findViewById(R.id.tv_tong_cong);
            tvTrangThai = itemView.findViewById(R.id.tv_trang_thai);
        }
    }
}
