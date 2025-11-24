package com.passwordmanager.util;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * 日期转换器
 * 用于Room数据库中Date类型与Long类型的相互转换
 */
public class DateConverter {
    
    /**
     * 将时间戳转换为Date对象
     * @param value 时间戳（毫秒）
     * @return Date对象，如果时间戳为null则返回null
     */
    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }
    
    /**
     * 将Date对象转换为时间戳
     * @param date Date对象
     * @return 时间戳（毫秒），如果Date为null则返回null
     */
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
}