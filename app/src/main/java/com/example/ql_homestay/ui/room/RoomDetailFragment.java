package com.example.ql_homestay.ui.room;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.Phong;
import com.example.ql_homestay.model.TienNghi;
import com.example.ql_homestay.repository.RoomRepository;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RoomDetailFragment – Chi tiết phòng (B2).
 * - Header ảnh 220dp + overlay + back + badge
 * - Card thông tin: tên, loại, giá, sức chứa, diện tích, tầng
 * - HorizontalScrollView tiện nghi (chip)
 * - Mô tả
 * - Row 2 nút dưới: Chỉnh sửa (ẩn nếu ChiXem) + Đặt phòng (ẩn nếu DangThue/DaDat)
 */
public class RoomDetailFragment extends Fragment {

    private static final String ARG_MA_PHONG = "ma_phong";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private int maPhong;

    private ImageView ivRoomHeader;
    private TextView tvRoomStatusHeader;
    private TextView tvRoomName, tvRoomType, tvRoomPrice;
    private TextView tvSucChua, tvDienTich, tvTang;
    private LinearLayout llTienNghi;
    private TextView tvMoTa;
    private Button btnBookRoom;
    private MaterialButton btnEditRoom, btnDeleteRoom;

    private Phong currentPhong;
    private RoomRepository roomRepository;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static RoomDetailFragment newInstance(int maPhong) {
        RoomDetailFragment f = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_PHONG, maPhong);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            maPhong = getArguments().getInt(ARG_MA_PHONG, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        roomRepository = new RoomRepository(dbHelper);

        bindViews(view);
        setupBreadcrumb(view);

        if (maPhong > 0) loadRoomDetail();
    }

    private void bindViews(View view) {
        ivRoomHeader = view.findViewById(R.id.iv_room_header);
        tvRoomStatusHeader = view.findViewById(R.id.tv_room_status_header);
        tvRoomName = view.findViewById(R.id.tv_room_name);
        tvRoomType = view.findViewById(R.id.tv_room_type);
        tvRoomPrice = view.findViewById(R.id.tv_room_price);
        tvSucChua = view.findViewById(R.id.tv_suc_chua);
        tvDienTich = view.findViewById(R.id.tv_dien_tich);
        tvTang = view.findViewById(R.id.tv_tang);
        llTienNghi = view.findViewById(R.id.ll_tien_nghi);
        tvMoTa = view.findViewById(R.id.tv_mo_ta);
        btnEditRoom = view.findViewById(R.id.btn_edit_room);
        btnDeleteRoom = view.findViewById(R.id.btn_delete_room);
        btnBookRoom = view.findViewById(R.id.btn_book_room);
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Phòng → Chi tiết");
    }

