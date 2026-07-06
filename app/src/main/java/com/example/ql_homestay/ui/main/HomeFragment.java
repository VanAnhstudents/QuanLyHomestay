package com.example.ql_homestay.ui.main;

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

import com.example.ql_homestay.MainActivity;
import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.PhongDAO;
import com.example.ql_homestay.model.CheckInOut;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.repository.BookingRepository;
import com.example.ql_homestay.ui.booking.BookingDetailFragment;
import com.example.ql_homestay.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private TextView tvGreeting, tvTotal, tvAvailable, tvOccupied, tvRevenue;
    private TextView tvSeeAllBookings, tvSeeAllActivity, tvEmptyBookings, tvEmptyActivity;
    private RecyclerView rvTodayBookings, rvRecentActivity;

    private BookingRepository bookingRepository;
    private PhongDAO phongDAO;
    private SessionManager session;
    private TodayBookingAdapter bookingAdapter;
    private RecentActivityAdapter activityAdapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        bookingRepository = new BookingRepository(dbHelper);
        phongDAO = new PhongDAO(dbHelper);
        session = SessionManager.getInstance(requireContext());

        bindViews(view);
        setupLists();
        setupClicks();
        loadHomeData();
    }

    private void bindViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvTotal = view.findViewById(R.id.tv_kpi_total_value);
        tvAvailable = view.findViewById(R.id.tv_kpi_available_value);
        tvOccupied = view.findViewById(R.id.tv_kpi_occupied_value);
        tvRevenue = view.findViewById(R.id.tv_kpi_revenue_value);
        tvSeeAllBookings = view.findViewById(R.id.tv_see_all_bookings);
        tvSeeAllActivity = view.findViewById(R.id.tv_see_all_activity);
        tvEmptyBookings = view.findViewById(R.id.tv_empty_bookings);
        tvEmptyActivity = view.findViewById(R.id.tv_empty_activity);
        rvTodayBookings = view.findViewById(R.id.rv_today_bookings);
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity);
    }

    private void setupLists() {
        bookingAdapter = new TodayBookingAdapter(this::openBookingDetail);
        rvTodayBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTodayBookings.setAdapter(bookingAdapter);

        activityAdapter = new RecentActivityAdapter(this::openBookingDetail);
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentActivity.setAdapter(activityAdapter);
    }

    private void setupClicks() {
        tvSeeAllBookings.setOnClickListener(v -> openBookingList());
        tvSeeAllActivity.setOnClickListener(v -> openBookingList());
    }

    private void loadHomeData() {
        String hoTen = session.getHoTen();
        tvGreeting.setText("Xin chào, " + (hoTen.isEmpty() ? session.getTenDangNhap() : hoTen) + "!");

        executor.execute(() -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            List<DatPhong> todayOrActive = bookingRepository.getTodayAndActive(today);
            List<DatPhong> allBookings = bookingRepository.getAllDatPhong();
            List<DatPhong> bookings = limit(todayOrActive.isEmpty() ? allBookings : todayOrActive, 3);
            List<CheckInOut> activities = bookingRepository.getRecentActivity(5);
            int totalRooms = phongDAO.countAll();
            int availableRooms = phongDAO.countByTrangThai("Trong");
            int occupiedRooms = phongDAO.countByTrangThai("DangThue");

            mainHandler.post(() -> {
                if (!isAdded()) return;
                tvTotal.setText(String.valueOf(totalRooms));
                tvAvailable.setText(String.valueOf(availableRooms));
                tvOccupied.setText(String.valueOf(occupiedRooms));
                tvRevenue.setText("0đ");
                bindBookings(bookings);
                bindActivities(activities);
            });
        });
    }

    private List<DatPhong> limit(List<DatPhong> source, int limit) {
        if (source == null || source.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(source.subList(0, Math.min(limit, source.size())));
    }

    private void bindBookings(List<DatPhong> bookings) {
        boolean empty = bookings == null || bookings.isEmpty();
        rvTodayBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyBookings.setVisibility(empty ? View.VISIBLE : View.GONE);
        bookingAdapter.setData(bookings);
    }

    private void bindActivities(List<CheckInOut> activities) {
        boolean empty = activities == null || activities.isEmpty();
        rvRecentActivity.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyActivity.setVisibility(empty ? View.VISIBLE : View.GONE);
        activityAdapter.setData(activities);
    }

    private void openBookingList() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setBottomNavSelection(R.id.nav_booking);
        }
    }

    private void openBookingDetail(int maDatPhong) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, BookingDetailFragment.newInstance(maDatPhong))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bookingRepository != null) loadHomeData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private static class TodayBookingAdapter extends RecyclerView.Adapter<TodayBookingAdapter.VH> {
        interface OnClick { void onClick(int maDatPhong); }

        private final List<DatPhong> data = new ArrayList<>();
        private final OnClick onClick;

        TodayBookingAdapter(OnClick onClick) {
            this.onClick = onClick;
        }

        void setData(List<DatPhong> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_compact, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            DatPhong dp = data.get(position);
            h.title.setText(dp.getTenKhachHang() != null ? dp.getTenKhachHang() : "Khách hàng");
            h.subtitle.setText((dp.getTenPhong() != null ? dp.getTenPhong() : "Phòng") +
                    " • " + nullToEmpty(dp.getNgayCheckIn()));
            h.badge.setText(statusLabel(dp.getTrangThai()));
            h.badge.setBackgroundResource(statusBadge(dp.getTrangThai()));
            h.itemView.setOnClickListener(v -> onClick.onClick(dp.getMaDatPhong()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView title, subtitle, badge;

            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_compact_customer);
                subtitle = v.findViewById(R.id.tv_compact_room);
                badge = v.findViewById(R.id.badge_compact_status);
            }
        }
    }

    private static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.VH> {
        interface OnClick { void onClick(int maDatPhong); }

        private final List<CheckInOut> data = new ArrayList<>();
        private final OnClick onClick;

        RecentActivityAdapter(OnClick onClick) {
            this.onClick = onClick;
        }

        void setData(List<CheckInOut> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_compact, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            CheckInOut log = data.get(position);
            h.title.setText(activityLabel(log.getLoai()) + " • " + nullToEmpty(log.getTenKhachHang()));
            h.subtitle.setText(nullToEmpty(log.getTenPhong()) + " • " + nullToEmpty(log.getThoiGian()));
            h.badge.setText(log.getLoai() != null ? log.getLoai() : "");
            h.badge.setBackgroundResource("CheckOut".equals(log.getLoai())
                    ? R.drawable.bg_badge_dathanhtoan
                    : R.drawable.bg_badge_dangthue);
            h.itemView.setOnClickListener(v -> onClick.onClick(log.getMaDatPhong()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView title, subtitle, badge;

            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_compact_customer);
                subtitle = v.findViewById(R.id.tv_compact_room);
                badge = v.findViewById(R.id.badge_compact_status);
            }
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String activityLabel(String loai) {
        if ("CheckOut".equals(loai)) return "Trả phòng";
        if ("CheckIn".equals(loai)) return "Nhận phòng";
        return "Hoạt động";
    }

    private static String statusLabel(String status) {
        if ("SapDen".equals(status)) return "Sắp đến";
        if ("DangO".equals(status)) return "Đang ở";
        if ("DaTraPhong".equals(status)) return "Đã trả";
        if ("DaHuy".equals(status)) return "Đã hủy";
        return status == null ? "" : status;
    }

    private static int statusBadge(String status) {
        if ("DangO".equals(status)) return R.drawable.bg_badge_dangthue;
        if ("DaTraPhong".equals(status)) return R.drawable.bg_badge_dathanhtoan;
        if ("DaHuy".equals(status)) return R.drawable.bg_badge_dahuy;
        return R.drawable.bg_badge_dadat;
    }
}
