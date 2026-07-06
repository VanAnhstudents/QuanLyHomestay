package com.example.ql_homestay.ui.statistics;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.repository.StatisticsRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * H3 – Báo cáo Công suất phòng.
 * DateRange picker, card 4 chỉ số (Tổng phòng / Đêm khả dụng / Đêm đã bán / Công suất%),
 * LineChart công suất theo ngày, bảng chi tiết từng phòng.
 */
public class OccupancyReportFragment extends Fragment {

    private StatisticsRepository statsRepo;

    // Views
    private TextView tvFromDate, tvToDate;
    private LinearLayout btnViewReport;
    private LinearLayout cardMetrics, cardLineChart, cardRoomTable;
    private TextView tvTotalRooms, tvAvailableNights, tvSoldNights, tvOccupancyRate;
    private TextView tvEmpty;
    private FrameLayout chartLineContainer;
    private LinearLayout llRoomTableRows;

    // Dữ liệu
    private String fromDate, toDate;
    private int totalDays = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_occupancy_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        statsRepo = new StatisticsRepository(dbHelper);

        bindViews(view);
        setupDatePickers();
        setupButtons();
        setDefaultMonth();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvFromDate        = view.findViewById(R.id.tv_from_date);
        tvToDate          = view.findViewById(R.id.tv_to_date);
        btnViewReport     = view.findViewById(R.id.btn_view_report);
        cardMetrics       = view.findViewById(R.id.card_metrics);
        cardLineChart     = view.findViewById(R.id.card_line_chart);
        cardRoomTable     = view.findViewById(R.id.card_room_table);
        tvTotalRooms      = view.findViewById(R.id.tv_total_rooms);
        tvAvailableNights = view.findViewById(R.id.tv_available_nights);
        tvSoldNights      = view.findViewById(R.id.tv_sold_nights);
        tvOccupancyRate   = view.findViewById(R.id.tv_occupancy_rate);
        tvEmpty           = view.findViewById(R.id.tv_empty);
        chartLineContainer= view.findViewById(R.id.chart_line_container);
        llRoomTableRows   = view.findViewById(R.id.ll_room_table_rows);
    }

    private void setDefaultMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = formatDateStr(cal);
        tvFromDate.setText(toDisplayDate(fromDate));

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = formatDateStr(cal);
        tvToDate.setText(toDisplayDate(toDate));
        totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void setupDatePickers() {
        View fromFrame = (View) requireView().findViewById(R.id.tv_from_date).getParent();
        fromFrame.setOnClickListener(v -> showDatePicker(true));

        View toFrame = (View) requireView().findViewById(R.id.tv_to_date).getParent();
        toFrame.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    String dateStr = formatDateStr(selected);
                    if (isFrom) {
                        fromDate = dateStr;
                        tvFromDate.setText(toDisplayDate(dateStr));
                    } else {
                        toDate = dateStr;
                        tvToDate.setText(toDisplayDate(dateStr));
                        // Tính totalDays
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            long diff = sdf.parse(toDate).getTime() - sdf.parse(fromDate).getTime();
                            totalDays = Math.max(1, (int)(diff / (1000 * 60 * 60 * 24)) + 1);
                        } catch (Exception e) { totalDays = 30; }
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupButtons() {
        btnViewReport.setOnClickListener(v -> loadReport());

        View backBtn = requireView().findViewById(R.id.btn_back);
        if (backBtn != null) backBtn.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    // ─── Load data ────────────────────────────────────────────────────────────

    private void loadReport() {
        if (fromDate == null || toDate == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn khoảng ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            int totalRooms = statsRepo.getTotalRooms();
            int availableNights = totalRooms * totalDays;
            double occupancyPct = statsRepo.getOccupancyRateByRange(fromDate, toDate, totalDays);

            // Tính đêm đã bán ≈ occupancyPct * availableNights / 100
            int soldNights = (int) Math.round(occupancyPct * availableNights / 100.0);

            // Công suất theo ngày (cho LineChart)
            List<Object[]> occupancyByDay = statsRepo.getOccupancyByDay(fromDate, toDate);

            // Chi tiết từng phòng
            List<Object[]> roomDetail = statsRepo.getRoomOccupancyDetail(fromDate, toDate, totalDays);

            if (getActivity() != null) {
                final int fTotalRooms = totalRooms;
                final int fAvail = availableNights;
                final int fSold = soldNights;
                final double fPct = occupancyPct;

                getActivity().runOnUiThread(() -> {
                    // Card chỉ số
                    cardMetrics.setVisibility(View.VISIBLE);
                    tvTotalRooms.setText(String.valueOf(fTotalRooms));
                    tvAvailableNights.setText(String.valueOf(fAvail));
                    tvSoldNights.setText(String.valueOf(fSold));
                    tvOccupancyRate.setText(String.format(Locale.getDefault(), "%.1f%%", fPct));

                    // LineChart
                    cardLineChart.setVisibility(View.VISIBLE);
                    buildLineChart(occupancyByDay);

                    // Bảng chi tiết
                    cardRoomTable.setVisibility(View.VISIBLE);
                    buildRoomTable(roomDetail);
                });
            }
        }).start();
    }

    // ─── Charts ───────────────────────────────────────────────────────────────

    private void buildLineChart(List<Object[]> data) {
        chartLineContainer.removeAllViews();
        if (data == null || data.isEmpty()) return;

        try {
            LineChart lineChart = new LineChart(requireContext());
            lineChart.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                Object[] row = data.get(i);
                entries.add(new Entry(i, ((Double) row[1]).floatValue()));
                String d = (String) row[0];
                labels.add(d != null && d.length() >= 10 ? d.substring(8,10)+"/"+d.substring(5,7) : "");
            }

            LineDataSet ds = new LineDataSet(entries, "Công suất (%)");
            ds.setColor(getResources().getColor(R.color.primary_main, null));
            ds.setCircleColor(getResources().getColor(R.color.primary_main, null));
            ds.setLineWidth(2f);
            ds.setCircleRadius(3f);
            ds.setDrawValues(data.size() <= 10);
            ds.setValueTextSize(9f);
            ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            ds.setDrawFilled(true);
            ds.setFillColor(getResources().getColor(R.color.primary_light, null));
            ds.setFillAlpha(80);

            LineData lineData = new LineData(ds);
            lineChart.setData(lineData);
            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            lineChart.getXAxis().setLabelRotationAngle(-30f);
            lineChart.getXAxis().setGranularity(1f);
            lineChart.getDescription().setEnabled(false);
            lineChart.getLegend().setEnabled(false);
            lineChart.getAxisRight().setEnabled(false);
            lineChart.getAxisLeft().setAxisMinimum(0f);
            lineChart.getAxisLeft().setAxisMaximum(100f);
            lineChart.animateX(800);
            lineChart.invalidate();

            chartLineContainer.addView(lineChart);
        } catch (Exception e) {
            // Fallback
            LinearLayout ll = new LinearLayout(requireContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            if (data != null) {
                for (Object[] row : data) {
                    TextView tv = new TextView(requireContext());
                    tv.setTextSize(12f);
                    tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                    tv.setPadding(0, dpToPx(3), 0, dpToPx(3));
                    tv.setText(toDisplayDate((String) row[0]) + ": " +
                            String.format(Locale.getDefault(), "%.1f%%", (Double) row[1]));
                    ll.addView(tv);
                }
            }
            chartLineContainer.addView(ll);
        }
    }

    // ─── Room table ──────────────────────────────────────────────────────────

    private void buildRoomTable(List<Object[]> roomDetail) {
        llRoomTableRows.removeAllViews();
        if (roomDetail == null || roomDetail.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Không có dữ liệu");
            tv.setTextSize(13f);
            tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
            llRoomTableRows.addView(tv);
            return;
        }

        for (Object[] row : roomDetail) {
            // row: {tenPhong, soDem, doanhThu, congSuat%}
            LinearLayout rowView = new LinearLayout(requireContext());
            rowView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(52)));
            rowView.setOrientation(LinearLayout.HORIZONTAL);
            rowView.setGravity(Gravity.CENTER_VERTICAL);
            rowView.setPadding(dpToPx(8), 0, dpToPx(8), 0);

            // Tên phòng
            TextView tvName = new TextView(requireContext());
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
            tvName.setTextSize(13f);
            tvName.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvName.setText((String) row[0]);

            // Số đêm
            TextView tvDem = new TextView(requireContext());
            tvDem.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvDem.setTextSize(13f);
            tvDem.setGravity(Gravity.CENTER_HORIZONTAL);
            tvDem.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvDem.setText(String.valueOf((Integer) row[1]));

            // Doanh thu
            TextView tvRev = new TextView(requireContext());
            tvRev.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
            tvRev.setTextSize(12f);
            tvRev.setGravity(Gravity.END);
            tvRev.setTextColor(getResources().getColor(R.color.primary_main, null));
            tvRev.setText(formatMoney((Double) row[2]));

            // Công suất %
            TextView tvPct = new TextView(requireContext());
            tvPct.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvPct.setTextSize(12f);
            tvPct.setGravity(Gravity.END);
            tvPct.setTextColor(getResources().getColor(R.color.status_warning, null));
            tvPct.setText(String.format(Locale.getDefault(), "%.0f%%", (Double) row[3]));

            rowView.addView(tvName);
            rowView.addView(tvDem);
            rowView.addView(tvRev);
            rowView.addView(tvPct);
            llRoomTableRows.addView(rowView);

            // Divider
            View div = new View(requireContext());
            div.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            div.setBackgroundColor(getResources().getColor(R.color.divider, null));
            llRoomTableRows.addView(div);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String formatDateStr(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private String toDisplayDate(String date) {
        if (date == null || date.length() < 10) return date != null ? date : "—";
        try {
            String[] p = date.substring(0, 10).split("-");
            return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception e) { return date; }
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000) {
            return String.format(Locale.getDefault(), "%.1f tr", amount / 1_000_000);
        }
        return NumberFormat.getNumberInstance(Locale.getDefault()).format((long) amount) + "đ";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
