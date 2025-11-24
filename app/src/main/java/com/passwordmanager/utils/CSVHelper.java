package com.passwordmanager.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.passwordmanager.model.Password;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVHelper {
    private static final String TAG = "CSVHelper";

    public interface ExportCallback {
        void onProgress(int progress, int total);
        void onSuccess(String filePath);
        void onError(String error);
    }

    public interface ImportCallback {
        void onProgress(int progress, int total);
        void onSuccess(List<Password> entries);
        void onError(String error);
    }

    public static void exportToCSV(Context context, List<Password> entries, ExportCallback callback) {
        new Thread(() -> {
            try {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }

                String fileName = "passwords_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
                File file = new File(downloadsDir, fileName);

                FileWriter writer = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);

                // 写入标题行，确保与导入时的字段顺序一致
                String[] headers = {"服务", "用户名", "手机号", "邮箱", "密码", "备注"};
                bufferedWriter.write(String.join(",", headers) + "\n");

                int total = entries.size();
                for (int i = 0; i < total; i++) {
                    Password entry = entries.get(i);
                    String[] values = {
                        entry.getService(),
                        entry.getUsername(),
                        entry.getPhoneNumber(),
                        entry.getEmail(),
                        entry.getPassword(),
                        entry.getNote()
                    };
                    
                    // 确保每个字段都经过正确的CSV转义
                    for (int j = 0; j < values.length; j++) {
                        values[j] = escapeCSV(values[j]);
                    }
                    
                    bufferedWriter.write(String.join(",", values) + "\n");
                    callback.onProgress(i + 1, total);
                }

                bufferedWriter.close();
                writer.close();

                callback.onSuccess(file.getAbsolutePath());

            } catch (IOException e) {
                Log.e(TAG, "导出CSV文件时出错", e);
                callback.onError("导出CSV文件时出错: " + e.getMessage());
            }
        }).start();
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        // 如果字符串包含逗号、引号或换行符，则需要用引号包裹并转义
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static void importFromCSV(Context context, Uri uri, ImportCallback callback) {
        new Thread(() -> {
            List<Password> entries = new ArrayList<>();
            InputStream inputStream = null;
            BufferedReader reader = null;

            try {
                String mimeType = context.getContentResolver().getType(uri);
                String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (mimeType == null || (!mimeType.equals("text/csv") && !"csv".equalsIgnoreCase(fileExtension))) {
                    callback.onError("请选择有效的CSV文件");
                    return;
                }

                inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    callback.onError("无法打开文件");
                    return;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String headerLine = reader.readLine();
                if (headerLine == null) {
                    callback.onError("CSV文件为空");
                    return;
                }

                // 解析标题行，获取字段索引
                List<String> headers = parseCSVLine(headerLine);
                int serviceIndex = headers.indexOf("服务");
                int usernameIndex = headers.indexOf("用户名");
                int phoneIndex = headers.indexOf("手机号");
                int emailIndex = headers.indexOf("邮箱");
                int passwordIndex = headers.indexOf("密码");
                int noteIndex = headers.indexOf("备注");

                // 验证必要的字段是否存在
                if (serviceIndex == -1 && usernameIndex == -1 && phoneIndex == -1 && emailIndex == -1 && passwordIndex == -1) {
                    callback.onError("CSV文件格式无效，未找到任何有效字段");
                    return;
                }

                String line;
                int lineCount = 0;
                int totalLines = 0;

                while (reader.readLine() != null) totalLines++;

                inputStream.close();
                inputStream = context.getContentResolver().openInputStream(uri);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                reader.readLine(); // 跳过标题行

                while ((line = reader.readLine()) != null) {
                    try {
                        List<String> values = parseCSVLine(line);
                        
                        // 确保values列表长度足够
                        while (values.size() < headers.size()) {
                            values.add("");
                        }

                        // 根据字段索引获取对应的值
                        String service = serviceIndex >= 0 && serviceIndex < values.size() ? values.get(serviceIndex).trim() : "";
                        String username = usernameIndex >= 0 && usernameIndex < values.size() ? values.get(usernameIndex).trim() : "";
                        String phone = phoneIndex >= 0 && phoneIndex < values.size() ? values.get(phoneIndex).trim() : "";
                        String email = emailIndex >= 0 && emailIndex < values.size() ? values.get(emailIndex).trim() : "";
                        String password = passwordIndex >= 0 && passwordIndex < values.size() ? values.get(passwordIndex).trim() : "";
                        String note = noteIndex >= 0 && noteIndex < values.size() ? values.get(noteIndex).trim() : "";

                        // 创建新的密码条目
                        Password entry = new Password(service, username, phone, email, password, note);
                        entries.add(entry);

                        lineCount++;
                        callback.onProgress(lineCount, totalLines);
                    } catch (Exception e) {
                        Log.e(TAG, "解析CSV行时出错: " + line, e);
                    }
                }

                if (entries.isEmpty()) {
                    callback.onError("没有找到有效的密码条目");
                } else {
                    callback.onSuccess(entries);
                }

            } catch (IOException e) {
                Log.e(TAG, "读取CSV文件时出错", e);
                callback.onError("读取CSV文件时出错: " + e.getMessage());
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "关闭文件流时出错", e);
                }
            }
        }).start();
    }

    private static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 处理双引号转义
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        result.add(currentField.toString());
        return result;
    }
}