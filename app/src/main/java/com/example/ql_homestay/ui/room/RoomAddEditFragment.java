package com.example.ql_homestay.ui.room;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.MainActivity;
import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.LoaiPhong;
import com.example.ql_homestay.model.Phong;
import com.example.ql_homestay.model.TienNghi;
import com.example.ql_homestay.repository.RoomRepository;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RoomAddEditFragment – Thêm/Sửa phòng (B3).
 * Truyền maPhong = -1 để tạo mới; maPhong > 0 để chỉnh sửa.
 */
public class RoomAddEditFragment extends Fragment {

    private static final String ARG_MA_PHONG = "ma_phong";

    /** Flag tĩnh để báo cho RoomListFragment biết vừa thêm phòng mới → scroll xuống cuối */
    public static volatile boolean sRoomJustAdded = false;

    private int maPhong = -1;

    private FrameLayout flPickImage;
    private ImageView ivRoomImagePreview;
    private View llImagePlaceholder;
    private TextView btnClearImage;
    private EditText etTenPhong, etGiaMoiDem, etSucChua, etDienTich, etTang, etMoTa;
    private MaterialAutoCompleteTextView actvLoaiPhong, actvTrangThai;
    private LinearLayout llTienNghiContainer;
    private Button btnSaveRoom;

    private List<LoaiPhong> loaiPhongList = new ArrayList<>();
    private List<TienNghi> tienNghiList   = new ArrayList<>();
    private final List<CheckBox> tienNghiCheckBoxes = new ArrayList<>();
    private Uri selectedImageUri = null;

