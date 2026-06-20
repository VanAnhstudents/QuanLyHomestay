package com.example.ql_homestay.ui.customer;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.DatPhong;
import com.example.ql_homestay.model.KhachHang;
import com.example.ql_homestay.repository.CustomerRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerDetailFragment extends Fragment {

    private static final String ARG_MA_KH = "maKH";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    public static CustomerDetailFragment newInstance(int maKH) {
        CustomerDetailFragment f = new CustomerDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_KH, maKH);
        f.setArguments(args);
        return f;
    }

    private int maKH = -1;
    private CustomerRepository repository;
    private SessionManager session;
    private DatabaseHelper dbHelper;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private TextView tvInitials, tvTenKhach, tvBadgeSoLanThue;
    private TextView tvSdt, tvEmail, tvCccd, tvDiaChi, tvNgaySinh;
    private RecyclerView rvLichSu;
    private TextView tvNoHistory;
    private View rowActionButtons;
    private MaterialButton btnChinhSua, btnXoa;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maKH = getArguments().getInt(ARG_MA_KH, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session    = SessionManager.getInstance(requireContext());
        dbHelper   = DatabaseHelper.getInstance(requireContext());
        repository = new CustomerRepository(requireContext());

        tvInitials        = view.findViewById(R.id.tv_initials_detail);
        tvTenKhach        = view.findViewById(R.id.tv_ten_khach_detail);
        tvBadgeSoLanThue  = view.findViewById(R.id.tv_badge_so_lan_thue);
        tvSdt             = view.findViewById(R.id.tv_sdt_detail);
        tvEmail           = view.findViewById(R.id.tv_email_detail);
        tvCccd            = view.findViewById(R.id.tv_cccd_detail);
        tvDiaChi          = view.findViewById(R.id.tv_dia_chi_detail);
        tvNgaySinh        = view.findViewById(R.id.tv_ngay_sinh_detail);
        rvLichSu          = view.findViewById(R.id.rv_lich_su_dat_phong);
        tvNoHistory       = view.findViewById(R.id.tv_no_history);
        rowActionButtons  = view.findViewById(R.id.row_action_buttons);
        btnChinhSua       = view.findViewById(R.id.btn_chinh_sua);
        btnXoa            = view.findViewById(R.id.btn_xoa);

        setupBreadcrumb(view);
        applyPermission();
        rvLichSu.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadData();
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc != null) {
            TextView tv = bc.findViewById(R.id.tv_breadcrumb);
            if (tv != null) tv.setText("Trang chủ → Khách hàng → Chi tiết");
        }
    }

    private void applyPermission() {
        boolean canEdit = PermissionHelper.hasFullAccess(dbHelper, session.getVaiTro(),
                PermissionHelper.MODULE_QUAN_LY_KHACH);
        rowActionButtons.setVisibility(canEdit ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        executor.execute(() -> {
            KhachHang kh = repository.getCustomerById(maKH);
            List<DatPhong> bookings = repository.getRecentBookings(maKH);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (kh != null) bindCustomer(kh);
                bindBookings(bookings);
            });
        });
    }

    private void bindCustomer(KhachHang kh) {
        tvInitials.setText(kh.getInitials());
        tvTenKhach.setText(kh.getHoTen());
        tvBadgeSoLanThue.setText("Đã thuê " + kh.getSoLanThue() + " lần");
        tvSdt.setText(orDash(kh.getSdt()));
        tvEmail.setText(orDash(kh.getEmail()));
        tvCccd.setText(orDash(kh.getCccd()));
        tvDiaChi.setText(orDash(kh.getDiaChi()));

        String ngaySinh = orDash(kh.getNgaySinh());
        if (kh.getGioiTinh() != null && !kh.getGioiTinh().isEmpty()) {
            String gt = "Nu".equals(kh.getGioiTinh()) ? "Nữ"
                      : "Nam".equals(kh.getGioiTinh()) ? "Nam" : "Khác";
            ngaySinh = ngaySinh + "  •  " + gt;
        }
        tvNgaySinh.setText(ngaySinh);

        if (rowActionButtons.getVisibility() == View.VISIBLE) {
            btnChinhSua.setOnClickListener(v -> openEdit(kh.getMaKH()));
            btnXoa.setOnClickListener(v -> confirmDelete(kh));
        }
    }

    private void bindBookings(List<DatPhong> list) {
        if (list == null || list.isEmpty()) {
            rvLichSu.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
            return;
        }
        rvLichSu.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);
        rvLichSu.setAdapter(new BookingHistoryAdapter(list));
    }

    private void openEdit(int maKH) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, CustomerAddEditFragment.newInstance(maKH))
                .addToBackStack(null)
                .commit();
    }

    private void confirmDelete(KhachHang kh) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((TextView) dialogView.findViewById(R.id.tv_dialog_title)).setText("Xóa khách hàng");
        ((TextView) dialogView.findViewById(R.id.tv_dialog_message))
                .setText("Xóa \"" + kh.getHoTen() + "\"? Hành động này không thể hoàn tác.");

        Dialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView).create();

        dialogView.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_dialog_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            executor.execute(() -> {
                int result = repository.deleteCustomer(kh.getMaKH());
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    if (result > 0) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(requireContext(),
                                "Không thể xóa: khách hàng đang có đặt phòng liên quan.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
        dialog.show();
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    // ── Inline adapter for booking history rows ──────────────────────────────

    private static class BookingHistoryAdapter
            extends RecyclerView.Adapter<BookingHistoryAdapter.VH> {

        private final List<DatPhong> data;

        BookingHistoryAdapter(List<DatPhong> data) { this.data = data; }

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
                tvRoom     = v.findViewById(R.id.tv_compact_room);
                tvStatus   = v.findViewById(R.id.badge_compact_status);
            }
        }

        private static String mapTrangThai(String tt) {
            if (tt == null) return "";
            switch (tt) {
                case "SapDen":      return "Sắp đến";
                case "DangO":       return "Đang ở";
                case "DaTraPhong":  return "Đã trả";
                case "DaHuy":       return "Đã hủy";
                default:            return tt;
            }
        }

        private static int badgeDrawable(String tt) {
            if (tt == null) return R.drawable.bg_badge_pill;
            switch (tt) {
                case "SapDen":     return R.drawable.bg_badge_dadat;
                case "DangO":      return R.drawable.bg_badge_dangthue;
                case "DaTraPhong": return R.drawable.bg_badge_dathanhtoan;
                case "DaHuy":      return R.drawable.bg_badge_dahuy;
                default:           return R.drawable.bg_badge_pill;
            }
        }
    }
}
