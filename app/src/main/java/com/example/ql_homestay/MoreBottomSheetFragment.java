package com.example.ql_homestay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * MoreBottomSheetFragment — Bottom Sheet cho menu "Hơn nữa".
 * Hiển thị các module phụ: Thanh toán / Nhân viên / Thống kê / Tài khoản.
 * Phân quyền RBAC: ẩn item theo role (implement sau khi các module sẵn sàng).
 */
public class MoreBottomSheetFragment extends BottomSheetDialogFragment {

    public static MoreBottomSheetFragment newInstance() {
        return new MoreBottomSheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Bind các nút module theo role — implement ở Lộ trình 1, 2, 3
        // Tạm thời: click bất kỳ item → gọi navigateToModule() ở MainActivity
    }
}