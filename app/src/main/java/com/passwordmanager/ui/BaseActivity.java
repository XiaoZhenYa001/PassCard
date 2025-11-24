package com.passwordmanager.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.passwordmanager.utils.ThemeManager;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置状态栏为透明
        setTransparentStatusBar();
        // 应用已保存的主题
        ThemeManager.applySavedTheme(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 确保在Activity恢复时应用当前主题
        ThemeManager.applySavedTheme(this);
        // 确保状态栏透明
        setTransparentStatusBar();
    }
    
    /**
     * 设置状态栏为透明色
     */
    private void setTransparentStatusBar() {
        Window window = getWindow();
        if (window != null) {
            // 使用WindowCompat设置内容延伸到状态栏
            WindowCompat.setDecorFitsSystemWindows(window, false);
            
            // 设置状态栏颜色为透明
            window.setStatusBarColor(Color.TRANSPARENT);
            
            // 设置状态栏图标为深色模式（黑色图标）
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(true);
        }
    }
}