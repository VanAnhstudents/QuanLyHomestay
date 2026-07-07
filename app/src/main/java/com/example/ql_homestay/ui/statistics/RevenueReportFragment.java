package com.example.ql_homestay.ui.statistics;

import android.content.Intent;
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
import com.example.ql_homestay.model.HoaDon;
import com.example.ql_homestay.repository.InvoiceRepository;
import com.example.ql_homestay.repository.StatisticsRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * H2 – Báo cáo Doanh thu.
 * DateRange picker, card tóm tắt (Tổng DT + % so kỳ trước),
 * BarChart DT theo ngày, danh sách HĐ compact, xuất báo cáo (Share).
 */
public class RevenueReportFragment extends Fragment {

    private StatisticsRepository statsRepo;
    private InvoiceRepository invoiceRepo;

    // Views
    private TextView tvFromDate, tvToDate;
    private LinearLayout btnViewReport;
    private LinearLayout cardSummary, cardChart, cardInvoiceList;
    private TextView tvTotalRevenue, tvComparePrev, tvInvoiceCount, tvEmpty;
    private FrameLayout chartContainer;
    private LinearLayout llInvoiceList;
    private LinearLayout btnExport;

    // Dữ liệu kỳ hiện tại
    private String fromDate, toDate;
    private List<HoaDon> reportInvoices;
    private double reportRevenue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_revenue_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        statsRepo   = new StatisticsRepository(dbHelper);
        invoiceRepo = new InvoiceRepository(dbHelper);

        bindViews(view);
        setupBreadcrumb(view);
        setupDatePickers();
        setupButtons();

