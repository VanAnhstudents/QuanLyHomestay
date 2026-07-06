package com.example.ql_homestay.ui.customer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.repository.CustomerRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerStayHistoryFragment extends Fragment {
    private static final String ARG_MA_KH = "maKH";

    public static CustomerStayHistoryFragment newInstance(int maKH) {
        CustomerStayHistoryFragment f = new CustomerStayHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_KH, maKH);
        f.setArguments(args);
        return f;
    }

    private int maKH = -1;
    private CustomerRepository repository;
    private RecyclerView rvHistory;
    private TextView tvNoHistory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maKH = getArguments().getInt(ARG_MA_KH, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_stay_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new CustomerRepository(requireContext());
        rvHistory = view.findViewById(R.id.rv_all_history);
        tvNoHistory = view.findViewById(R.id.tv_no_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        View bc = view.findViewById(R.id.breadcrumb);
        TextView tvBreadcrumb = bc != null ? bc.findViewById(R.id.tv_breadcrumb) : null;
        if (tvBreadcrumb != null) tvBreadcrumb.setText("Trang chủ → Khách hàng → Lịch sử lưu trú");

        loadHistory();
    }

    private void loadHistory() {
        executor.execute(() -> {
            List<DatPhong> list = repository.getAllBookings(maKH);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                boolean empty = list == null || list.isEmpty();
                rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
                tvNoHistory.setVisibility(empty ? View.VISIBLE : View.GONE);
                if (!empty) rvHistory.setAdapter(new HistoryAdapter(list));
            });
        });
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private final List<DatPhong> data;

        HistoryAdapter(List<DatPhong> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_compact, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            DatPhong dp = data.get(pos);
            h.tvCustomer.setText("Check-in: " + dp.getNgayCheckIn());
            h.tvRoom.setText(dp.getTenPhong() != null ? dp.getTenPhong() : "—");
            h.tvStatus.setText(mapTrangThai(dp.getTrangThai()));
            h.tvStatus.setBackgroundResource(badgeDrawable(dp.getTrangThai()));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvCustomer, tvRoom, tvStatus;

            VH(View v) {
                super(v);
                tvCustomer = v.findViewById(R.id.tv_compact_customer);
                tvRoom = v.findViewById(R.id.tv_compact_room);
                tvStatus = v.findViewById(R.id.badge_compact_status);
            }
        }

        private static String mapTrangThai(String tt) {
            if (tt == null) return "";
            switch (tt) {
                case "SapDen": return "Sắp đến";
                case "DangO": return "Đang ở";
                case "DaTraPhong": return "Đã trả";
                case "DaHuy": return "Đã hủy";
                default: return tt;
            }
        }

        private static int badgeDrawable(String tt) {
            if (tt == null) return R.drawable.bg_badge_pill;
            switch (tt) {
                case "SapDen": return R.drawable.bg_badge_dadat;
                case "DangO": return R.drawable.bg_badge_dangthue;
                case "DaTraPhong": return R.drawable.bg_badge_dathanhtoan;
                case "DaHuy": return R.drawable.bg_badge_dahuy;
                default: return R.drawable.bg_badge_pill;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
