package edu.ivytech.runtrackersp22

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.ivytech.runtrackersp22.database.RunDatabase
import edu.ivytech.runtrackersp22.database.RunLocation
import java.util.concurrent.Executors

private const val DATABASE_NAME = "location_database.db"
class LocationRepository private constructor(context : Context){
    companion object{
        private var INSTANCE : LocationRepository? = null

        fun initialize(context: Context) {
            if(INSTANCE == null) {
                INSTANCE = LocationRepository(context)
            }
        }

        fun get(): LocationRepository {
            return INSTANCE?: throw IllegalStateException("Location Repository must be initialized")
        }
    }
    private val database: RunDatabase =
        Room.databaseBuilder(context.applicationContext,
            RunDatabase::class.java,
            DATABASE_NAME).build()
    private val locationDao = database.locationDao()
    private val executor = Executors.newSingleThreadExecutor()
    fun getLocations(): LiveData<List<RunLocation>> = locationDao.getLocations()
    fun addLocation(runLocation: RunLocation) {
        executor.execute {
            locationDao.insertLocation(runLocation)
        }
    }
    fun deleteLocations() {
        executor.execute{
            locationDao.deleteRunLocations()
        }
    }

}