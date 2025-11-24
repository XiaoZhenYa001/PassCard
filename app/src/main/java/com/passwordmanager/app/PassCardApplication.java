package com.passwordmanager.app;

import android.app.Application;
import com.passwordmanager.utils.ThemeManager;

public class PassCardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在应用启动时初始化主题
        ThemeManager.applySavedTheme(null);
    }
}