    private RoomRepository roomRepository;
    private DatabaseHelper dbHelper;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Launcher để mở thư viện ảnh */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    showImagePreview(uri);
                }
            });

    public static RoomAddEditFragment newInstance(int maPhong) {
        RoomAddEditFragment f = new RoomAddEditFragment();
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
        return inflater.inflate(R.layout.fragment_room_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper       = DatabaseHelper.getInstance(requireContext());
        roomRepository = new RoomRepository(dbHelper);

        bindViews(view);
        updateAppBarTitle();
        setupImagePicker();
        setupCancelButton(view);
        setupSaveButton();

        loadFormData();
    }

    private void bindViews(View view) {
        flPickImage         = view.findViewById(R.id.fl_pick_image);
        ivRoomImagePreview  = view.findViewById(R.id.iv_room_image_preview);
        llImagePlaceholder  = view.findViewById(R.id.ll_image_placeholder);
        btnClearImage       = view.findViewById(R.id.btn_clear_image);
        etTenPhong          = view.findViewById(R.id.et_ten_phong);
        actvLoaiPhong       = view.findViewById(R.id.actvLoaiPhong);
        etGiaMoiDem         = view.findViewById(R.id.et_gia_moi_dem);
        etSucChua           = view.findViewById(R.id.et_suc_chua);
        etDienTich          = view.findViewById(R.id.et_dien_tich);
        etTang              = view.findViewById(R.id.et_tang);
        actvTrangThai       = view.findViewById(R.id.actvTrangThai);
        llTienNghiContainer = view.findViewById(R.id.ll_tien_nghi_container);
        etMoTa              = view.findViewById(R.id.et_mo_ta);
        btnSaveRoom         = view.findViewById(R.id.btn_save_room);
    }

    /** Cập nhật tiêu đề AppBar dùng chung của MainActivity */
    private void updateAppBarTitle() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setAppBarTitle(
                    maPhong > 0 ? "Sửa phòng" : "Thêm phòng");
        }
    }

    private void setupCancelButton(View view) {
        Button btnCancel = view.findViewById(R.id.btn_cancel_room);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> navigateBack());
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }

    private void setupImagePicker() {
        if (flPickImage != null) {
            flPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }
        if (btnClearImage != null) {
            btnClearImage.setOnClickListener(v -> clearImagePreview());
        }
    }

    /** Hiện ảnh đã chọn / đã lưu */
    private void showImagePreview(Uri uri) {
        if (ivRoomImagePreview != null) {
            ivRoomImagePreview.setImageURI(uri);
            ivRoomImagePreview.setVisibility(View.VISIBLE);
        }
        if (llImagePlaceholder != null) llImagePlaceholder.setVisibility(View.GONE);
        if (btnClearImage != null) btnClearImage.setVisibility(View.VISIBLE);
    }

    /** Xóa ảnh, trả về placeholder */
    private void clearImagePreview() {
        selectedImageUri = null;
        if (ivRoomImagePreview != null) {
            ivRoomImagePreview.setImageURI(null);
            ivRoomImagePreview.setVisibility(View.GONE);
        }
        if (llImagePlaceholder != null) llImagePlaceholder.setVisibility(View.VISIBLE);
        if (btnClearImage != null) btnClearImage.setVisibility(View.GONE);
    }

    private void setupSaveButton() {
        if (btnSaveRoom != null) btnSaveRoom.setOnClickListener(v -> validateAndSave());
    }

    private void loadFormData() {
        dbExecutor.execute(() -> {
            List<LoaiPhong> loais    = roomRepository.getAllLoaiPhong();
            List<TienNghi> tenNghis  = roomRepository.getAllTienNghi();
            Phong existing           = maPhong > 0 ? roomRepository.findPhongById(maPhong) : null;
            List<TienNghi> existTN   = maPhong > 0 ? roomRepository.getTienNghiByPhong(maPhong) : null;

            mainHandler.post(() -> {
                if (!isAdded()) return;
                loaiPhongList = loais;
                tienNghiList  = tenNghis;
                setupLoaiPhongSpinner();
                setupTrangThaiSpinner();
                buildTienNghiCheckBoxes(existTN);
                if (existing != null) fillFormForEdit(existing);
            });
        });
    }

    private void setupLoaiPhongSpinner() {
        if (actvLoaiPhong == null || loaiPhongList == null) return;
        List<String> labels = new ArrayList<>();
        for (LoaiPhong lp : loaiPhongList) labels.add(lp.getTenLoai());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels);
        actvLoaiPhong.setAdapter(adapter);
        // Chọn mặc định item đầu tiên
        if (!labels.isEmpty() && (actvLoaiPhong.getText() == null || actvLoaiPhong.getText().toString().isEmpty())) {
            actvLoaiPhong.setText(labels.get(0), false);
        }
    }

    private void setupTrangThaiSpinner() {
        if (actvTrangThai == null) return;
        String[] options = {"Trống", "Đang thuê", "Đã đặt"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, options);
        actvTrangThai.setAdapter(adapter);
        // Chọn mặc định "Trống"
        if (actvTrangThai.getText() == null || actvTrangThai.getText().toString().isEmpty()) {
            actvTrangThai.setText(options[0], false);
        }
    }

    /** Tạo CheckBox tiện nghi theo 2 cột (2 item/hàng). */
    private void buildTienNghiCheckBoxes(List<TienNghi> existingTN) {
        if (llTienNghiContainer == null || tienNghiList == null) return;
        llTienNghiContainer.removeAllViews();
        tienNghiCheckBoxes.clear();

        List<Integer> existIds = new ArrayList<>();
        if (existingTN != null)
            for (TienNghi tn : existingTN) existIds.add(tn.getMaTienNghi());

        LinearLayout currentRow = null;
        for (int i = 0; i < tienNghiList.size(); i++) {
            TienNghi tn = tienNghiList.get(i);
            if (i % 2 == 0) {
                currentRow = new LinearLayout(requireContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                llTienNghiContainer.addView(currentRow);
            }
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(tn.getTenTienNghi());
            cb.setTextSize(14);
            cb.setTag(tn.getMaTienNghi());
            cb.setChecked(existIds.contains(tn.getMaTienNghi()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(0, 4, 0, 4);
            cb.setLayoutParams(lp);
            if (currentRow != null) currentRow.addView(cb);
            tienNghiCheckBoxes.add(cb);
        }
    }

    private void fillFormForEdit(Phong phong) {
        etTenPhong.setText(phong.getTenPhong());
        etGiaMoiDem.setText(String.valueOf((long) phong.getGiaMoiDem()));
        etSucChua.setText(String.valueOf(phong.getSucChua()));
        etDienTich.setText(String.valueOf((int) phong.getDienTich()));
        etTang.setText(String.valueOf(phong.getTang()));
        if (phong.getMoTa() != null) etMoTa.setText(phong.getMoTa());

        // Hiện ảnh: ưu tiên URI, fallback sang drawable name
        String hinhAnh = phong.getHinhAnh();
        if (hinhAnh != null && !hinhAnh.isEmpty()) {
            if (hinhAnh.startsWith("content://") || hinhAnh.startsWith("file://")) {
                // Ảnh từ thư viện thiết bị — thử load qua URI
                try {
                    Uri savedUri = Uri.parse(hinhAnh);
                    selectedImageUri = savedUri;
                    showImagePreview(savedUri);
                } catch (Exception ignored) {
                    loadDrawablePreview(hinhAnh);
                }
            } else {
                // Tên drawable có sẵn (room_standard, room_deluxe, ...)
                loadDrawablePreview(hinhAnh);
            }
        }

        // Loại phòng
        for (int i = 0; i < loaiPhongList.size(); i++) {
            if (loaiPhongList.get(i).getMaLoaiPhong() == phong.getMaLoaiPhong()) {
                if (actvLoaiPhong != null)
                    actvLoaiPhong.setText(loaiPhongList.get(i).getTenLoai(), false);
                break;
            }
        }

        // Trạng thái
        if (actvTrangThai != null) {
            String tt = phong.getTrangThai();
            if ("Trong".equals(tt))         actvTrangThai.setText("Trống", false);
            else if ("DangThue".equals(tt)) actvTrangThai.setText("Đang thuê", false);
            else if ("DaDat".equals(tt))    actvTrangThai.setText("Đã đặt", false);
        }
    }

    /**
     * Tải ảnh từ drawable resource theo tên và hiển thị lên preview.
     * selectedImageUri giữ nguyên null vì đây là ảnh mặc định (không cần lưu lại URI).
     */
    private void loadDrawablePreview(String drawableName) {
        if (ivRoomImagePreview == null) return;
        int resId;
        switch (drawableName) {
            case "room_deluxe":     resId = R.drawable.room_deluxe;     break;
            case "room_deluxe_top": resId = R.drawable.room_deluxe_top; break;
            case "room_suite":      resId = R.drawable.room_suite;      break;
            default:                resId = R.drawable.room_standard;   break;
        }
        ivRoomImagePreview.setImageResource(resId);
        ivRoomImagePreview.setVisibility(View.VISIBLE);
        if (llImagePlaceholder != null) llImagePlaceholder.setVisibility(View.GONE);
        if (btnClearImage != null) btnClearImage.setVisibility(View.VISIBLE);
        // Ghi lại tên drawable vào selectedImageUri tạm thời để validateAndSave() có thể lưu lại đúng
        selectedImageUri = Uri.parse("drawable://" + drawableName);
    }

    private void validateAndSave() {
        String tenPhong = etTenPhong.getText() != null ? etTenPhong.getText().toString().trim() : "";
        String giaStr   = etGiaMoiDem.getText() != null ? etGiaMoiDem.getText().toString().trim() : "";

        if (tenPhong.isEmpty()) { etTenPhong.setError("Nhập tên phòng"); etTenPhong.requestFocus(); return; }
        if (giaStr.isEmpty())   { etGiaMoiDem.setError("Nhập giá/đêm"); etGiaMoiDem.requestFocus(); return; }

        double gia;
        try { gia = Double.parseDouble(giaStr); } catch (NumberFormatException e) {
            etGiaMoiDem.setError("Giá không hợp lệ"); return;
        }

        int maLoaiPhong = 1;
        if (!loaiPhongList.isEmpty() && actvLoaiPhong != null) {
            String selectedLoai = actvLoaiPhong.getText() != null ? actvLoaiPhong.getText().toString() : "";
            for (LoaiPhong lp : loaiPhongList) {
                if (lp.getTenLoai().equals(selectedLoai)) {
                    maLoaiPhong = lp.getMaLoaiPhong();
                    break;
                }
            }
        }

        // Map label hiển thị → giá trị DB
        String trangThai = "Trong"; // mặc định
        if (actvTrangThai != null) {
            String selectedTT = actvTrangThai.getText() != null ? actvTrangThai.getText().toString() : "";
            if ("Đang thuê".equals(selectedTT))   trangThai = "DangThue";
            else if ("Đã đặt".equals(selectedTT)) trangThai = "DaDat";
            else                                   trangThai = "Trong";
        }

        int sucChua = 0, tang = 0; double dienTich = 0.0;
        try { sucChua  = Integer.parseInt(etSucChua.getText().toString().trim()); } catch (Exception ignored) {}
        try { dienTich = Double.parseDouble(etDienTich.getText().toString().trim()); } catch (Exception ignored) {}
        try { tang     = Integer.parseInt(etTang.getText().toString().trim()); } catch (Exception ignored) {}
        String moTa = etMoTa.getText() != null ? etMoTa.getText().toString().trim() : "";

        List<Integer> selectedTN = new ArrayList<>();
        for (CheckBox cb : tienNghiCheckBoxes)
            if (cb.isChecked()) selectedTN.add((Integer) cb.getTag());

        String hinhAnhStr;
        if (selectedImageUri == null) {
            hinhAnhStr = null;
        } else {
            String uriStr = selectedImageUri.toString();
            if (uriStr.startsWith("drawable://")) {
                // Ảnh drawable có sẵn — lưu tên drawable (bỏ prefix "drawable://")
                hinhAnhStr = uriStr.substring("drawable://".length());
            } else {
                // Ảnh từ thư viện thiết bị — lưu full URI
                hinhAnhStr = uriStr;
            }
        }

        Phong phong = new Phong();
        if (maPhong > 0) phong.setMaPhong(maPhong);
        phong.setTenPhong(tenPhong);
        phong.setMaLoaiPhong(maLoaiPhong);
        phong.setGiaMoiDem(gia);
        phong.setSucChua(sucChua);
        phong.setDienTich(dienTich);
        phong.setTang(tang);
        phong.setTrangThai(trangThai);
        phong.setHinhAnh(hinhAnhStr);
        phong.setMoTa(moTa);

        // Disable nút tránh double-click
        if (btnSaveRoom != null) btnSaveRoom.setEnabled(false);

        final boolean isNew = (maPhong <= 0);

        dbExecutor.execute(() -> {
            // Kiểm tra trùng tên phòng (không phân biệt hoa thường)
            boolean isDuplicate = roomRepository.isTenPhongDuplicate(tenPhong, maPhong);
            if (isDuplicate) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    if (btnSaveRoom != null) btnSaveRoom.setEnabled(true);
                    etTenPhong.setError("Tên phòng \"" + tenPhong + "\" đã tồn tại");
                    etTenPhong.requestFocus();
                });
                return;
            }

            boolean success = isNew
                    ? roomRepository.saveNewPhong(phong, selectedTN) > 0
                    : roomRepository.saveEditPhong(phong, selectedTN) > 0;

            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (btnSaveRoom != null) btnSaveRoom.setEnabled(true);
                if (success) {
                    // Gửi tín hiệu về RoomListFragment để scroll xuống cuối (chỉ khi thêm mới)
                    if (isNew) {
                        Bundle result = new Bundle();
                        result.putBoolean("room_added", true);
                        getParentFragmentManager().setFragmentResult("room_saved", result);
                        sRoomJustAdded = true;   // flag tĩnh, đọc trong RoomListFragment.onResume
                    }
                    Snackbar.make(requireView(),
                            isNew ? "Thêm phòng thành công" : "Cập nhật phòng thành công",
                            Snackbar.LENGTH_LONG).show();
                    navigateBack();
                } else {
                    Snackbar.make(requireView(), "Lưu thất bại. Vui lòng thử lại.",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Khôi phục tiêu đề AppBar về "Lala House" khi rời khỏi màn hình Add/Edit
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).resetAppBarTitle();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
