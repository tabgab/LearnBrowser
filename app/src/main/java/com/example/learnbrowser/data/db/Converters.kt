package com.example.learnbrowser.data.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database.
 * These converters handle conversion between Date objects and Long timestamps.
 */
class Converters {
    /**
     * Converts a timestamp (Long) to a Date object.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a Date object to a timestamp (Long).
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
