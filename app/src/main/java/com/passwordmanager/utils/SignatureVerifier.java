package com.passwordmanager.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 签名验证工具类
 * 用于验证应用的签名是否合法，防止应用被篡改或重新打包
 */
public class SignatureVerifier {
    private static final String TAG = "SignatureVerifier";
    
    // 正式发布版本的签名SHA-256指纹
    // 这个值是从应用签名文件中提取的SHA-256指纹
    private static final String RELEASE_SIGNATURE = "a7d70e6fa4dbbab2c6917efc965d506b113a3fff2e7300d497fbed004dc00a56";

    /**
     * 验证应用签名是否合法
     * @param context 应用上下文
     * @return 签名是否合法
     */
    public static boolean verifyAppSignature(Context context) {
        try {
            PackageInfo packageInfo = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo = context.getPackageManager()
                            .getPackageInfo(context.getPackageName(),
                                    PackageManager.GET_SIGNING_CERTIFICATES);  // 使用新的 API
                    if (packageInfo.signingInfo == null || packageInfo.signingInfo.getSigningCertificateHistory().length == 0) {
                        return false;
                    }
                } else {
                    packageInfo = context.getPackageManager()
                            .getPackageInfo(context.getPackageName(),
                                    PackageManager.GET_SIGNATURES);
                    if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                        return false;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "找不到包名", e);
                return false;
            }

            // 获取当前应用的签名指纹
            String currentSignature = getAppSignature(context);

            // 如果是调试版本，允许任何签名
            if (isDebugBuild()) {
                Log.d(TAG, "调试版本，跳过签名验证");
                return true;
            }

            // 比对签名是否匹配
            boolean isValid = RELEASE_SIGNATURE.equals(currentSignature);

            if (!isValid) {
                Log.e(TAG, "签名验证错误 !  " );
            } else {
                Log.d(TAG, "签名验证成功");
            }

            return isValid;

        } catch (Exception e) {
            Log.e(TAG, "签名验证失败", e);
            return false;
        }
    }

    /**
     * 获取应用的签名SHA-256指纹
     * @param context 应用上下文
     * @return 签名的SHA-256指纹字符串
     */
    public static String getAppSignature(Context context) {
        try {
            // 使用兼容API 26的方法获取签名
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 
                        PackageManager.GET_SIGNING_CERTIFICATES);
                if (packageInfo.signingInfo != null && packageInfo.signingInfo.getSigningCertificateHistory().length > 0) {
                    return getSHA256(packageInfo.signingInfo.getSigningCertificateHistory()[0].toByteArray());
                }
            } else {
                packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 
                        PackageManager.GET_SIGNATURES);
                if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                    return getSHA256(packageInfo.signatures[0].toByteArray());
                }
            }
            return "";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "找不到包名", e);
            return "";
        }
    }

    /**
     * 计算字节数组的SHA-256哈希值
     * @param data 要计算哈希的字节数组
     * @return SHA-256哈希值的十六进制字符串
     */
    private static String getSHA256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256算法不可用", e);
            return "";
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 要转换的字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 判断当前是否为调试版本
     * @return 是否为调试版本
     */
    private static boolean isDebugBuild() {
        return Build.TYPE.equals("eng") || 
               Build.TYPE.equals("user_debug") || 
               Build.TYPE.contains("debug");
    }
}