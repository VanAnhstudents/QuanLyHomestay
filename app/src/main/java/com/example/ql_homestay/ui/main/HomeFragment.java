package com.example.ql_homestay.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ql_homestay.R;

/**
 * HomeFragment — màn hình Trang chủ (Dashboard).
 * Placeholder tối thiểu để MainActivity không crash.
 * Sẽ được implement đầy đủ theo Lộ trình 0 - B3.
 */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}