package com.example.ql_homestay.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.repository.StatisticsRepository;import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * H1 – Dashboard Thống kê.
 * Row chọn kỳ (Hôm nay / Tuần / Tháng / Quý / Năm),
 * 4 KPI card, BarChart doanh thu theo ngày, PieChart trạng thái phòng,
 * Top 5 phòng doanh thu cao.
 */
public class StatisticsDashboardFragment extends Fragment {

    private StatisticsRepository statsRepo;
    private DatabaseHelper dbHelper;

    // KPI views
    private TextView tvRevenue, tvGuests, tvOccupancy, tvBookings;

    // Legend
    private TextView tvLegendTrong, tvLegendDangThue, tvLegendDaDat;

    // Navigation detail buttons
    private TextView tvRevenueDetail, tvOccupancyDetail;

    // Chart containers
    private FrameLayout chartBarContainer, chartPieContainer;
    private TextView tvChartBarEmpty;

    // Top phòng
    private LinearLayout llTopRooms;

    // Period chips
    private TextView chipToday, chipWeek, chipMonth, chipQuarter, chipYear;

    // Current period
    private String fromDate, toDate;
    private int totalDays = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper  = DatabaseHelper.getInstance(requireContext());
        statsRepo = new StatisticsRepository(dbHelper);

        bindViews(view);
        setupBreadcrumb(view);
        setupPeriodChips();
        setupDetailNavigation();

