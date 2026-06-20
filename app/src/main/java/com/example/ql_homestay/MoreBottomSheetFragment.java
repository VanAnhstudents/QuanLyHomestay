package com.example.ql_homestay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.ui.account.AccountListFragment;
import com.example.ql_homestay.ui.staff.StaffListFragment;
import com.example.ql_homestay.util.PermissionHelper;
import com.example.ql_homestay.util.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

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

        SessionManager session = SessionManager.getInstance(requireContext());
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        String vaiTro = session.getVaiTro();

        // Nhân viên — ẩn nếu không có quyền truy cập
        View rowStaff = view.findViewById(R.id.row_staff);
        boolean canSeeStaff = PermissionHelper.canAccess(
                dbHelper.getReadableDatabase(), vaiTro, PermissionHelper.MODULE_NHAN_VIEN);
        rowStaff.setVisibility(canSeeStaff ? View.VISIBLE : View.GONE);
        rowStaff.setOnClickListener(v -> navigate(new StaffListFragment()));

        // Tài khoản — chỉ Admin
        View rowAccount = view.findViewById(R.id.row_account);
        boolean canSeeAccount = PermissionHelper.hasFullAccess(
                dbHelper, vaiTro, PermissionHelper.MODULE_CAI_DAT);
        rowAccount.setVisibility(canSeeAccount ? View.VISIBLE : View.GONE);
        rowAccount.setOnClickListener(v -> navigate(new AccountListFragment()));

        // Đăng xuất
        view.findViewById(R.id.row_logout).setOnClickListener(v -> {
            dismiss();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });
    }

    private void navigate(androidx.fragment.app.Fragment fragment) {
        dismiss();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
