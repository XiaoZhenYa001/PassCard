package com.passwordmanager.app;

import android.app.Application;
import android.widget.Toast;
import com.passwordmanager.data.AppDatabase;
import com.passwordmanager.utils.SignatureVerifier;

public class PasswordManagerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 验证应用签名
       if (SignatureVerifier.verifyAppSignature(this)) {
            // 签名验证失败，提示用户
            Toast.makeText(this, "警告：应用签名异常，可能被篡改！", Toast.LENGTH_LONG).show();
            // 这里可以根据需要采取进一步措施，如退出应用
          //  System.exit(0);
       }
        
        // 初始化数据库
        AppDatabase.getInstance(this);
    }
}