        // Default: tháng hiện tại
        setDefaultMonth();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvFromDate      = view.findViewById(R.id.tv_from_date);
        tvToDate        = view.findViewById(R.id.tv_to_date);
        btnViewReport   = view.findViewById(R.id.btn_view_report);
        cardSummary     = view.findViewById(R.id.card_summary);
        cardChart       = view.findViewById(R.id.card_chart);
        cardInvoiceList = view.findViewById(R.id.card_invoice_list);
        tvTotalRevenue  = view.findViewById(R.id.tv_total_revenue);
        tvComparePrev   = view.findViewById(R.id.tv_compare_prev);
        tvInvoiceCount  = view.findViewById(R.id.tv_invoice_count);
        tvEmpty         = view.findViewById(R.id.tv_empty);
        chartContainer  = view.findViewById(R.id.chart_container);
        llInvoiceList   = view.findViewById(R.id.ll_invoice_list);
        btnExport       = view.findViewById(R.id.btn_export);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Thống kê → Doanh thu");
    }

    private void setDefaultMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = formatDateStr(cal);
        tvFromDate.setText(toDisplayDate(fromDate));

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = formatDateStr(cal);
        tvToDate.setText(toDisplayDate(toDate));
    }

    private void setupDatePickers() {
        // Từ ngày
        View fromLayout = requireView().findViewById(R.id.tv_from_date);
        // Tìm FrameLayout parent để đặt click listener
        View fromFrame = (View) fromLayout.getParent();
        fromFrame.setOnClickListener(v -> showDatePicker(true));

        View toFrame = (View) requireView().findViewById(R.id.tv_to_date).getParent();
        toFrame.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFrom) {
        // Dùng DatePickerDialog chuẩn Android
        Calendar cal = Calendar.getInstance();
        if (isFrom && fromDate != null && fromDate.length() == 10) {
            try {
                String[] p = fromDate.split("-");
                cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1])-1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {}
        }

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
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupButtons() {
        btnViewReport.setOnClickListener(v -> loadReport());
        btnExport.setOnClickListener(v -> exportReport());
    }

    // ─── Load data ────────────────────────────────────────────────────────────

    private void loadReport() {
        if (fromDate == null || toDate == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn khoảng ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            // Kỳ hiện tại
            double revenue = statsRepo.getRevenueByRange(fromDate, toDate);
            List<Object[]> revenueByDay = statsRepo.getRevenueByDay(fromDate, toDate);
            List<HoaDon> invoices = invoiceRepo.getInvoicesByDateRange(fromDate, toDate);

            // Kỳ trước (cùng khoảng thời gian liền trước)
            long daysDiff = daysBetween(fromDate, toDate) + 1;
            String prevTo = shiftDate(fromDate, -1);
            String prevFrom = shiftDate(prevTo, -(int)(daysDiff - 1));
            double prevRevenue = statsRepo.getRevenueByRange(prevFrom, prevTo);

            double changePct = prevRevenue > 0
                    ? ((revenue - prevRevenue) / prevRevenue) * 100.0
                    : (revenue > 0 ? 100.0 : 0.0);

            reportInvoices = invoices;
            reportRevenue  = revenue;

            if (getActivity() != null) {
                final double fRevenue = revenue;
                final double fChangePct = changePct;
                final int fCount = invoices.size();

                getActivity().runOnUiThread(() -> {
                    // Card tóm tắt
                    cardSummary.setVisibility(View.VISIBLE);
                    tvTotalRevenue.setText(formatMoney(fRevenue));
                    tvInvoiceCount.setText(String.valueOf(fCount));
                    String pctStr = (fChangePct >= 0 ? "+" : "") +
                            String.format(Locale.getDefault(), "%.1f%%", fChangePct);
                    tvComparePrev.setText(pctStr);
                    tvComparePrev.setTextColor(getResources().getColor(
                            fChangePct >= 0 ? R.color.status_success : R.color.status_error, null));

                    // Chart
                    cardChart.setVisibility(View.VISIBLE);
                    buildBarChart(revenueByDay);

                    // Danh sách HĐ
                    cardInvoiceList.setVisibility(View.VISIBLE);
                    buildInvoiceList(invoices);
                });
            }
        }).start();
    }

    // ─── Chart ───────────────────────────────────────────────────────────────

    private void buildBarChart(List<Object[]> data) {
        chartContainer.removeAllViews();
        if (data == null || data.isEmpty()) return;

        try {
            BarChart chart = new BarChart(requireContext());
            chart.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels   = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                entries.add(new BarEntry(i, (float)((Double) data.get(i)[1] / 1_000_000.0)));
                String d = (String) data.get(i)[0];
                labels.add(d != null && d.length() >= 10 ? d.substring(8,10)+"/"+d.substring(5,7) : "");
            }

            BarDataSet ds = new BarDataSet(entries, "Doanh thu (triệu đ)");
            ds.setColor(getResources().getColor(R.color.primary_main, null));
            ds.setDrawValues(data.size() <= 14);
            ds.setValueTextSize(9f);

            BarData barData = new BarData(ds);
            barData.setBarWidth(0.7f);
            chart.setData(barData);
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.getXAxis().setLabelRotationAngle(-30f);
            chart.getXAxis().setGranularity(1f);
            chart.getDescription().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.animateY(600);
            chart.invalidate();

            chartContainer.addView(chart);
        } catch (Exception e) {
            // Fallback list
            LinearLayout ll = new LinearLayout(requireContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            for (Object[] row : data) {
                TextView tv = new TextView(requireContext());
                tv.setTextSize(12f);
                tv.setTextColor(getResources().getColor(R.color.text_primary, null));
                tv.setPadding(0, dpToPx(4), 0, dpToPx(4));
                tv.setText(toDisplayDate((String) row[0]) + ": " + formatMoney((Double) row[1]));
                ll.addView(tv);
            }
            chartContainer.addView(ll);
        }
    }

    // ─── Invoice list compact ─────────────────────────────────────────────────

    private void buildInvoiceList(List<HoaDon> list) {
        llInvoiceList.removeAllViews();
        if (list == null || list.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Không có hóa đơn trong kỳ này");
            tv.setTextSize(13f);
            tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
            llInvoiceList.addView(tv);
            return;
        }

        for (HoaDon hd : list) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(56)));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            // Khách + phòng (cột trái)
            LinearLayout leftCol = new LinearLayout(requireContext());
            leftCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            leftCol.setOrientation(LinearLayout.VERTICAL);

            TextView tvName = new TextView(requireContext());
            tvName.setTextSize(13f);
            tvName.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvName.setText(hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "—");
            leftCol.addView(tvName);

            TextView tvRoom = new TextView(requireContext());
            tvRoom.setTextSize(11f);
            tvRoom.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tvRoom.setText(hd.getTenPhong() != null ? hd.getTenPhong() : "—");
            leftCol.addView(tvRoom);

            // Tổng tiền (cột phải)
            TextView tvAmount = new TextView(requireContext());
            tvAmount.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvAmount.setTextSize(14f);
            tvAmount.setTextColor(getResources().getColor(R.color.primary_main, null));
            tvAmount.setText(formatMoney(hd.getTongCong()));
            tvAmount.setGravity(Gravity.END);

            row.addView(leftCol);
            row.addView(tvAmount);
            llInvoiceList.addView(row);

            // Divider
            View div = new View(requireContext());
            div.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            div.setBackgroundColor(getResources().getColor(R.color.divider, null));
            llInvoiceList.addView(div);
        }
    }

    // ─── Export ───────────────────────────────────────────────────────────────

    private void exportReport() {
        if (reportInvoices == null) {
            Toast.makeText(requireContext(), "Vui lòng xem báo cáo trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== BÁO CÁO DOANH THU – LALA HOUSE ===\n");
        sb.append("Kỳ báo cáo: ").append(toDisplayDate(fromDate))
          .append(" → ").append(toDisplayDate(toDate)).append("\n");
        sb.append("Tổng doanh thu: ").append(formatMoney(reportRevenue)).append("\n");
        sb.append("Số hóa đơn: ").append(reportInvoices.size()).append("\n\n");
        sb.append("CHI TIẾT:\n");
        for (HoaDon hd : reportInvoices) {
            sb.append("• HĐ #").append(String.format(Locale.getDefault(), "%03d", hd.getMaHD()))
              .append(" – ").append(hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "—")
              .append(" – ").append(hd.getTenPhong() != null ? hd.getTenPhong() : "—")
              .append(": ").append(formatMoney(hd.getTongCong())).append("\n");
        }
        sb.append("========================================");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Xuất báo cáo doanh thu"));
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
        return NumberFormat.getNumberInstance(Locale.getDefault()).format((long) amount) + " đ";
    }

    private long daysBetween(String from, String to) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            long diff = sdf.parse(to).getTime() - sdf.parse(from).getTime();
            return diff / (1000 * 60 * 60 * 24);
        } catch (Exception e) { return 0; }
    }

    private String shiftDate(String date, int days) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(date));
            cal.add(Calendar.DAY_OF_MONTH, days);
            return sdf.format(cal.getTime());
        } catch (Exception e) { return date; }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
