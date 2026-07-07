package com.example.ql_homestay.ui.staff;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;
import com.example.ql_homestay.model.PhanCongCa;
import com.example.ql_homestay.repository.StaffRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Phân công ca làm việc.
 * Bảng 3 hàng (Sáng/Chiều/Tối) × 7 cột (T2–CN).
 * Load PhanCongCa hiện tại → Nút "Lưu" → replaceAll().
 *
 * maCa mapping (theo seed DatabaseHelper):
 *   1 = Sáng, 2 = Chiều, 3 = Tối
 * thuTrongTuan: 1=T2, 2=T3, 3=T4, 4=T5, 5=T6, 6=T7, 7=CN
 */
public class ShiftAssignmentFragment extends Fragment {

    private static final String ARG_MA_NV = "maNV";
    private static final String ARG_TUAN_BAT_DAU = "tuanBatDau";

    public static ShiftAssignmentFragment newInstance(int maNV) {
        return newInstance(maNV, null);
    }

    public static ShiftAssignmentFragment newInstance(int maNV, @Nullable String tuanBatDau) {
        ShiftAssignmentFragment f = new ShiftAssignmentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MA_NV, maNV);
        args.putString(ARG_TUAN_BAT_DAU, tuanBatDau);
        f.setArguments(args);
        return f;
    }

    private int maNV = -1;
    private String initialTuanBatDau = null;
    private StaffRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // CheckBoxes — Ca Sáng (maCa=1)
    private CheckBox cbSangT2, cbSangT3, cbSangT4, cbSangT5, cbSangT6, cbSangT7, cbSangCN;
    // Ca Chiều (maCa=2)
    private CheckBox cbChieuT2, cbChieuT3, cbChieuT4, cbChieuT5, cbChieuT6, cbChieuT7, cbChieuCN;
    // Ca Tối (maCa=3)
    private CheckBox cbToiT2, cbToiT3, cbToiT4, cbToiT5, cbToiT6, cbToiT7, cbToiCN;

    private MaterialButton btnLuu;
    private TextInputEditText etTuanBatDau;
    private String tuanBatDau;
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maNV = getArguments().getInt(ARG_MA_NV, -1);
            initialTuanBatDau = getArguments().getString(ARG_TUAN_BAT_DAU);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shift_assignment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new StaffRepository(requireContext());

        bindViews(view);
        setupBreadcrumb(view);
        setupWeekPicker();

        btnLuu.setOnClickListener(v -> saveShifts());
        loadCurrentShifts();
    }

    private void bindViews(View v) {
        // Ca Sáng
        cbSangT2 = v.findViewById(R.id.cb_sang_t2); cbSangT3 = v.findViewById(R.id.cb_sang_t3);
        cbSangT4 = v.findViewById(R.id.cb_sang_t4); cbSangT5 = v.findViewById(R.id.cb_sang_t5);
        cbSangT6 = v.findViewById(R.id.cb_sang_t6); cbSangT7 = v.findViewById(R.id.cb_sang_t7);
        cbSangCN = v.findViewById(R.id.cb_sang_cn);
        // Ca Chiều
        cbChieuT2 = v.findViewById(R.id.cb_chieu_t2); cbChieuT3 = v.findViewById(R.id.cb_chieu_t3);
        cbChieuT4 = v.findViewById(R.id.cb_chieu_t4); cbChieuT5 = v.findViewById(R.id.cb_chieu_t5);
        cbChieuT6 = v.findViewById(R.id.cb_chieu_t6); cbChieuT7 = v.findViewById(R.id.cb_chieu_t7);
        cbChieuCN = v.findViewById(R.id.cb_chieu_cn);
        // Ca Tối
        cbToiT2 = v.findViewById(R.id.cb_toi_t2); cbToiT3 = v.findViewById(R.id.cb_toi_t3);
        cbToiT4 = v.findViewById(R.id.cb_toi_t4); cbToiT5 = v.findViewById(R.id.cb_toi_t5);
        cbToiT6 = v.findViewById(R.id.cb_toi_t6); cbToiT7 = v.findViewById(R.id.cb_toi_t7);
        cbToiCN = v.findViewById(R.id.cb_toi_cn);
        btnLuu = v.findViewById(R.id.btn_luu);
        etTuanBatDau = v.findViewById(R.id.et_tuan_bat_dau);
    }

    private void setupBreadcrumb(View v) {
        View bc = v.findViewById(R.id.breadcrumb);
        if (bc == null) return;
        TextView tv = bc.findViewById(R.id.tv_breadcrumb);
        if (tv != null) tv.setText("Trang chủ → Nhân viên → Phân công ca");
    }

    private void setupWeekPicker() {
        Calendar monday = initialTuanBatDau == null || "1970-01-05".equals(initialTuanBatDau)
                ? mondayOf(Calendar.getInstance())
                : calendarFromDbDate(initialTuanBatDau);
        setWeek(monday);
        etTuanBatDau.setOnClickListener(v -> new DatePickerDialog(requireContext(),
                (picker, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    setWeek(mondayOf(selected));
                    loadCurrentShifts();
                },
                monday.get(Calendar.YEAR), monday.get(Calendar.MONTH), monday.get(Calendar.DAY_OF_MONTH))
                .show());
    }

    private Calendar calendarFromDbDate(String date) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dbDateFormat.parse(date));
        } catch (Exception ignored) {
            return mondayOf(Calendar.getInstance());
        }
        return c;
    }

    private void setWeek(Calendar monday) {
        tuanBatDau = dbDateFormat.format(monday.getTime());
        Calendar sunday = (Calendar) monday.clone();
        sunday.add(Calendar.DAY_OF_MONTH, 6);
        etTuanBatDau.setText(tuanBatDau + " - " + dbDateFormat.format(sunday.getTime()));
    }

    private Calendar mondayOf(Calendar date) {
        Calendar c = (Calendar) date.clone();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        return c;
    }

    private void loadCurrentShifts() {
        executor.execute(() -> {
            List<PhanCongCa> list = repository.getShiftAssignments(maNV, tuanBatDau);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                clearAll();
                for (PhanCongCa pc : list) {
                    CheckBox cb = getCheckBox(pc.getMaCa(), pc.getThuTrongTuan());
                    if (cb != null) cb.setChecked(true);
                }
            });
        });
    }

    private void saveShifts() {
        List<PhanCongCa> list = collectChecked();
        btnLuu.setEnabled(false);
        executor.execute(() -> {
            repository.saveShiftAssignments(maNV, tuanBatDau, list);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                btnLuu.setEnabled(true);
                Toast.makeText(requireContext(), "Phân công ca đã được lưu.", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        });
    }

    /** Thu thập tất cả CheckBox được tick thành danh sách PhanCongCa. */
    private List<PhanCongCa> collectChecked() {
        List<PhanCongCa> result = new ArrayList<>();
        // Sáng (maCa=1)
        CheckBox[] sang = {cbSangT2, cbSangT3, cbSangT4, cbSangT5, cbSangT6, cbSangT7, cbSangCN};
        for (int i = 0; i < 7; i++) {
            if (sang[i] != null && sang[i].isChecked())
                result.add(new PhanCongCa(maNV, 1, i + 1));
        }
        // Chiều (maCa=2)
        CheckBox[] chieu = {cbChieuT2, cbChieuT3, cbChieuT4, cbChieuT5, cbChieuT6, cbChieuT7, cbChieuCN};
        for (int i = 0; i < 7; i++) {
            if (chieu[i] != null && chieu[i].isChecked())
                result.add(new PhanCongCa(maNV, 2, i + 1));
        }
        // Tối (maCa=3)
        CheckBox[] toi = {cbToiT2, cbToiT3, cbToiT4, cbToiT5, cbToiT6, cbToiT7, cbToiCN};
        for (int i = 0; i < 7; i++) {
            if (toi[i] != null && toi[i].isChecked())
                result.add(new PhanCongCa(maNV, 3, i + 1));
        }
        return result;
    }

    private void clearAll() {
        CheckBox[] all = {
                cbSangT2, cbSangT3, cbSangT4, cbSangT5, cbSangT6, cbSangT7, cbSangCN,
                cbChieuT2, cbChieuT3, cbChieuT4, cbChieuT5, cbChieuT6, cbChieuT7, cbChieuCN,
                cbToiT2, cbToiT3, cbToiT4, cbToiT5, cbToiT6, cbToiT7, cbToiCN
        };
        for (CheckBox cb : all) if (cb != null) cb.setChecked(false);
    }

    @Nullable
    private CheckBox getCheckBox(int maCa, int thu) {
        switch (maCa) {
            case 1:
                switch (thu) {
                    case 1: return cbSangT2; case 2: return cbSangT3; case 3: return cbSangT4;
                    case 4: return cbSangT5; case 5: return cbSangT6; case 6: return cbSangT7;
                    case 7: return cbSangCN;
                } break;
            case 2:
                switch (thu) {
                    case 1: return cbChieuT2; case 2: return cbChieuT3; case 3: return cbChieuT4;
                    case 4: return cbChieuT5; case 5: return cbChieuT6; case 6: return cbChieuT7;
                    case 7: return cbChieuCN;
                } break;
            case 3:
                switch (thu) {
                    case 1: return cbToiT2; case 2: return cbToiT3; case 3: return cbToiT4;
                    case 4: return cbToiT5; case 5: return cbToiT6; case 6: return cbToiT7;
                    case 7: return cbToiCN;
                } break;
        }
        return null;
    }

    @Override
    public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
