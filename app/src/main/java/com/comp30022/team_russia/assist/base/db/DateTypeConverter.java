package com.comp30022.team_russia.assist.base.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Type converter for storing Date in SQLite. Used by Room database.
 */
public class DateTypeConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
