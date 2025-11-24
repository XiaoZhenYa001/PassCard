package com.passwordmanager.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.passwordmanager.model.PasswordEntry;
import com.passwordmanager.utils.WallpaperManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.passwordmanager.R;
import com.passwordmanager.model.Password;
import com.passwordmanager.utils.CSVHelper;
import com.passwordmanager.utils.TransparencyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import com.passwordmanager.viewmodel.PasswordViewModel;

/**
 * 主活动类
 * 显示密码列表，提供搜索、添加等功能
 */
public class MainActivity extends AppCompatActivity {

    private PasswordViewModel passwordViewModel;
    private PasswordAdapter passwordAdapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private EditText searchEditText;
    private ImageButton searchTypeButton;
    private ImageButton menuButton;
    private String currentSearchType = "all";
    private TextView searchTypeText;
    private ImageButton fabAdd;
    private TextView toolbarTitleText;
    private MaterialButton deleteButton;
    private List<Password> selectedPasswords = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private float touchStartX;
    private float touchStartY;
    private static final float SWIPE_THRESHOLD = 50; // 降低滑动阈值，使滑动更容易触发
    private GestureDetector gestureDetector;
    private float EDGE_THRESHOLD; // 触发区域为屏幕宽度的20%

    private boolean getMd3Mode() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("isMd3Mode", false);
    }

    private void backupDatabase() {
        File currentDB = getDatabasePath("password_manager_db");
        File backupDB = new File(getExternalFilesDir(null), "password_manager_db_backup");
        
        if (currentDB.exists()) {
            try {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.passwordmanager.action.WALLPAPER_CHANGED".equals(action)) {
                // 重新加载壁纸
                ImageView backgroundImage = findViewById(R.id.background_image);
                WallpaperManager.loadWallpaper(MainActivity.this, backgroundImage, true);
            } else if ("com.passwordmanager.action.TRANSPARENCY_CHANGED".equals(action)) {
                String type = intent.getStringExtra("type");
                if ("search_bar".equals(type)) {
                    // 更新搜索栏透明度
                    View searchBar = findViewById(R.id.search_card);
                    TransparencyManager.applySearchBarAlpha(MainActivity.this, searchBar);
                } else if ("password_card".equals(type)) {
                    // 更新密码卡片透明度
                    passwordAdapter.notifyDataSetChanged();
                } else if ("delete_button".equals(type)) {
                    // 更新删除按钮透明度
                    MaterialButton deleteButton = findViewById(R.id.delete_button);
                    TransparencyManager.applyDeleteButtonAlpha(MainActivity.this, deleteButton);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在调用 super.onCreate() 之前设置主题
        if (getMd3Mode()) {
            setTheme(R.style.Theme_PassCard_MD3);
        } else {
            setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 根据MD3模式设置背景
        if (getMd3Mode()) {
            // MD3模式下不加载背景图片，并设置背景为白色
            findViewById(R.id.background_image).setVisibility(View.GONE);
            findViewById(android.R.id.content).setBackgroundColor(Color.WHITE);
        } else {
            // 非MD3模式下加载背景图片
            ImageView backgroundImage = findViewById(R.id.background_image);
            WallpaperManager.loadWallpaper(this, backgroundImage, true);
        }

        // 初始化工具栏标题文本
        toolbarTitleText = findViewById(R.id.toolbar_title);
        
        // 初始化应用标题点击事件
        ImageView appTitle = findViewById(R.id.app_title);
        appTitle.setOnClickListener(v -> {
            if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 初始化壁纸选择器
        wallpaperLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // 设置主界面壁纸
                        WallpaperManager.setWallpaper(this, uri, true);
                        // 重新加载壁纸
                        ImageView backgroundImage = findViewById(R.id.background_image);
                        WallpaperManager.loadWallpaper(this, backgroundImage, true);
                    }
                }
            }
        );

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.passwordmanager.action.WALLPAPER_CHANGED");
        filter.addAction("com.passwordmanager.action.TRANSPARENCY_CHANGED");
        registerReceiver(mainReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // 应用初始透明度设置
        View searchBar = findViewById(R.id.search_card);
        TransparencyManager.applySearchBarAlpha(this, searchBar);

        // 在数据库初始化之前进行备份
        backupDatabase();



        // 初始化UI组件
        searchTypeText = findViewById(R.id.search_type_text);
        if (getMd3Mode()) {
            searchTypeText.setTextColor(Color.BLACK);
        }
        fabAdd = findViewById(R.id.fab_add);
        searchTypeButton = findViewById(R.id.search_type_button);
        menuButton = findViewById(R.id.toolbar_menu_button);

        // 设置菜单按钮点击事件
        menuButton.setOnClickListener(v -> showPopupMenu(v));

        // 初始化视图
        recyclerView = findViewById(R.id.password_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        searchEditText = findViewById(R.id.search_edit_text);
        deleteButton = findViewById(R.id.delete_button);

        // 设置RecyclerView的缓存策略
        recyclerView.setItemViewCacheSize(20);

        
        // 初始化ViewModel
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        
        // 使用预加载观察者模式加载数据
        passwordViewModel.getAllPasswords().observe(this, passwords -> {
            if (passwords != null && !passwords.isEmpty()) {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                passwordAdapter.setPasswords(passwords);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });

        // 设置删除按钮点击事件
        deleteButton.setOnClickListener(v -> {
            List<Password> selectedItems = passwordAdapter.getSelectedPasswords();
            if (!selectedItems.isEmpty()) {
                new AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("确定要删除选中的" + selectedItems.size() + "条密码记录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 删除选中的密码
                        for (Password password : selectedItems) {
                            passwordViewModel.delete(password);
                        }
                        // 退出多选模式
                        passwordAdapter.toggleMultiSelectMode(false);
                        // 隐藏删除按钮
                        deleteButton.setVisibility(View.GONE);
                        // 显示提示信息
                        Toast.makeText(this, "已删除" + selectedItems.size() + "条密码记录", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
        });
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordAdapter = new PasswordAdapter();
        recyclerView.setAdapter(passwordAdapter);

        // 添加RecyclerView触摸事件拦截
        recyclerView.setOnTouchListener((v, event) -> {
            float x = event.getX();
            // 如果触摸点在屏幕左侧边缘区域
            if (x <= EDGE_THRESHOLD) {
                // 将事件传递给GestureDetector处理
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                // 处理基本的滑动手势
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStartX = x;
                        touchStartY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - touchStartX;
                        float deltaY = event.getY() - touchStartY;
                        // 如果是水平滑动（水平位移大于垂直位移）
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_THRESHOLD) {
                            if (deltaX > 0 && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                drawerLayout.openDrawer(GravityCompat.START);
                                return true;
                            }
                        }
                        break;
                }
            }
            // 不在边缘区域，让RecyclerView处理正常的滚动
            return false;
        });
        
        // 初始化EDGE_THRESHOLD为屏幕宽度的20%
        EDGE_THRESHOLD = getResources().getDisplayMetrics().widthPixels * 0.5f;

        // 初始化GestureDetector
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null) return false;
                
                // 只处理起始点在边缘区域的手势
                if (e1.getX() > EDGE_THRESHOLD) return false;
                
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                
                // 计算与水平线的夹角
                double angleWithHorizontal = Math.abs(Math.toDegrees(Math.atan2(deltaY, deltaX)));
                
                // 只处理接近水平的滑动
                if (angleWithHorizontal <= 45) {
                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        // 使用动画效果打开抽屉
                        drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                
                // 只处理起始点在边缘区域的手势
                if (e1.getX() > EDGE_THRESHOLD) return false;
                
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                
                // 计算与水平线的夹角
                double angleWithHorizontal = Math.abs(Math.toDegrees(Math.atan2(deltaY, deltaX)));
                
                // 只处理接近水平的滑动
                if (angleWithHorizontal <= 45) {
                    if (Math.abs(velocityX) > 200) {
                        if (velocityX > 0 && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.openDrawer(GravityCompat.START);
                            return true;
                        } else if (velocityX < 0 && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        // 添加触摸事件监听
        View mainContent = findViewById(R.id.constraintLayout);
        mainContent.setOnTouchListener((v, event) -> {
            // 将事件传递给GestureDetector处理
            gestureDetector.onTouchEvent(event);
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    // 只有在屏幕左边缘区域才开始处理滑动
                    return touchStartX <= EDGE_THRESHOLD;
                case MotionEvent.ACTION_UP:
                    float touchEndX = event.getX();
                    float touchEndY = event.getY();
                    float deltaX = touchEndX - touchStartX;
                    float deltaY = touchEndY - touchStartY;
                    
                    // 计算滑动距离
                    float swipeDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (swipeDistance < SWIPE_THRESHOLD) {
                        return false; // 滑动距离太小，忽略
                    }
                    // 计算与水平线的夹角
                    double angleWithHorizontal = Math.abs(Math.toDegrees(Math.atan2(deltaY, deltaX)));
                    
                    // 放宽水平滑动的角度判断
                    if (angleWithHorizontal <= 60) { // 增加角度范围到60度
                        if (deltaX > 0 && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            // 向右滑动，打开侧边栏
                            drawerLayout.openDrawer(GravityCompat.START);
                            return true;
                        }
                    } else if (angleWithHorizontal >= 120) { // 放宽反向滑动的角度范围
                        if (deltaX < 0 && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            // 向左滑动，关闭侧边栏
                            drawerLayout.closeDrawer(GravityCompat.START);
                            return true;
                        }
                    }
                    return false;
            }
            return false;
        });

        // 初始化抽屉布局
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 设置抽屉布局的宽度为屏幕宽度的60%
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.6);
        navigationView.setLayoutParams(params);

        // 为设置按钮添加点击事件
        View headerView = navigationView.getHeaderView(0);
        TextView settingsButton = headerView.findViewById(R.id.nav_settings_btn);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        
        // 为备份按钮添加点击事件
        TextView backupButton = headerView.findViewById(R.id.nav_backup_btn);
        backupButton.setOnClickListener(v -> {
            Toast.makeText(this, "开发中", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        
        // 为恢复按钮添加点击事件
        TextView restoreButton = headerView.findViewById(R.id.nav_restore_btn);
        restoreButton.setOnClickListener(v -> {
            Toast.makeText(this, "开发中", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        
        // 为作者介绍按钮添加点击事件
        TextView aboutButton = headerView.findViewById(R.id.nav_about_btn);
        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // 初始化工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置工具栏标题点击事件
        toolbarTitleText.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 设置抽屉布局的阴影和背景
//        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setScrimColor(Color.parseColor("#80000000")); // 设置半透明遮罩

        // 初始化视图
        // 删除以下重复初始化的代码
        // 初始化视图
        // recyclerView = findViewById(R.id.password_recycler_view);
        // emptyView = findViewById(R.id.empty_view);
        // searchEditText = findViewById(R.id.search_edit_text);
        // searchTypeText = findViewById(R.id.search_type_text);
        // fabAdd = findViewById(R.id.fab_add);
        // searchTypeButton = findViewById(R.id.search_type_button);
        // menuButton = findViewById(R.id.menu_button);
        
        // 删除以下重复初始化的代码
        // 设置RecyclerView
        // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // passwordAdapter = new PasswordAdapter();
        // recyclerView.setAdapter(passwordAdapter);

        // 设置密码适配器的多选模式监听器
        passwordAdapter.setOnMultiSelectModeChangeListener((isMultiSelectMode, selectedCount) -> {
            toolbarTitleText = findViewById(R.id.toolbar_title);
            ImageButton toolbarMenuButton = findViewById(R.id.toolbar_menu_button);
            MaterialButton deleteButton = findViewById(R.id.delete_button);
            
            if (isMultiSelectMode) {
                // 进入多选模式
                searchTypeButton.setImageResource(R.drawable.ic_search); // 保持搜索图标不变
                appTitle.setImageResource(R.drawable.ic_close); // 将PassCard标题改为关闭图标
                appTitle.setOnClickListener(null); // 移除PassCard按钮的点击事件
                toolbarMenuButton.setImageResource(R.drawable.ic_select_all);
                fabAdd.setVisibility(View.GONE);
                searchTypeText.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.bringToFront(); // 确保删除按钮显示在最上层
                TransparencyManager.applyDeleteButtonAlpha(MainActivity.this, deleteButton);
                deleteButton.postDelayed(() -> {
                    if (deleteButton.getVisibility() != View.VISIBLE) {
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                }, 100); // 延迟100ms再次确认按钮可见性
                
                // 修改标题栏显示
                toolbarTitleText.setVisibility(View.VISIBLE);
                toolbarTitleText.setText(selectedCount > 0 ? "已选择" + selectedCount + "项" : "请选择密码");
                
                // 将关闭图标放在标题栏左侧
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                titleParams.gravity = android.view.Gravity.CENTER;
                titleParams.weight = 1;
                toolbarTitleText.setLayoutParams(titleParams);
                toolbarTitleText.setGravity(android.view.Gravity.CENTER);

                // 设置关闭多选模式的点击事件
                appTitle.setOnClickListener(v -> passwordAdapter.toggleMultiSelectMode(false));
                
                // 设置全选/取消全选的点击事件
                menuButton.setOnClickListener(v -> {
                    passwordAdapter.toggleMultiSelectMode(true);
                    // 根据选择状态更新图标
                    if (selectedCount == passwordAdapter.getPasswords().size()) {
                        toolbarMenuButton.setImageResource(R.drawable.ic_unselect_all);
                    } else {
                        toolbarMenuButton.setImageResource(R.drawable.ic_select_all);
                    }
                });
            } else {
                // 退出多选模式
                fabAdd.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                
                // 恢复标题栏显示
                appTitle.setImageResource(R.drawable.ic_passcard_txt); // 恢复PassCard图标
                toolbarTitleText.setVisibility(View.GONE);
                toolbarMenuButton.setImageResource(R.drawable.ic_menu); // 恢复菜单图标

                // 恢复原有的点击事件
                // 恢复PassCard标题的点击事件
                appTitle.setOnClickListener(v -> {
                    if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
                
                // 设置菜单按钮点击事件
                menuButton.setOnClickListener(v -> showPopupMenu(menuButton));
                
                // 初始化搜索类型文本
                searchTypeText.setText("全部");
                searchTypeText.setVisibility(View.VISIBLE);
            }
        });

        // 初始化ViewModel
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        // 观察密码数据变化
        passwordViewModel.getAllPasswords().observe(this, this::updatePasswordList);

        // 设置搜索功能，添加延迟搜索
        // 使用Handler实现延迟搜索
        final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        final long SEARCH_DELAY = 350; // 延迟500毫秒
        final Runnable searchRunnable = new Runnable() {
            @Override
            public void run() {
                String query = searchEditText.getText().toString();
                searchPasswords(query);
            }
        };
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不需要实现
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 不需要实现
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 取消之前的搜索任务
                searchHandler.removeCallbacks(searchRunnable);
                // 延迟执行新的搜索
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
        });

        // 设置添加按钮点击事件
        fabAdd.setOnClickListener(v -> addNewPassword());

        // 初始化搜索类型文本
        searchTypeText.setText("全部");
    }

    /**
     * 显示搜索类型选择菜单
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("全部");
        popupMenu.getMenu().add("服务");
        popupMenu.getMenu().add("用户名");
        popupMenu.getMenu().add("手机号");
        popupMenu.getMenu().add("邮箱");
        popupMenu.getMenu().add("密码");
        popupMenu.getMenu().add("备注");

        popupMenu.setOnMenuItemClickListener(item -> {
            searchTypeText.setText(item.getTitle());
            String query = searchEditText.getText().toString();
            switch (item.getTitle().toString()) {
                case "全部":
                    currentSearchType = "all";
                    break;
                case "用户名":
                    currentSearchType = "username";
                    break;
                case "手机号":
                    currentSearchType = "phone";
                    break;
                case "邮箱":
                    currentSearchType = "email";
                    break;
                case "网站":
                    currentSearchType = "service";
                    break;
                case "密码":
                    currentSearchType = "password";
                    break;
                case "备注":
                    currentSearchType = "note";
                    break;
            }
            // 执行搜索
            searchPasswords(query);
            return true;
        });

        popupMenu.show();
    }

    private void showSearchTypeMenu() {
        showPopupMenu(menuButton);
    }

    /**
     * 更新密码列表
     * @param passwords 密码列表
     */
    private void updatePasswordList(List<Password> passwords) {
        if (passwords != null && !passwords.isEmpty()) {
            passwordAdapter.setPasswords(passwords);
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 搜索密码
     * @param query 搜索关键词
     */
    private void searchPasswords(String query) {
        if (query.isEmpty()) {
            passwordViewModel.getAllPasswords().observe(this, this::updatePasswordList);
        } else {
            switch (currentSearchType) {
                case "username":
                    passwordViewModel.searchByUsername(query).observe(this, this::updatePasswordList);
                    break;
                case "phone":
                    passwordViewModel.searchByPhoneNumber(query).observe(this, this::updatePasswordList);
                    break;
                case "email":
                    passwordViewModel.searchByEmail(query).observe(this, this::updatePasswordList);
                    break;
                case "service":
                    passwordViewModel.searchByService(query).observe(this, this::updatePasswordList);
                    break;
                case "password":
                    passwordViewModel.searchByPassword(query).observe(this, this::updatePasswordList);
                    break;
                case "note":
                    passwordViewModel.searchByNote(query).observe(this, this::updatePasswordList);
                    break;
                default:
                    passwordViewModel.searchAll(query).observe(this, this::updatePasswordList);
                    break;
            }
        }
    }

    /**
     * 显示添加密码选项菜单
     */
    private void addNewPassword() {
        showAddPasswordDialog();
    }

    /**
     * 显示添加密码对话框
     */
    private void showAddPasswordDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_password);
        
        // 加载对话框布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_password, null);
        builder.setView(dialogView);
        
        // 获取输入框引用
        TextInputEditText serviceEdit = dialogView.findViewById(R.id.edit_service);
        TextInputEditText usernameEdit = dialogView.findViewById(R.id.edit_username);
        TextInputEditText phoneEdit = dialogView.findViewById(R.id.edit_phone);
        TextInputEditText emailEdit = dialogView.findViewById(R.id.edit_email);
        TextInputEditText passwordEdit = dialogView.findViewById(R.id.edit_password);
        TextInputEditText noteEdit = dialogView.findViewById(R.id.edit_note);

        // 创建保存操作的Runnable
        Runnable saveAction = () -> {
            // 获取用户输入
            String username = usernameEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String note = noteEdit.getText().toString().trim();
            String service = serviceEdit.getText().toString().trim();
            
            // 验证输入（至少需要一个字段有值）
            if (username.isEmpty() && phone.isEmpty() && email.isEmpty() && password.isEmpty()) {
                return;
            }
            
            // 创建新的密码条目
            Password entry = new Password(service, username, phone, email, password, note);
            
            // 保存到数据库
            passwordViewModel.insert(entry);
            
            // 显示成功消息
        };
        
        // 设置对话框按钮
        builder.setPositiveButton(R.string.save, (dialog, which) -> saveAction.run());
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        
        // 设置点击对话框外部区域的行为
        builder.setCancelable(true);
        builder.setOnCancelListener(null);
        
        // 显示对话框
        AlertDialog dialog = builder.create();
        
        dialog.show();
    }

    public PasswordViewModel getPasswordViewModel() {
        return passwordViewModel;
    }

    private ActivityResultLauncher<Intent> wallpaperLauncher;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 处理CSV文件导入
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 显示进度对话框
                AlertDialog.Builder progressBuilder = new AlertDialog.Builder(this);
                View progressView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
                ProgressBar progressBar = progressView.findViewById(R.id.progress_bar);
                TextView progressText = progressView.findViewById(R.id.progress_text);
                progressBuilder.setView(progressView);
                progressBuilder.setCancelable(false);
                AlertDialog progressDialog = progressBuilder.create();
                progressDialog.setTitle("导入进度");
                progressDialog.setCancelable(false);
                progressDialog.show();

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
                            Toast.makeText(MainActivity.this, 
                                String.format("成功导入%d条密码记录", entries.size()), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(mainReceiver);
    }
}