        // Mặc định: Hôm nay
        setTodayPeriod();
        loadData();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvRevenue         = view.findViewById(R.id.tv_revenue);
        tvGuests          = view.findViewById(R.id.tv_guests);
        tvOccupancy       = view.findViewById(R.id.tv_occupancy);
        tvBookings        = view.findViewById(R.id.tv_bookings);
        tvLegendTrong     = view.findViewById(R.id.tv_legend_trong);
        tvLegendDangThue  = view.findViewById(R.id.tv_legend_dangthue);
        tvLegendDaDat     = view.findViewById(R.id.tv_legend_dadat);
        tvRevenueDetail   = view.findViewById(R.id.tv_revenue_detail);
        tvOccupancyDetail = view.findViewById(R.id.tv_occupancy_detail);
        chartBarContainer = view.findViewById(R.id.chart_container_bar);
        chartPieContainer = view.findViewById(R.id.chart_container_pie);
        tvChartBarEmpty   = view.findViewById(R.id.tv_chart_bar_empty);
        llTopRooms        = view.findViewById(R.id.ll_top_rooms);
        chipToday         = view.findViewById(R.id.chip_today);
        chipWeek          = view.findViewById(R.id.chip_week);
        chipMonth         = view.findViewById(R.id.chip_month);
        chipQuarter       = view.findViewById(R.id.chip_quarter);
        chipYear          = view.findViewById(R.id.chip_year);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Thống kê");
    }

    private void setupDetailNavigation() {
        if (tvRevenueDetail != null) {
            tvRevenueDetail.setOnClickListener(v -> navigateTo(new RevenueReportFragment()));
        }
        if (tvOccupancyDetail != null) {
            tvOccupancyDetail.setOnClickListener(v -> navigateTo(new OccupancyReportFragment()));
        }
    }

    /** Điều hướng sang Fragment chi tiết, đẩy vào back stack để có thể Back. */
    private void navigateTo(androidx.fragment.app.Fragment target) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, target)
                .addToBackStack(null)
                .commit();
    }

    private void setupPeriodChips() {
        chipToday.setOnClickListener(v -> { setTodayPeriod();   updateChipStyle(chipToday);   loadData(); });
        chipWeek.setOnClickListener(v  -> { setWeekPeriod();    updateChipStyle(chipWeek);    loadData(); });
        chipMonth.setOnClickListener(v -> { setMonthPeriod();   updateChipStyle(chipMonth);   loadData(); });
        chipQuarter.setOnClickListener(v->{ setQuarterPeriod(); updateChipStyle(chipQuarter); loadData(); });
        chipYear.setOnClickListener(v  -> { setYearPeriod();    updateChipStyle(chipYear);    loadData(); });
    }

    // ─── Periods ──────────────────────────────────────────────────────────────

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private void setTodayPeriod() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        fromDate = today; toDate = today; totalDays = 1;
    }

    private void setWeekPeriod() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        fromDate = formatDate(cal);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        toDate = formatDate(cal);
        totalDays = 7;
    }

    private void setMonthPeriod() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = formatDate(cal);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = formatDate(cal);
        totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void setQuarterPeriod() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int quarterStart = (month / 3) * 3;
        cal.set(Calendar.MONTH, quarterStart);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = formatDate(cal);
        cal.set(Calendar.MONTH, quarterStart + 2);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = formatDate(cal);
        totalDays = 91;
    }

    private void setYearPeriod() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        fromDate = formatDate(cal);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        toDate = formatDate(cal);
        totalDays = 365;
    }

    // ─── Load data ────────────────────────────────────────────────────────────

    private void loadData() {
        if (fromDate == null || toDate == null) return;

        new Thread(() -> {
            // KPI
            double revenue    = statsRepo.getRevenueByRange(fromDate, toDate);
            int guests        = statsRepo.getTotalGuestsByRange(fromDate, toDate);
            double occupancy  = statsRepo.getOccupancyRateByRange(fromDate, toDate, totalDays);
            int bookings      = statsRepo.getTotalBookingsByRange(fromDate, toDate);

            // Phòng theo trạng thái (realtime)
            int roomTrong    = statsRepo.getRoomsByTrangThai("Trong");
            int roomDangThue = statsRepo.getRoomsByTrangThai("DangThue");
            int roomDaDat    = statsRepo.getRoomsByTrangThai("DaDat");

            // Doanh thu theo ngày
            List<Object[]> revenueByDay = statsRepo.getRevenueByDay(fromDate, toDate);

            // Top 5 phòng
            List<Object[]> topRooms = statsRepo.getTopRoomsByRevenue(5);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    updateKpiCards(revenue, guests, occupancy, bookings);
                    updatePieLegend(roomTrong, roomDangThue, roomDaDat);
                    updateBarChart(revenueByDay);
                    updatePieChart(roomTrong, roomDangThue, roomDaDat);
                    updateTopRooms(topRooms);
                });
            }
        }).start();
    }

    private void updateKpiCards(double revenue, int guests, double occupancy, int bookings) {
        String revenueStr = formatMoney(revenue);
        tvRevenue.setText(revenueStr);
        tvGuests.setText(String.valueOf(guests));
        tvOccupancy.setText(String.format(Locale.getDefault(), "%.1f%%", occupancy));
        tvBookings.setText(String.valueOf(bookings));
    }

    private void updatePieLegend(int trong, int dangThue, int daDat) {
        tvLegendTrong.setText("⬤ Trống: " + trong);
        tvLegendDangThue.setText("⬤ Đang thuê: " + dangThue);
        tvLegendDaDat.setText("⬤ Đã đặt: " + daDat);
    }

    // ─── Charts ───────────────────────────────────────────────────────────────

    private void updateBarChart(List<Object[]> revenueByDay) {
        chartBarContainer.removeAllViews();

        if (revenueByDay == null || revenueByDay.isEmpty()) {
            tvChartBarEmpty.setVisibility(View.VISIBLE);
            chartBarContainer.setVisibility(View.GONE);
            return;
        }

        tvChartBarEmpty.setVisibility(View.GONE);
        chartBarContainer.setVisibility(View.VISIBLE);

        try {
            BarChart barChart = new BarChart(requireContext());
            barChart.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels   = new ArrayList<>();
            for (int i = 0; i < revenueByDay.size(); i++) {
                Object[] row = revenueByDay.get(i);
                entries.add(new BarEntry(i, (float) ((Double) row[1] / 1_000_000.0)));
                // Lấy ngày dạng "dd/MM"
                String dateStr = (String) row[0];
                if (dateStr != null && dateStr.length() >= 10) {
                    labels.add(dateStr.substring(8, 10) + "/" + dateStr.substring(5, 7));
                } else {
                    labels.add(String.valueOf(i + 1));
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (triệu đ)");
            dataSet.setColor(getResources().getColor(R.color.primary_main, null));
            dataSet.setValueTextSize(9f);
            dataSet.setDrawValues(revenueByDay.size() <= 14);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.7f);
            barChart.setData(barData);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setLabelRotationAngle(-30f);
            barChart.getDescription().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getAxisRight().setEnabled(false);
            barChart.setTouchEnabled(true);
            barChart.animateY(800);
            barChart.invalidate();

            chartBarContainer.addView(barChart);
        } catch (Exception e) {
            // MPAndroidChart không có sẵn hoặc lỗi – hiện fallback
            showBarChartFallback(revenueByDay);
        }
    }

    private void showBarChartFallback(List<Object[]> revenueByDay) {
        // Hiện danh sách text nếu không có thư viện chart
        LinearLayout ll = new LinearLayout(requireContext());
        ll.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 8, 0, 8);

        for (Object[] row : revenueByDay) {
            TextView tv = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(28));
            tv.setLayoutParams(lp);
            tv.setTextSize(12f);
            tv.setTextColor(getResources().getColor(R.color.text_primary, null));
            String dateStr = (String) row[0];
            String label = dateStr != null && dateStr.length() >= 10
                    ? dateStr.substring(8, 10) + "/" + dateStr.substring(5, 7)
                    : dateStr;
            tv.setText(label + ": " + formatMoney((Double) row[1]));
            ll.addView(tv);
        }
        chartBarContainer.addView(ll);
    }

    private void updatePieChart(int trong, int dangThue, int daDat) {
        chartPieContainer.removeAllViews();
        int total = trong + dangThue + daDat;
        if (total == 0) return;

        try {
            PieChart pieChart = new PieChart(requireContext());
            pieChart.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            List<PieEntry> entries = new ArrayList<>();
            if (trong > 0)    entries.add(new PieEntry(trong,    "Trống"));
            if (dangThue > 0) entries.add(new PieEntry(dangThue, "Đang thuê"));
            if (daDat > 0)    entries.add(new PieEntry(daDat,    "Đã đặt"));

            int[] colors = {
                    getResources().getColor(R.color.status_success, null),
                    getResources().getColor(R.color.status_warning, null),
                    getResources().getColor(R.color.status_error, null)
            };

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.WHITE);

            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.getLegend().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(40f);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.animateY(1000);
            pieChart.invalidate();

            chartPieContainer.addView(pieChart);
        } catch (Exception e) {
            // Fallback text
            TextView tv = new TextView(requireContext());
            tv.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER));
            tv.setText("Trống: " + trong + " | Đang thuê: " + dangThue + " | Đã đặt: " + daDat);
            tv.setTextSize(14f);
            tv.setTextColor(getResources().getColor(R.color.text_primary, null));
            chartPieContainer.addView(tv);
        }
    }

    private void updateTopRooms(List<Object[]> topRooms) {
        llTopRooms.removeAllViews();
        if (topRooms == null || topRooms.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Chưa có dữ liệu");
            tv.setTextSize(13f);
            tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
            llTopRooms.addView(tv);
            return;
        }

        for (int i = 0; i < topRooms.size(); i++) {
            Object[] row = topRooms.get(i);
            LinearLayout rowView = new LinearLayout(requireContext());
            rowView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(44)));
            rowView.setOrientation(LinearLayout.HORIZONTAL);
            rowView.setGravity(Gravity.CENTER_VERTICAL);

            // Số thứ tự
            TextView tvNum = new TextView(requireContext());
            tvNum.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), LinearLayout.LayoutParams.WRAP_CONTENT));
            tvNum.setText(String.valueOf(i + 1));
            tvNum.setTextSize(14f);
            tvNum.setFontFeatureSettings("@font/sans-serif-bold");
            tvNum.setTextColor(getResources().getColor(R.color.primary_main, null));
            tvNum.setGravity(Gravity.CENTER_HORIZONTAL);

            // Tên phòng
            TextView tvName = new TextView(requireContext());
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameParams);
            tvName.setText((String) row[0]);
            tvName.setTextSize(14f);
            tvName.setTextColor(getResources().getColor(R.color.text_primary, null));

            // Doanh thu
            TextView tvRev = new TextView(requireContext());
            tvRev.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvRev.setText(formatMoney((Double) row[1]));
            tvRev.setTextSize(13f);
            tvRev.setTextColor(getResources().getColor(R.color.primary_main, null));

            rowView.addView(tvNum);
            rowView.addView(tvName);
            rowView.addView(tvRev);
            llTopRooms.addView(rowView);

            // Divider
            if (i < topRooms.size() - 1) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams dvp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(dvp);
                divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                llTopRooms.addView(divider);
            }
        }
    }

    // ─── Chip styles ─────────────────────────────────────────────────────────

    private void updateChipStyle(TextView activeChip) {
        TextView[] chips = {chipToday, chipWeek, chipMonth, chipQuarter, chipYear};
        for (TextView chip : chips) {
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.bg_button_primary);
                chip.setTextColor(getResources().getColor(R.color.text_on_primary, null));
                chip.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            } else {
                chip.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                chip.setTextColor(getResources().getColor(R.color.text_primary, null));
                chip.setTypeface(android.graphics.Typeface.DEFAULT);
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String formatMoney(double amount) {
        if (amount >= 1_000_000) {
            return NumberFormat.getNumberInstance(Locale.getDefault())
                    .format((long) (amount / 1_000_000)) + " triệu đ";
        }
        return NumberFormat.getNumberInstance(Locale.getDefault())
                .format((long) amount) + " đ";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
