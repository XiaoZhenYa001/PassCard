package com.passwordmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.passwordmanager.R;

public class AboutActivity extends AppCompatActivity {

    private TextView tvSupportAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 初始化工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // 初始化支持作者按钮并设置点击事件
        tvSupportAuthor = findViewById(R.id.tv_support_author);
        tvSupportAuthor.setOnClickListener(v -> showSupportDialog());
    }
    
    /**
     * 显示支持作者的对话框
     */
    private void showSupportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("支持一下");
        builder.setMessage("如果阁下还未满18周岁, 阁下使用该软件就是对软件的支持 !");
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("微信", (dialog, which) -> {
            // 跳转到微信支付页面
            Intent intent = new Intent(this, WechatPayActivity.class);
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 处理返回按钮点击事件
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}