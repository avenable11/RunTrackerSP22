package edu.ivytech.runtrackersp22.database

import androidx.room.TypeConverter
import java.util.*

class LocationConverter {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch : Long?) : Date? {
        return millisSinceEpoch?.let { Date(it) }
    }
}