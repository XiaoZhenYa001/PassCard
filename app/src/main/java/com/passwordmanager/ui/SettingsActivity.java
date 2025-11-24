package com.passwordmanager.ui;

import android.Manifest;
import android.app.Activity;  // 添加这行
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import android.widget.CompoundButton;
import android.content.Context;
import android.content.SharedPreferences;
import com.passwordmanager.R;
import com.passwordmanager.model.Password;
import com.passwordmanager.model.PasswordEntry;
import com.passwordmanager.utils.CSVHelper;
import com.passwordmanager.utils.ThemeManager;
import com.passwordmanager.utils.TransparencyManager;
import com.passwordmanager.utils.VersionManager;
import com.passwordmanager.utils.WallpaperManager;
import com.passwordmanager.viewmodel.PasswordViewModel;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 100;
    private static final int ACTION_BACKUP = 1;
    private static final int ACTION_RESTORE = 2;
    private static final int ACTION_EXPORT = 3;
    private static final int ACTION_IMPORT = 4;
    private static final int ACTION_CHANGE_MAIN_WALLPAPER = 5;
    private static final int ACTION_CHANGE_SIDEBAR_WALLPAPER = 6;
    
    private int currentAction = 0;


    private TextView buttonBackup;
    private TextView buttonRestore;
    private TextView buttonExport;
    private TextView buttonImport;
    private TextView textVersion;
    private TextView buttonRandomTheme;
    private TextView buttonSaveTheme;
    private TextView buttonWallpaper;
    private LinearLayout wallpaperOptionsContainer;
    private SeekBar searchBarAlphaSeekBar;
    private SeekBar passwordCardAlphaSeekBar;
    private SeekBar deleteButtonAlphaSeekBar;

    private ActivityResultLauncher<Intent> csvFileLauncher;
    private ActivityResultLauncher<Intent> wallpaperLauncher;

    private boolean getMd3Mode() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("isMd3Mode", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在调用 super.onCreate() 之前设置主题
        if (getMd3Mode()) {
            setTheme(R.style.Theme_PassCard_MD3);
        } else {
            setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 初始化CSV文件选择器
        csvFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleImportCSV(uri);
                    }
                }
            }
        );

        // 初始化壁纸选择器
        wallpaperLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // 保存选择的壁纸
                        WallpaperManager.saveWallpaper(this, uri, currentAction == ACTION_CHANGE_MAIN_WALLPAPER);
                    }
                }
            }
        );

        // 初始化视图
        initViews();
        // 设置点击事件
        setupClickListeners();
    }

    private void initViews() {

        buttonBackup = findViewById(R.id.button_backup);
        buttonRestore = findViewById(R.id.button_restore);
        buttonExport = findViewById(R.id.button_export);
        buttonImport = findViewById(R.id.button_import);
        textVersion = findViewById(R.id.text_version);
        buttonRandomTheme = findViewById(R.id.button_random_theme);
        TextView buttonResetTheme = findViewById(R.id.button_reset_theme);
        buttonWallpaper = findViewById(R.id.button_wallpaper);
        wallpaperOptionsContainer = findViewById(R.id.wallpaper_options_container);

        // 根据MD3模式设置壁纸相关控件的可见性
        if (getMd3Mode()) {
            buttonWallpaper.setVisibility(View.GONE);
            wallpaperOptionsContainer.setVisibility(View.GONE);
        } else {
            buttonWallpaper.setVisibility(View.VISIBLE);
            wallpaperOptionsContainer.setVisibility(View.VISIBLE);
        }
        
        // 初始化透明度滑动条
        searchBarAlphaSeekBar = findViewById(R.id.search_bar_alpha_seekbar);
        passwordCardAlphaSeekBar = findViewById(R.id.password_card_alpha_seekbar);
        deleteButtonAlphaSeekBar = findViewById(R.id.delete_button_alpha_seekbar);
        
        // 设置初始透明度值
        searchBarAlphaSeekBar.setProgress((int)(TransparencyManager.getSearchBarAlpha(this) * 100));
        passwordCardAlphaSeekBar.setProgress((int)(TransparencyManager.getPasswordCardAlpha(this) * 100));
        deleteButtonAlphaSeekBar.setProgress((int)(TransparencyManager.getDeleteButtonAlpha(this) * 100));
        
        // 设置透明度滑动条监听器
        searchBarAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100f;
                TransparencyManager.saveSearchBarAlpha(SettingsActivity.this, alpha);
                // 修改广播发送方式，添加包名限制
                Intent intent = new Intent("com.passwordmanager.action.TRANSPARENCY_CHANGED")
                    .setPackage(getPackageName());
                intent.putExtra("type", "search_bar");
                sendBroadcast(intent);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        passwordCardAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100f;
                TransparencyManager.savePasswordCardAlpha(SettingsActivity.this, alpha);
                // 发送广播通知MainActivity更新透明度
                Intent intent = new Intent("com.passwordmanager.action.TRANSPARENCY_CHANGED")
                    .setPackage(getPackageName());  // 添加包名限制，确保只在应用内广播
                intent.putExtra("type", "password_card");
                sendBroadcast(intent);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 设置透明度滑动条监听器
        deleteButtonAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100f;
                TransparencyManager.saveDeleteButtonAlpha(SettingsActivity.this, alpha);
                // 发送广播通知MainActivity更新透明度
                Intent intent = new Intent("com.passwordmanager.action.TRANSPARENCY_CHANGED")
                    .setPackage(getPackageName());
                intent.putExtra("type", "delete_button");
                sendBroadcast(intent);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 设置版本信息
        String version = VersionManager.getAppVersion(this);
        textVersion.setText(String.format("版本：%s", version));

        // 从SharedPreferences加载MD3模式状态
        TextView md3TextView = findViewById(R.id.md3_text_view);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        md3TextView.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            boolean currentMd3Mode = prefs.getBoolean("isMd3Mode", false);
            editor.putBoolean("isMd3Mode", !currentMd3Mode);
            editor.apply();



            Toast.makeText(this, "MD3 Mode " + (getMd3Mode() ? "Enabled" : "Disabled") + ", Restart app to apply changes", Toast.LENGTH_SHORT).show();
        });

        // 设置恢复默认主题按钮点击事件
        buttonResetTheme.setOnClickListener(v -> {
            ThemeManager.resetToDefaultTheme(this);
        });
    }
    /**
     * 检查存储权限
     * @return 是否已有权限
     */
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                PERMISSION_REQUEST_STORAGE);
            return false;
        }
        return true;
    }

    private void setupClickListeners() {
        // 随机主题按钮
        buttonRandomTheme.setOnClickListener(v -> {
            ThemeManager.applyRandomTheme(this);
        });

        // 应用已保存的主题
        ThemeManager.applySavedTheme(this);

        // 更换壁纸按钮点击事件
        buttonWallpaper.setOnClickListener(v -> {
            // 使用属性动画实现平滑的展开/收起效果
            if (wallpaperOptionsContainer.getVisibility() == View.VISIBLE) {
                // 收起动画
                wallpaperOptionsContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        wallpaperOptionsContainer.setVisibility(View.GONE);
                    })
                    .start();
            } else {
                // 展开动画
                wallpaperOptionsContainer.setAlpha(0f);
                wallpaperOptionsContainer.setVisibility(View.VISIBLE);
                wallpaperOptionsContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            }
        });

        // 更换主界面按钮点击事件
        findViewById(R.id.button_change_main_wallpaper).setOnClickListener(v -> {
            currentAction = ACTION_CHANGE_MAIN_WALLPAPER;
            // 启动图片选择器
            WallpaperManager.startImagePicker(this, wallpaperLauncher);
        });

        // 更换侧边栏按钮点击事件
        findViewById(R.id.button_change_sidebar_wallpaper).setOnClickListener(v -> {
            // TODO: 实现更换侧边栏壁纸的功能
            Toast.makeText(this, "更换侧边栏壁纸功能开发中", Toast.LENGTH_SHORT).show();
        });


        // 备份按钮
        buttonBackup.setOnClickListener(v -> {
            currentAction = ACTION_BACKUP;
            if (checkStoragePermission()) {
                // 如果已有权限，直接执行备份操作
                // TODO: 实现数据备份功能
                Toast.makeText(this, "备份功能开发中", Toast.LENGTH_SHORT).show();
            }
            // 如果没有权限，checkStoragePermission会请求权限，然后在onRequestPermissionsResult中处理
        });

        // 恢复按钮
        buttonRestore.setOnClickListener(v -> {
            currentAction = ACTION_RESTORE;
            if (checkStoragePermission()) {
                // 如果已有权限，直接执行恢复操作
                // TODO: 实现数据恢复功能
                Toast.makeText(this, "恢复功能开发中", Toast.LENGTH_SHORT).show();
            }
            // 如果没有权限，checkStoragePermission会请求权限，然后在onRequestPermissionsResult中处理
        });

        // 导出按钮
        buttonExport.setOnClickListener(v -> {
            currentAction = ACTION_EXPORT;
            if (checkStoragePermission()) {
                // 如果已有权限，直接显示导出确认对话框
                new AlertDialog.Builder(this)
                    .setTitle("确认导出")
                    .setMessage("确定要导出所有密码数据吗？")
                    .setPositiveButton("确定", (dialog, which) -> exportPasswordsToCSV())
                    .setNegativeButton("取消", null)
                    .show();
            }
            // 如果没有权限，checkStoragePermission会请求权限，然后在onRequestPermissionsResult中处理
        });

        // 导入按钮
        buttonImport.setOnClickListener(v -> {
            currentAction = ACTION_IMPORT;
            if (checkStoragePermission()) {
                // 如果已有权限，直接显示导入确认对话框
                new AlertDialog.Builder(this)
                    .setTitle("确认导入")
                    .setMessage("导入新的密码数据可能会覆盖现有数据，确定要继续吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 创建文件选择Intent
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        try {
                            csvFileLauncher.launch(Intent.createChooser(intent, "选择CSV文件"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
            // 如果没有权限，checkStoragePermission会请求权限，然后在onRequestPermissionsResult中处理
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // 如果请求被取消，结果数组为空
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
                
                // 根据之前的操作执行相应的功能
                switch (currentAction) {
                    case ACTION_BACKUP:
                        // TODO: 实现数据备份功能
                        Toast.makeText(this, "备份功能开发中", Toast.LENGTH_SHORT).show();
                        break;
                    case ACTION_RESTORE:
                        // TODO: 实现数据恢复功能
                        Toast.makeText(this, "恢复功能开发中", Toast.LENGTH_SHORT).show();
                        break;
                    case ACTION_EXPORT:
                        new AlertDialog.Builder(this)
                            .setTitle("确认导出")
                            .setMessage("确定要导出所有密码数据吗？")
                            .setPositiveButton("确定", (dialog, which) -> exportPasswordsToCSV())
                            .setNegativeButton("取消", null)
                            .show();
                        break;
                    case ACTION_IMPORT:
                        new AlertDialog.Builder(this)
                            .setTitle("确认导入")
                            .setMessage("导入新的密码数据可能会覆盖现有数据，确定要继续吗？")
                            .setPositiveButton("确定", (dialog, which) -> {
                                // 创建文件选择Intent
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");
                                intent.addCategory(Intent.CATEGORY_OPENABLE);

                                try {
                                    csvFileLauncher.launch(Intent.createChooser(intent, "选择CSV文件"));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                        break;
                }
                // 重置当前操作
                currentAction = 0;
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要存储权限才能进行导入/导出操作", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void exportPasswordsToCSV() {
        // 显示进度对话框
        AlertDialog.Builder progressBuilder = new AlertDialog.Builder(this);
        View progressView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        ProgressBar progressBar = progressView.findViewById(R.id.progress_bar);
        TextView progressText = progressView.findViewById(R.id.progress_text);
        progressBuilder.setView(progressView);
        progressBuilder.setCancelable(false);
        AlertDialog progressDialog = progressBuilder.create();
        progressDialog.setTitle("导出进度");
        progressDialog.show();

        // 获取ViewModel实例
        PasswordViewModel passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        // 获取所有密码数据
        passwordViewModel.getAllPasswords().observe(this, passwords -> {
            if (passwords != null && !passwords.isEmpty()) {
                // 导出密码数据到CSV文件
                CSVHelper.exportToCSV(this, passwords, new CSVHelper.ExportCallback() {
                    @Override
                    public void onProgress(int progress, int total) {
                        runOnUiThread(() -> {
                            progressBar.setMax(total);
                            progressBar.setProgress(progress);
                            progressText.setText(String.format("导出进度：%d/%d", progress, total));
                        });
                    }

                    @Override
                    public void onSuccess(String filePath) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, 
                                String.format("密码已导出到：%s", filePath), 
                                Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "没有可导出的密码数据", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleImportCSV(Uri uri) {
        // 显示进度对话框
        AlertDialog.Builder progressBuilder = new AlertDialog.Builder(this);
        View progressView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        ProgressBar progressBar = progressView.findViewById(R.id.progress_bar);
        TextView progressText = progressView.findViewById(R.id.progress_text);
        progressBuilder.setView(progressView);
        progressBuilder.setCancelable(false);
        AlertDialog progressDialog = progressBuilder.create();
        progressDialog.setTitle("导入进度");
        progressDialog.show();

        // 获取ViewModel实例
        PasswordViewModel passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        // 开始导入CSV文件
        CSVHelper.importFromCSV(this, uri, new CSVHelper.ImportCallback() {
            @Override
            public void onProgress(int progress, int total) {
                runOnUiThread(() -> {
                    progressBar.setMax(total);
                    progressBar.setProgress(progress);
                    progressText.setText(String.format("导入进度：%d/%d", progress, total));
                });
            }

            @Override
            public void onSuccess(List<Password> entries) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    // 批量插入密码条目
                    for (Password entry : entries) {
                        passwordViewModel.insert(entry);
                    }
                    Toast.makeText(SettingsActivity.this, 
                        String.format("成功导入%d条密码记录", entries.size()), 
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}