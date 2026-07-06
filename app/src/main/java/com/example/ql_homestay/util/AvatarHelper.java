package com.example.ql_homestay.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ql_homestay.R;

public class AvatarHelper {
    private static final String TAG = "AvatarHelper";

    /**
     * Load avatar từ String vào ImageView. Nếu avatarStr null, hiển thị initials trên TextView.
     * @param context Context
     * @param avatarStr String ảnh từ database (drawable resource name, Base64, hoặc null)
     * @param hoTen Họ tên để tạo initials
     * @param imageView ImageView để hiển thị ảnh
     * @param textView TextView để hiển thị initials (nếu không có ảnh)
     */
    public static void loadAvatar(Context context, String avatarStr, String hoTen, 
                                   ImageView imageView, TextView textView) {
        if (avatarStr != null && !avatarStr.trim().isEmpty()) {
            // Thử load từ drawable resource trước
            int resId = context.getResources().getIdentifier(avatarStr.trim(), "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
                imageView.setImageTintList(null);
                imageView.setPadding(0, 0, 0, 0);
                imageView.setBackground(null);
                imageView.setVisibility(android.view.View.VISIBLE);
                if (textView != null) textView.setVisibility(android.view.View.GONE);
                return;
            }
            
            // Nếu không phải resource, thử decode Base64
            byte[] avatarBytes = stringToBytes(avatarStr);
            if (avatarBytes != null && avatarBytes.length > 0) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setImageTintList(null);
                        imageView.setPadding(0, 0, 0, 0);
                        imageView.setBackground(null);
                        imageView.setVisibility(android.view.View.VISIBLE);
                        if (textView != null) textView.setVisibility(android.view.View.GONE);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to decode avatar bitmap", e);
                }
            }
        }
        // Không có ảnh hoặc decode lỗi: hiển thị initials
        String initials = getInitials(hoTen);
        if (textView != null) {
            textView.setText(initials);
            textView.setVisibility(android.view.View.VISIBLE);
        }
        imageView.setVisibility(android.view.View.GONE);
    }

    /**
     * Load avatar chỉ vào ImageView (dùng cho app bar, không có TextView fallback)
     */
    public static void loadAvatarOnly(Context context, String avatarStr, ImageView imageView) {
        if (avatarStr != null && !avatarStr.trim().isEmpty()) {
            // Thử load từ drawable resource trước
            int resId = context.getResources().getIdentifier(avatarStr.trim(), "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
                imageView.setImageTintList(null);
                imageView.setPadding(0, 0, 0, 0);
                imageView.setBackground(null);
                return;
            }
            
            // Nếu không phải resource, thử decode Base64
            byte[] avatarBytes = stringToBytes(avatarStr);
            if (avatarBytes != null && avatarBytes.length > 0) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setImageTintList(null);
                        imageView.setPadding(0, 0, 0, 0);
                        imageView.setBackground(null);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to decode avatar bitmap", e);
                }
            }
        }
        // Fallback về icon person
        imageView.setImageResource(R.drawable.ic_person);
    }

    public static void loadAvatarPreview(Context context, String avatarStr,
                                         ImageView imageView, android.view.View placeholderView) {
        if (avatarStr != null && !avatarStr.trim().isEmpty()) {
            int resId = context.getResources().getIdentifier(avatarStr.trim(), "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
                imageView.setVisibility(android.view.View.VISIBLE);
                if (placeholderView != null) placeholderView.setVisibility(android.view.View.GONE);
                return;
            }

            byte[] avatarBytes = stringToBytes(avatarStr);
            if (avatarBytes != null && avatarBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(android.view.View.VISIBLE);
                    if (placeholderView != null) placeholderView.setVisibility(android.view.View.GONE);
                    return;
                }
            }
        }
        imageView.setVisibility(android.view.View.GONE);
        if (placeholderView != null) placeholderView.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * Convert String avatar (Base64 hoặc file path) thành byte[]
     */
    private static byte[] stringToBytes(String avatarStr) {
        if (avatarStr == null || avatarStr.trim().isEmpty()) return null;
        try {
            // Thử decode Base64
            return Base64.decode(avatarStr.trim(), Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Avatar string is not valid Base64: " + avatarStr.substring(0, Math.min(20, avatarStr.length())));
            return null;
        }
    }

    /**
     * Lấy initials từ họ tên (2 chữ cái đầu)
     */
    public static String getInitials(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return "?";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length == 1) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        }
        return (String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
