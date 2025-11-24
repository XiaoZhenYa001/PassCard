package com.passwordmanager.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import androidx.room.Ignore;
import com.passwordmanager.util.DateConverter;
import java.util.Date;

/**
 * 密码条目实体类
 * 用于存储用户的密码信息
 */
@Entity(
    tableName = "password_entries",
    indices = {
        @Index(value = {"username"}),
        @Index(value = {"phone_number"}),
        @Index(value = {"email"}),
        @Index(value = {"service"}),
        @Index(value = {"updated_at"})
    }
)
public class PasswordEntry {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "service")
    private String service;
    
    @ColumnInfo(name = "username")
    private String username;
    
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "password")
    private String password;
    
    @ColumnInfo(name = "note")
    private String note;
    
    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;
    
    @ColumnInfo(name = "updated_at")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;
    
    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    private boolean isPinned;

    // 无参构造函数 - Room需要
    public PasswordEntry() {
        // 默认构造函数，Room需要
        this.isPinned = false;
    }
    
    // 带参数的构造函数
    @Ignore
    public PasswordEntry(String service, String username, String phoneNumber, String email, 
                        String password, String note) {
        this.service = service;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.note = note;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPinned = false;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 更新修改时间
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }
    
    public boolean isPinned() {
        return isPinned;
    }
    
    public void setPinned(boolean pinned) {
        isPinned = pinned;
        updateTimestamp();
    }
}