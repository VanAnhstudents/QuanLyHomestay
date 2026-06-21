package com.example.ql_homestay.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImagePickerHelper {

    public static final int MAX_IMAGE_SIZE = 500; // KB
    
    /**
     * Mở Intent chọn ảnh từ thư viện
     */
    public static void pickImage(Fragment fragment, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    /**
     * Xử lý kết quả chọn ảnh và hiển thị preview
     * @return byte[] của ảnh đã nén, hoặc null nếu lỗi
     */
    public static byte[] handleImageResult(Context context, Uri imageUri, 
                                           ImageView imageView, View placeholderView) {
        if (imageUri == null) return null;
        
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            
            if (bitmap == null) return null;
            
            // Nén ảnh để tiết kiệm dung lượng database
            Bitmap resized = resizeBitmap(bitmap, 400);
            byte[] imageBytes = compressBitmap(resized);
            
            // Hiển thị preview
            imageView.setImageBitmap(resized);
            imageView.setVisibility(android.view.View.VISIBLE);
            if (placeholderView != null) {
                placeholderView.setVisibility(android.view.View.GONE);
            }
            
            return imageBytes;
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resize bitmap về kích thước tối đa (giữ tỷ lệ)
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Nén bitmap thành byte[] với chất lượng 80%
     */
    private static byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }
}
