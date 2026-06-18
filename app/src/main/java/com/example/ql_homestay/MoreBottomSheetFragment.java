package com.example.ql_homestay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * MoreBottomSheetFragment — Bottom Sheet cho tab "Hơn nữa".
 * Hiển thị các module phụ: Thanh toán, Nhân viên, Thống kê, Tài khoản.
 * Các item được ẩn/hiện theo quyền của role hiện tại (RBAC runtime).
 * Cách mở từ MainActivity:
 *   MoreBottomSheetFragment sheet = MoreBottomSheetFragment.newInstance();
 *   sheet.show(getSupportFragmentManager(), "more_sheet");
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
        // Layout đơn giản tạo runtime (không cần file XML riêng)
        // Nếu muốn, có thể tách ra fragment_more_sheet.xml
        return inflater.inflate(R.layout.fragment_more_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = SessionManager.getInstance(requireContext());
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        String vaiTro = session.getVaiTro();

        // Thanh toán
        LinearLayout rowPayment = view.findViewById(R.id.row_payment);
        if (rowPayment != null) {
            boolean showPayment = PermissionHelper.canAccess(
                    dbHelper.getReadableDatabase(), vaiTro,
                    PermissionHelper.MODULE_HOA_DON);
            rowPayment.setVisibility(showPayment ? View.VISIBLE : View.GONE);
            rowPayment.setOnClickListener(v -> navigateTo("payment"));
        }

        // Nhân viên
        LinearLayout rowStaff = view.findViewById(R.id.row_staff);
        if (rowStaff != null) {
            boolean showStaff = PermissionHelper.canAccess(
                    dbHelper.getReadableDatabase(), vaiTro,
                    PermissionHelper.MODULE_NHAN_VIEN);
            rowStaff.setVisibility(showStaff ? View.VISIBLE : View.GONE);
            rowStaff.setOnClickListener(v -> navigateTo("staff"));
        }

        // Thống kê
        LinearLayout rowStats = view.findViewById(R.id.row_statistics);
        if (rowStats != null) {
            boolean showStats = PermissionHelper.canAccess(
                    dbHelper.getReadableDatabase(), vaiTro,
                    PermissionHelper.MODULE_BAO_CAO);
            rowStats.setVisibility(showStats ? View.VISIBLE : View.GONE);
            rowStats.setOnClickListener(v -> navigateTo("statistics"));
        }

        // Tài khoản
        LinearLayout rowAccount = view.findViewById(R.id.row_account);
        if (rowAccount != null) {
            boolean showAccount = PermissionHelper.canAccess(
                    dbHelper.getReadableDatabase(), vaiTro,
                    PermissionHelper.MODULE_CAI_DAT);
            rowAccount.setVisibility(showAccount ? View.VISIBLE : View.GONE);
            rowAccount.setOnClickListener(v -> navigateTo("account"));
        }

        // Đăng xuất
        LinearLayout rowLogout = view.findViewById(R.id.row_logout);
        if (rowLogout != null) {
            rowLogout.setOnClickListener(v -> {
                dismiss();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            });
        }
    }

    private void navigateTo(String moduleKey) {
        dismiss();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToModule(moduleKey);
        }
    }
}
