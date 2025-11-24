package com.passwordmanager.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.passwordmanager.model.Password;
import com.passwordmanager.repository.PasswordRepository;

import java.util.List;

/**
 * 密码ViewModel类
 * 作为UI和数据层之间的桥梁，处理UI相关的数据逻辑
 */
public class PasswordViewModel extends AndroidViewModel {
    
    private final PasswordRepository repository;
    private final LiveData<List<Password>> allPasswords;
    
    /**
     * 构造函数
     * @param application 应用实例
     */
    public PasswordViewModel(Application application) {
        super(application);
        repository = new PasswordRepository(application);
        allPasswords = repository.getAllPasswords();
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
        return repository.getPasswordById(id);
    }
    
    /**
     * 按用户名搜索密码条目
     * @param username 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByUsername(String username) {
        return repository.searchByUsername(username);
    }
    
    /**
     * 按手机号搜索密码条目
     * @param phoneNumber 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByPhoneNumber(String phoneNumber) {
        return repository.searchByPhoneNumber(phoneNumber);
    }
    
    /**
     * 按邮箱搜索密码条目
     * @param email 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByEmail(String email) {
        return repository.searchByEmail(email);
    }
    
    /**
     * 全文搜索密码条目
     * @param query 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchAll(String query) {
        return repository.searchAll(query);
    }

    /**
     * 按服务名称搜索密码条目
     * @param service 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByService(String service) {
        return repository.searchByService(service);
    }

    /**
     * 按密码搜索密码条目
     * @param password 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByPassword(String password) {
        return repository.searchByPassword(password);
    }

    /**
     * 按备注搜索密码条目
     * @param note 搜索关键词
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchByNote(String note) {
        return repository.searchByNote(note);
    }
    
    /**
     * 根据搜索类型搜索密码条目
     * @param query 搜索关键词
     * @param searchType 搜索类型
     * @return 匹配的密码条目列表LiveData
     */
    public LiveData<List<Password>> searchPasswords(String query, String searchType) {
        switch (searchType) {
            case "username":
                return searchByUsername(query);
            case "phone":
                return searchByPhoneNumber(query);
            case "email":
                return searchByEmail(query);
            case "service":
                return searchByService(query);
            case "password":
                return searchByPassword(query);
            case "note":
                return searchByNote(query);
            default:
                return searchAll(query);
        }
    }
    
    /**
     * 插入新的密码条目
     * @param passwordEntry 要插入的密码条目
     */
    public void insert(Password passwordEntry) {
        repository.insert(passwordEntry);
    }
    
    /**
     * 更新现有密码条目
     * @param passwordEntry 要更新的密码条目
     */
    public void update(Password passwordEntry) {
        repository.update(passwordEntry);
    }
    
    /**
     * 删除密码条目
     * @param passwordEntry 要删除的密码条目
     */
    public void delete(Password passwordEntry) {
        repository.delete(passwordEntry);
    }
}