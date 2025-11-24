package com.passwordmanager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.passwordmanager.data.AppDatabase;
import com.passwordmanager.data.PasswordDao;
import com.passwordmanager.model.Password;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 密码数据仓库类
 * 作为数据操作的中间层，连接UI和数据库
 */
public class PasswordRepository {
    
    private final PasswordDao passwordDao;
    private final LiveData<List<Password>> allPasswords;
    private final ExecutorService executorService;
    
    /**
     * 构造函数
     * @param application 应用实例
     */
    public PasswordRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        passwordDao = database.passwordDao();
        allPasswords = passwordDao.getAllPasswords();
        executorService = Executors.newFixedThreadPool(4); // 创建线程池执行数据库操作
    }
    
    /**
     * 获取所有密码条目
     * @return 所有密码条目的LiveData
     */
    public LiveData<List<Password>> getAllPasswords() {
        return allPasswords;
    }
    
    /**
     * 根据ID获取密码条目
     * @param id 密码条目ID
     * @return 对应的密码条目LiveData
     */
    public LiveData<Password> getPasswordById(int id) {
        return passwordDao.getPasswordById(id);
    }
    
    /**
     * 按用户名搜索密码条目
     * @param username 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByUsername(String username) {
        return passwordDao.searchByUsername(username);
    }
    
    /**
     * 按手机号搜索密码条目
     * @param phoneNumber 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByPhoneNumber(String phoneNumber) {
        return passwordDao.searchByPhoneNumber(phoneNumber);
    }
    
    /**
     * 按邮箱搜索密码条目
     * @param email 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByEmail(String email) {
        return passwordDao.searchByEmail(email);
    }
    
    /**
     * 全文搜索密码条目
     * @param query 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchAll(String query) {
        return passwordDao.searchAll(query);
    }

    /**
     * 按服务名称搜索密码条目
     * @param service 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByService(String service) {
        return passwordDao.searchByService(service);
    }

    /**
     * 按密码搜索密码条目
     * @param password 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByPassword(String password) {
        return passwordDao.searchByPassword(password);
    }

    /**
     * 按备注搜索密码条目
     * @param note 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByNote(String note) {
        return passwordDao.searchByNote(note);
    }
    
    
    /**
     * 插入新的密码条目
     * @param passwordEntry 要插入的密码条目
     */
    public void insert(Password passwordEntry) {
        executorService.execute(() -> passwordDao.insert(passwordEntry));
    }
    
    /**
     * 更新现有密码条目
     * @param passwordEntry 要更新的密码条目
     */
    public void update(Password passwordEntry) {
        passwordEntry.updateTimestamp(); // 更新修改时间
        executorService.execute(() -> passwordDao.update(passwordEntry));
    }
    
    /**
     * 删除密码条目
     * @param passwordEntry 要删除的密码条目
     */
    public void delete(Password passwordEntry) {
        executorService.execute(() -> passwordDao.delete(passwordEntry));
    }
}