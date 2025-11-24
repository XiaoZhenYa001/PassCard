package com.passwordmanager.data;

import android.content.Context;

import androidx.room.Database;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.passwordmanager.model.Password;
import com.passwordmanager.util.DateConverter;

/**
 * 应用数据库类
 * 使用Room持久性库管理SQLite数据库
 */
@Database(entities = {com.passwordmanager.model.Password.class}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    // 数据库执行器，用于异步执行数据库操作
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);
    
    // 数据库名称
    private static final String DATABASE_NAME = "password_manager_db";
    
    // 单例实例
    private static volatile AppDatabase INSTANCE;
    
    // 获取DAO接口
    public abstract PasswordDao passwordDao();
    
    /**
     * 获取数据库实例（单例模式）
     * @param context 应用上下文
     * @return 数据库实例
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 保存旧数据
            database.execSQL("CREATE TABLE IF NOT EXISTS password_entries_backup AS SELECT * FROM password_entries");
            
            // 删除旧表
            database.execSQL("DROP TABLE IF EXISTS password_entries");
            
            // 创建新表
            database.execSQL("CREATE TABLE IF NOT EXISTS password_entries ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "service TEXT,"
                    + "username TEXT,"
                    + "phone_number TEXT,"
                    + "email TEXT,"
                    + "password TEXT,"
                    + "note TEXT,"
                    + "created_at INTEGER,"
                    + "updated_at INTEGER"
                    + ")");
            
            // 恢复数据
            database.execSQL("INSERT INTO password_entries SELECT * FROM password_entries_backup");
            
            // 删除备份表
            database.execSQL("DROP TABLE IF EXISTS password_entries_backup");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建索引以提高查询性能
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_username ON password_entries (username)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_phone_number ON password_entries (phone_number)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_email ON password_entries (email)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_service ON password_entries (service)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_updated_at ON password_entries (updated_at)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 添加 is_pinned 字段
            database.execSQL("ALTER TABLE password_entries ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0");
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_is_pinned ON password_entries (is_pinned)");
        }
    };


    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}