package com.example.ql_homestay.ui.room;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.model.LoaiPhong;
import com.example.ql_homestay.model.Phong;
import com.example.ql_homestay.model.TienNghi;
import com.example.ql_homestay.repository.RoomRepository;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.snackbar.Snackbar;

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

    private int maPhong = -1;

    private FrameLayout flPickImage;
    private ImageView ivRoomImagePreview;
    private View llImagePlaceholder;
    private EditText etTenPhong, etGiaMoiDem, etSucChua, etDienTich, etTang, etMoTa;
    private Spinner spinnerLoaiPhong, spinnerTrangThai;
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
                    if (ivRoomImagePreview != null) {
                        ivRoomImagePreview.setImageURI(uri);
                        ivRoomImagePreview.setVisibility(View.VISIBLE);
                    }
                    if (llImagePlaceholder != null) llImagePlaceholder.setVisibility(View.GONE);
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
        setupAppBarTitle(view);
        setupImagePicker();
        setupCancelButton(view);
        setupSaveButton();
        setupBackButton(view);

        loadFormData();
    }

    private void bindViews(View view) {
        flPickImage         = view.findViewById(R.id.fl_pick_image);
        ivRoomImagePreview  = view.findViewById(R.id.iv_room_image_preview);
        llImagePlaceholder  = view.findViewById(R.id.ll_image_placeholder);
        etTenPhong          = view.findViewById(R.id.et_ten_phong);
        spinnerLoaiPhong    = view.findViewById(R.id.spinner_loai_phong);
        etGiaMoiDem         = view.findViewById(R.id.et_gia_moi_dem);
        etSucChua           = view.findViewById(R.id.et_suc_chua);
        etDienTich          = view.findViewById(R.id.et_dien_tich);
        etTang              = view.findViewById(R.id.et_tang);
        spinnerTrangThai    = view.findViewById(R.id.spinner_trang_thai);
        llTienNghiContainer = view.findViewById(R.id.ll_tien_nghi_container);
        etMoTa              = view.findViewById(R.id.et_mo_ta);
        btnSaveRoom         = view.findViewById(R.id.btn_save_room);
    }

    private void setupAppBarTitle(View view) {
        View appbarView = view.findViewById(R.id.appbar);
        if (appbarView != null) {
            android.widget.TextView tvTitle = appbarView.findViewById(R.id.tv_app_title);
            if (tvTitle != null) tvTitle.setText(maPhong > 0 ? "Sửa phòng" : "Thêm phòng");
        }
    }

    private void setupBackButton(View view) {
        View appbarView = view.findViewById(R.id.appbar);
        View btnBack = appbarView != null ? appbarView.findViewById(R.id.btn_appbar_back) : null;
        if (btnBack == null) btnBack = view.findViewById(R.id.btn_appbar_back);
        if (btnBack != null) {
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(v -> navigateBack());
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
        if (spinnerLoaiPhong == null || loaiPhongList == null) return;
        List<String> labels = new ArrayList<>();
        for (LoaiPhong lp : loaiPhongList) labels.add(lp.getTenLoai());
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoaiPhong.setAdapter(a);
    }

    private void setupTrangThaiSpinner() {
        if (spinnerTrangThai == null) return;
        String[] options = {"Trống", "Đang thuê", "Đã đặt"};
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrangThai.setAdapter(a);
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

        for (int i = 0; i < loaiPhongList.size(); i++) {
            if (loaiPhongList.get(i).getMaLoaiPhong() == phong.getMaLoaiPhong()) {
                spinnerLoaiPhong.setSelection(i);
                break;
            }
        }
        String tt = phong.getTrangThai();
        if ("Trong".equals(tt))         spinnerTrangThai.setSelection(0);
        else if ("DangThue".equals(tt)) spinnerTrangThai.setSelection(1);
        else if ("DaDat".equals(tt))    spinnerTrangThai.setSelection(2);
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

        int maLoaiPhong = loaiPhongList.isEmpty() ? 1
                : loaiPhongList.get(spinnerLoaiPhong.getSelectedItemPosition()).getMaLoaiPhong();
        String[] ttOptions = {"Trong", "DangThue", "DaDat"};
        String trangThai = ttOptions[spinnerTrangThai.getSelectedItemPosition()];

        int sucChua = 0, tang = 0; double dienTich = 0.0;
        try { sucChua  = Integer.parseInt(etSucChua.getText().toString().trim()); } catch (Exception ignored) {}
        try { dienTich = Double.parseDouble(etDienTich.getText().toString().trim()); } catch (Exception ignored) {}
        try { tang     = Integer.parseInt(etTang.getText().toString().trim()); } catch (Exception ignored) {}
        String moTa = etMoTa.getText() != null ? etMoTa.getText().toString().trim() : "";

        List<Integer> selectedTN = new ArrayList<>();
        for (CheckBox cb : tienNghiCheckBoxes)
            if (cb.isChecked()) selectedTN.add((Integer) cb.getTag());

        Phong phong = new Phong();
        if (maPhong > 0) phong.setMaPhong(maPhong);
        phong.setTenPhong(tenPhong);
        phong.setMaLoaiPhong(maLoaiPhong);
        phong.setGiaMoiDem(gia);
        phong.setSucChua(sucChua);
        phong.setDienTich(dienTich);
        phong.setTang(tang);
        phong.setTrangThai(trangThai);
        phong.setMoTa(moTa);

        dbExecutor.execute(() -> {
            boolean success = maPhong > 0
                    ? roomRepository.saveEditPhong(phong, selectedTN) > 0
                    : roomRepository.saveNewPhong(phong, selectedTN) > 0;

            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (success) {
                    Snackbar.make(requireView(),
                            maPhong > 0 ? "Cập nhật phòng thành công" : "Thêm phòng thành công",
                            Snackbar.LENGTH_LONG).show();
                    if (getParentFragmentManager().getBackStackEntryCount() > 0)
                        getParentFragmentManager().popBackStack();
                } else {
                    Snackbar.make(requireView(), "Lưu thất bại. Vui lòng thử lại.",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
