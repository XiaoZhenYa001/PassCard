package com.passwordmanager.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.passwordmanager.model.Password;

import java.util.List;

/**
 * 密码数据访问对象接口
 * 定义了所有与密码条目相关的数据库操作
 */
@Dao
public interface PasswordDao {
    
    /**
     * 插入新的密码条目
     * @param passwordEntry 要插入的密码条目
     * @return 插入条目的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Password passwordEntry);
    
    /**
     * 更新现有密码条目
     * @param passwordEntry 要更新的密码条目
     */
    @Update
    void update(Password passwordEntry);
    
    /**
     * 删除密码条目
     * @param passwordEntry 要删除的密码条目
     */
    @Delete
    void delete(Password passwordEntry);
    
    /**
     * 根据ID获取密码条目
     * @param id 密码条目ID
     * @return 对应的密码条目
     */
    @Query("SELECT * FROM password_entries WHERE id = :id")
    LiveData<Password> getPasswordById(int id);
    
    /**
     * 获取所有密码条目，按更新时间降序排列
     * @return 所有密码条目的列表
     */
    @Query("SELECT * FROM password_entries ORDER BY updated_at DESC")
    LiveData<List<Password>> getAllPasswords();
    
    /**
     * 按用户名搜索密码条目
     * @param username 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(username) LIKE '%' || LOWER(:username) || '%' ORDER BY updated_at DESC")
    LiveData<List<Password>> searchByUsername(String username);
    
    /**
     * 按手机号搜索密码条目
     * @param phoneNumber 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(phone_number) LIKE '%' || LOWER(:phoneNumber) || '%' ORDER BY updated_at DESC")
    LiveData<List<Password>> searchByPhoneNumber(String phoneNumber);
    
    /**
     * 按邮箱搜索密码条目
     * @param email 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(email) LIKE '%' || LOWER(:email) || '%' ORDER BY updated_at DESC")
    LiveData<List<Password>> searchByEmail(String email);
    
    /**
     * 全文搜索密码条目（在服务名称、用户名、手机号和邮箱中查找）
     * @param query 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(service) LIKE '%' || LOWER(:query) || '%' " +
           "OR LOWER(username) LIKE '%' || LOWER(:query) || '%' " +
           "OR LOWER(phone_number) LIKE '%' || LOWER(:query) || '%' " +
           "OR LOWER(email) LIKE '%' || LOWER(:query) || '%' " +
           "ORDER BY updated_at DESC")
    LiveData<List<Password>> searchAll(String query);

    /**
     * 按服务名称搜索密码条目
     * @param service 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(service) LIKE '%' || LOWER(:service) || '%' ORDER BY updated_at DESC")
    LiveData<List<Password>> searchByService(String service);

    /**
     * 按密码搜索密码条目
     * @param password 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(password) LIKE '%' || LOWER(:password) || '%' ORDER BY updated_at DESC")
    LiveData<List<Password>> searchByPassword(String password);

    /**
     * 按备注搜索密码条目
     * @param note 搜索关键词
     * @return 匹配的密码条目列表
     */
    @Query("SELECT * FROM password_entries WHERE LOWER(note) LIKE '%' || LOWER(:note) || '%' ORDER BY updated_at DESC")
    LiveData<List<Password>> searchByNote(String note);
}