package edu.ivytech.runtrackersp22.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [RunLocation::class], version = 1)
@TypeConverters(LocationConverter::class)
abstract class RunDatabase : RoomDatabase() {
    abstract fun locationDao() : LocationDAO
}