    private void loadRoomDetail() {
        dbExecutor.execute(() -> {
            Phong phong = roomRepository.findPhongById(maPhong);
            List<TienNghi> tienNghiList = roomRepository.getTienNghiByPhong(maPhong);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (phong != null) {
                    currentPhong = phong;
                    bindData(phong, tienNghiList);
                    applyPermission(phong);
                }
            });
        });
    }

    private void bindData(Phong phong, List<TienNghi> tienNghiList) {
        // Header ảnh
        setRoomImage(ivRoomHeader, phong.getHinhAnh());

        // Badge trạng thái header
        applyBadgeStyle(tvRoomStatusHeader, phong.getTrangThai());
        tvRoomStatusHeader.setText(getTrangThaiLabel(phong.getTrangThai()));

        // Thông tin chính
        tvRoomName.setText(phong.getTenPhong());
        tvRoomType.setText(phong.getTenLoaiPhong() != null ? phong.getTenLoaiPhong() : "");

        String gia = NumberFormat.getNumberInstance(Locale.getDefault())
                .format((long) phong.getGiaMoiDem()) + " đ/đêm";
        tvRoomPrice.setText(gia);

        tvSucChua.setText(phong.getSucChua() + " người");
        tvDienTich.setText((int) phong.getDienTich() + " m²");
        tvTang.setText("Tầng " + phong.getTang());

        tvMoTa.setText(phong.getMoTa() != null ? phong.getMoTa() : "Không có mô tả.");

        // Tiện nghi chips
        buildTienNghiChips(tienNghiList);
    }

    private void buildTienNghiChips(List<TienNghi> list) {
        if (llTienNghi == null) return;
        llTienNghi.removeAllViews();
        if (list == null || list.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Không có tiện nghi");
            empty.setTextColor(requireContext().getResources().getColor(R.color.text_secondary, null));
            empty.setTextSize(13);
            llTienNghi.addView(empty);
            return;
        }
        for (TienNghi tn : list) {
            TextView chip = new TextView(requireContext());
            chip.setText(tn.getTenTienNghi());
            chip.setTextSize(12);
            chip.setPadding(20, 8, 20, 8);
            chip.setBackgroundResource(R.drawable.bg_chip_filter_active);
            chip.setTextColor(requireContext().getResources().getColor(R.color.text_primary, null));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);
            llTienNghi.addView(chip);
        }
    }

    /**
     * Áp dụng RBAC:
     * - Chỉnh sửa: chỉ Admin (ToanQuyen QuanLyPhong)
     * - Đặt phòng: Admin + LeTan, và chỉ hiện khi phòng Trống
     */
    private void applyPermission(Phong phong) {
        String vaiTro = sessionManager.getVaiTro();
        boolean canEdit = PermissionHelper.hasFullAccess(dbHelper, vaiTro,
                PermissionHelper.MODULE_QUAN_LY_PHONG);
        btnEditRoom.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        if (btnDeleteRoom != null) btnDeleteRoom.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        // Đặt phòng: có quyền ToanQuyen ở QuanLyDatPhong và phòng đang Trống
        boolean canBook = PermissionHelper.hasAccess(dbHelper, vaiTro,
                PermissionHelper.MODULE_QUAN_LY_DAT_PHONG, PermissionHelper.QUYEN_TOAN_QUYEN)
                && "Trong".equals(phong.getTrangThai());
        btnBookRoom.setVisibility(canBook ? View.VISIBLE : View.GONE);

        // Setup click listeners
        if (canEdit) {
            btnEditRoom.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(FRAGMENT_CONTAINER_ID, RoomAddEditFragment.newInstance(maPhong))
                            .addToBackStack(null)
                            .commit());

            if (btnDeleteRoom != null) {
                btnDeleteRoom.setOnClickListener(v -> showDeleteConfirm(phong));
            }
        }
        if (canBook) {
            btnBookRoom.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(FRAGMENT_CONTAINER_ID,
                                    com.example.ql_homestay.ui.booking.BookingAddEditFragment.newInstance(-1, maPhong))
                            .addToBackStack(null)
                            .commit());
        }
    }

    private void showDeleteConfirm(Phong phong) {
        // Chỉ cho xóa phòng có trạng thái Trống
        if (!"Trong".equals(phong.getTrangThai())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Không thể xóa")
                    .setMessage("Chỉ có thể xóa phòng có trạng thái \"Trống\".\nPhòng " + phong.getTenPhong() + " hiện đang " + getTrangThaiLabel(phong.getTrangThai()) + ".")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc chắn muốn xóa phòng " + phong.getTenPhong() + "?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteRoom(phong))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRoom(Phong phong) {
        dbExecutor.execute(() -> {
            int rows = roomRepository.deletePhong(phong.getMaPhong());
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (rows > 0) {
                    // Lưu context trước khi pop (sau pop fragment bị detach)
                    android.content.Context ctx = requireContext().getApplicationContext();
                    String msg = "Đã xóa phòng " + phong.getTenPhong();

                    // popBackStack() quay về RoomListFragment (replace() chỉ tạo 1 entry)
                    androidx.fragment.app.FragmentManager fm = getParentFragmentManager();
                    fm.popBackStack();

                    android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_LONG).show();
                } else {
                    Snackbar.make(requireView(),
                            "Xóa thất bại. Phòng có thể đang được sử dụng.",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setRoomImage(ImageView iv, String hinhAnh) {
        if (iv == null) return;
        if (hinhAnh == null || hinhAnh.isEmpty()) {
            iv.setImageResource(R.drawable.room_standard);
            return;
        }
        // Thử load từ URI (ảnh người dùng chọn từ thiết bị)
        if (hinhAnh.startsWith("content://") || hinhAnh.startsWith("file://")) {
            try {
                iv.setImageURI(android.net.Uri.parse(hinhAnh));
                if (iv.getDrawable() != null) return;
            } catch (Exception ignored) {}
        }
        // Fallback: map tên drawable
        switch (hinhAnh) {
            case "room_deluxe":     iv.setImageResource(R.drawable.room_deluxe);     break;
            case "room_deluxe_top": iv.setImageResource(R.drawable.room_deluxe_top); break;
            case "room_suite":      iv.setImageResource(R.drawable.room_suite);      break;
            default:                iv.setImageResource(R.drawable.room_standard);   break;
        }
    }

    private String getTrangThaiLabel(String tt) {
        if (tt == null) return "";
        switch (tt) {
            case "Trong":    return "Trống";
            case "DangThue": return "Đang thuê";
            case "DaDat":    return "Đã đặt";
            default:         return tt;
        }
    }

    private void applyBadgeStyle(TextView tv, String trangThai) {
        if (tv == null || trangThai == null) return;
        switch (trangThai) {
            case "Trong":
                tv.setBackgroundResource(R.drawable.bg_badge_trong);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_trong_text, null));
                break;
            case "DangThue":
                tv.setBackgroundResource(R.drawable.bg_badge_dangthue);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_dangthue_text, null));
                break;
            case "DaDat":
                tv.setBackgroundResource(R.drawable.bg_badge_dadat);
                tv.setTextColor(requireContext().getResources().getColor(R.color.badge_dadat_text, null));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
