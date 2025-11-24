package com.passwordmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WallpaperManager {
    private static final String PREFS_NAME = "WallpaperPrefs";
    private static final String KEY_MAIN_WALLPAPER = "main_wallpaper";
    private static final String KEY_SIDEBAR_WALLPAPER = "sidebar_wallpaper";
    /**
     * 保存壁纸到内部存储
     * @param context 上下文
     * @param uri 壁纸图片的Uri
     * @param isMainWallpaper 是否是主界面壁纸
     * @return 保存后的文件路径
     */
    public static String saveWallpaper(Context context, Uri uri, boolean isMainWallpaper) {
        String fileName = isMainWallpaper ? "main_wallpaper.jpg" : "sidebar_wallpaper.jpg";
        File wallpaperFile = new File(context.getFilesDir(), fileName);

        try {
            // 从Uri读取图片
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            // 保存图片到文件
            FileOutputStream outputStream = new FileOutputStream(wallpaperFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();

            // 保存文件路径到SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(isMainWallpaper ? KEY_MAIN_WALLPAPER : KEY_SIDEBAR_WALLPAPER, wallpaperFile.getAbsolutePath());
            editor.apply();

            return wallpaperFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存壁纸失败", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 加载壁纸
     * @param context 上下文
     * @param imageView 要设置壁纸的ImageView
     * @param isMainWallpaper 是否是主界面壁纸
     */
    public static void loadWallpaper(Context context, ImageView imageView, boolean isMainWallpaper) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String wallpaperPath = prefs.getString(isMainWallpaper ? KEY_MAIN_WALLPAPER : KEY_SIDEBAR_WALLPAPER, null);

        if (wallpaperPath != null) {
            File wallpaperFile = new File(wallpaperPath);
            if (wallpaperFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(wallpaperPath);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 启动图片选择器
     * @param activity Activity实例
     * @param launcher ActivityResultLauncher实例
     */
    public static void startImagePicker(Activity activity, androidx.activity.result.ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    /**
     * 设置壁纸
     * @param context 上下文
     * @param uri 壁纸图片的Uri
     * @param isMainWallpaper 是否是主界面壁纸
     */
    public static void setWallpaper(Context context, Uri uri, boolean isMainWallpaper) {
        saveWallpaper(context, uri, isMainWallpaper);
    }
}