package com.passwordmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;

import java.util.Random;

public class ThemeManager {
    private static final Random random = new Random();
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME_COLORS = "theme_colors";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final int[] DEFAULT_THEME_COLORS = {
        Color.parseColor("#2196F3"),  // 主色调（按钮背景色）
        Color.parseColor("#333333"),  // 文字颜色
        Color.parseColor("#FFFFFF")   // 卡片背景色
    };
    
    private static final int[] DARK_THEME_COLORS = {
        Color.parseColor("#1976D2"),  // 深蓝色（按钮背景色）
        Color.parseColor("#FFFFFF"),  // 白色文字
        Color.parseColor("#121212")   // 深灰色卡片背景
    };

    private static int[] currentThemeColors;

    public static void applyRandomTheme(Activity activity) {
        if (activity == null) return;

        // 生成新的随机颜色
        currentThemeColors = new int[]{
            generateRandomColor(),
            generateRandomColor(),
            generateRandomColor()
        };

        applyThemeToActivity(activity);
        
        // 保存主题设置
        saveCurrentTheme(activity);
        
        // 重建Activity以确保主题完全应用
        activity.recreate();
    }

    public static void saveCurrentTheme(Context context) {
        if (context == null || currentThemeColors == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // 将颜色数组转换为字符串存储
        StringBuilder colors = new StringBuilder();
        for (int color : currentThemeColors) {
            colors.append(color).append(",");
        }
        editor.putString(KEY_THEME_COLORS, colors.toString());
        editor.apply();
    }

    public static void applySavedTheme(Activity activity) {
        if (activity == null) return;

        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String colorsStr = prefs.getString(KEY_THEME_COLORS, null);

        if (colorsStr != null) {
            String[] colorStrArray = colorsStr.split(",");
            currentThemeColors = new int[colorStrArray.length];
            for (int i = 0; i < colorStrArray.length; i++) {
                try {
                    currentThemeColors[i] = Integer.parseInt(colorStrArray[i]);
                } catch (NumberFormatException e) {
                    currentThemeColors = DEFAULT_THEME_COLORS.clone();
                    break;
                }
            }
        } else {
            currentThemeColors = DEFAULT_THEME_COLORS.clone();
        }

        applyThemeToActivity(activity);
    }
    

    private static void applyThemeToActivity(Activity activity) {
        if (activity == null || currentThemeColors == null) return;
        
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        applyColorsToViewGroup(rootView);
    }

    private static void applyColorsToViewGroup(ViewGroup viewGroup) {
        if (viewGroup == null) return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof ViewGroup) {
                applyColorsToViewGroup((ViewGroup) child);
            }

            applyRandomColorToView(child);
        }
    }

    private static void applyRandomColorToView(View view) {
        if (view == null) return;

        if (view instanceof Button) {
            view.setBackgroundColor(currentThemeColors[0]);
            ((Button) view).setTextColor(currentThemeColors[1]);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(currentThemeColors[1]);
        } else if (view instanceof MaterialCardView) {
            ((MaterialCardView) view).setCardBackgroundColor(currentThemeColors[2]);
        } else if (view instanceof androidx.appcompat.widget.Toolbar) {
            view.setBackgroundColor(currentThemeColors[0]);
        } else if (view.getClass().getName().contains("PopupMenu")) {
            view.setBackgroundColor(currentThemeColors[2]);
        } else if (view.getClass().getName().contains("PopupWindow")) {
            view.setBackgroundColor(currentThemeColors[2]);
        } else if (view.getClass().getName().contains("PopupWindow")) {
            view.setBackgroundColor(currentThemeColors[2]);
        } else if (view instanceof android.widget.EditText) {
            ((android.widget.EditText) view).setTextColor(currentThemeColors[1]);
            ((android.widget.EditText) view).setHintTextColor(Color.argb(128, Color.red(currentThemeColors[1]), 
                Color.green(currentThemeColors[1]), Color.blue(currentThemeColors[1])));
        } else if (view instanceof android.widget.Spinner) {
            ((android.widget.Spinner) view).setBackgroundColor(currentThemeColors[0]);
        } else if (view instanceof android.widget.CheckBox) {
            ((android.widget.CheckBox) view).setTextColor(currentThemeColors[1]);
        } else if (view instanceof android.widget.RadioButton) {
            ((android.widget.RadioButton) view).setTextColor(currentThemeColors[1]);
        } else if (view instanceof android.widget.Switch || view instanceof androidx.appcompat.widget.SwitchCompat) {
            if (view instanceof android.widget.Switch) {
                ((android.widget.Switch) view).setTextColor(currentThemeColors[1]);
            } else {
                ((androidx.appcompat.widget.SwitchCompat) view).setTextColor(currentThemeColors[1]);
            }
        } else if (view instanceof android.widget.ProgressBar) {
            if (Build.VERSION.SDK_INT >= 29) { // API 29 is Android Q
                ((android.widget.ProgressBar) view).setProgressTintList(ColorStateList.valueOf(currentThemeColors[0]));
            }
        }
    }

    private static int generateRandomColor() {
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private static int generateContrastColor() {
        return Color.argb(255, random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128);
    }

    public static void resetToDefaultTheme(Activity activity) {
        if (activity == null) return;

        // 设置当前主题为默认主题
        currentThemeColors = DEFAULT_THEME_COLORS.clone();

        // 应用默认主题到界面
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        applyColorsToViewGroup(rootView);

        // 清除保存的主题设置
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_THEME_COLORS).apply();
        prefs.edit().putBoolean(KEY_THEME_MODE, false).apply();
    }
    
    public static void applyDarkTheme(Activity activity) {
        if (activity == null) return;
        
        // 设置当前主题为黑色主题
        currentThemeColors = DARK_THEME_COLORS.clone();
        
        // 应用黑色主题到界面
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        applyColorsToViewGroup(rootView);
        
        // 保存主题模式设置
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_THEME_MODE, true).apply();
        
        // 保存主题颜色设置
        saveCurrentTheme(activity);
    }
    
    public static void applyLightTheme(Activity activity) {
        if (activity == null) return;
        
        // 设置当前主题为白色主题（默认主题）
        currentThemeColors = DEFAULT_THEME_COLORS.clone();
        
        // 应用白色主题到界面
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        applyColorsToViewGroup(rootView);
        
        // 保存主题模式设置
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_THEME_MODE, false).apply();
        
        // 保存主题颜色设置
        saveCurrentTheme(activity);
    }
    
    public static boolean isDarkThemeEnabled(Context context) {
        if (context == null) return false;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_THEME_MODE, false);
    }
    
    public static void toggleThemeMode(Activity activity) {
        if (activity == null) return;
        
        boolean isDarkMode = isDarkThemeEnabled(activity);
        if (isDarkMode) {
            applyLightTheme(activity);
        } else {
            applyDarkTheme(activity);
        }
        
        // 重建Activity以确保主题完全应用
        activity.recreate();
    }
}