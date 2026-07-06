package com.example.ql_homestay.ui.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.KhachHang;
import com.example.ql_homestay.repository.CustomerRepository;
import com.example.ql_homestay.util.AvatarHelper;
import com.example.ql_homestay.util.ImagePickerHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerAddEditFragment extends Fragment {

    private static final String ARG_MA_KH = "maKH";

    public static CustomerAddEditFragment newInstance(int maKH) {
        CustomerAddEditFragment f = new CustomerAddEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_KH, maKH);
        f.setArguments(args);
        return f;
    }

    private int maKH = -1;
    private CustomerRepository repository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private FrameLayout flAvatarPicker;
    private ImageView ivAvatarPick;
    private View tvAvatarPlaceholder;
    private TextInputLayout tilHoTen, tilSdt;
    private TextInputEditText etHoTen, etSdt, etEmail, etCccd, etDiaChi, etNgaySinh;
    private MaterialAutoCompleteTextView dropdownGioiTinh;
    private MaterialButton btnLuu;
    
    private byte[] avatarBytes = null;
    private String existingAvatar = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    avatarBytes = ImagePickerHelper.handleImageResult(
                            requireContext(), imageUri, ivAvatarPick, tvAvatarPlaceholder);
                }
            });

    private static final String[] GIOI_TINH_DISPLAY = {"Nam", "Nữ", "Khác"};
    private static final String[] GIOI_TINH_VALUE   = {"Nam", "Nu",  "Khac"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) maKH = getArguments().getInt(ARG_MA_KH, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new CustomerRepository(requireContext());

        flAvatarPicker      = view.findViewById(R.id.fl_avatar_picker);
        ivAvatarPick        = view.findViewById(R.id.iv_avatar_pick);
        tvAvatarPlaceholder = view.findViewById(R.id.tv_avatar_placeholder);
        tilHoTen         = view.findViewById(R.id.til_ho_ten);
        tilSdt           = view.findViewById(R.id.til_sdt);
        etHoTen          = view.findViewById(R.id.et_ho_ten);
        etSdt            = view.findViewById(R.id.et_sdt);
        etEmail          = view.findViewById(R.id.et_email);
        etCccd           = view.findViewById(R.id.et_cccd);
        etDiaChi         = view.findViewById(R.id.et_dia_chi);
        etNgaySinh       = view.findViewById(R.id.et_ngay_sinh);
        dropdownGioiTinh = view.findViewById(R.id.dropdown_gioi_tinh);
        btnLuu           = view.findViewById(R.id.btn_luu);

        setupBreadcrumb(view);
        setupAvatarPicker();
        setupGenderDropdown();
        setupDatePicker();

        if (maKH > 0) {
            btnLuu.setText("Cập nhật");
            loadExistingData();
        }

        btnLuu.setOnClickListener(v -> onSave());
    }

    private void setupBreadcrumb(View view) {
        View bc = view.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        android.widget.TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText(maKH > 0
                ? "Trang chủ → Khách hàng → Chỉnh sửa"
                : "Trang chủ → Khách hàng → Thêm mới");
    }
    
    private void setupAvatarPicker() {
        flAvatarPicker.setOnClickListener(v -> 
            ImagePickerHelper.pickImage(this, imagePickerLauncher));
    }

    private void setupGenderDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, GIOI_TINH_DISPLAY);
        dropdownGioiTinh.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etNgaySinh.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etNgaySinh.setText(
                            String.format("%02d/%02d/%04d", d, m + 1, y)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    private void loadExistingData() {
        executor.execute(() -> {
            KhachHang kh = repository.getCustomerById(maKH);
            mainHandler.post(() -> {
                if (!isAdded() || kh == null) return;
                existingAvatar = kh.getAvatar();
                AvatarHelper.loadAvatarPreview(requireContext(), existingAvatar, ivAvatarPick, tvAvatarPlaceholder);
                etHoTen.setText(kh.getHoTen());
                etSdt.setText(kh.getSdt());
                etEmail.setText(kh.getEmail());
                etCccd.setText(kh.getCccd());
                etDiaChi.setText(kh.getDiaChi());
                etNgaySinh.setText(kh.getNgaySinh());
                // Pre-select gender dropdown
                String gt = kh.getGioiTinh();
                for (int i = 0; i < GIOI_TINH_VALUE.length; i++) {
                    if (GIOI_TINH_VALUE[i].equals(gt)) {
                        dropdownGioiTinh.setText(GIOI_TINH_DISPLAY[i], false);
                        break;
                    }
                }
            });
        });
    }

    private void onSave() {
        String hoTen = text(etHoTen);
        String sdt   = text(etSdt);

        boolean valid = true;
        if (TextUtils.isEmpty(hoTen)) {
            tilHoTen.setError("Họ và tên không được để trống");
            valid = false;
        } else {
            tilHoTen.setError(null);
        }
        if (TextUtils.isEmpty(sdt)) {
            tilSdt.setError("Số điện thoại không được để trống");
            valid = false;
        } else {
            tilSdt.setError(null);
        }
        if (!valid) return;

        KhachHang kh = new KhachHang();
        kh.setMaKH(maKH > 0 ? maKH : 0);
        kh.setHoTen(hoTen);
        kh.setSdt(sdt);
        kh.setEmail(text(etEmail));
        kh.setCccd(text(etCccd));
        kh.setDiaChi(text(etDiaChi));
        kh.setNgaySinh(text(etNgaySinh));
        kh.setGioiTinh(selectedGioiTinhValue());
        // Convert byte[] to Base64 string
        if (avatarBytes != null && avatarBytes.length > 0) {
            kh.setAvatar(Base64.encodeToString(avatarBytes, Base64.DEFAULT));
        } else {
            kh.setAvatar(existingAvatar);
        }

        btnLuu.setEnabled(false);
        executor.execute(() -> {
            boolean success = maKH > 0
                    ? repository.updateCustomer(kh) > 0
                    : repository.addCustomer(kh) > 0;
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (success) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    btnLuu.setEnabled(true);
                    Toast.makeText(requireContext(), "Lưu thất bại, vui lòng thử lại.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String selectedGioiTinhValue() {
        String display = dropdownGioiTinh.getText().toString().trim();
        for (int i = 0; i < GIOI_TINH_DISPLAY.length; i++) {
            if (GIOI_TINH_DISPLAY[i].equals(display)) return GIOI_TINH_VALUE[i];
        }
        return "";
    }
}
