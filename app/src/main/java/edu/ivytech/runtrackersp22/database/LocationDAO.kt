package edu.ivytech.runtrackersp22.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDAO {

    @Insert
    fun insertLocation(location : RunLocation)

    @Query("Select * from runlocation order by date")
    fun getLocations() : LiveData<List<RunLocation>>

    @Query("delete from runlocation")
    fun deleteRunLocations()
}