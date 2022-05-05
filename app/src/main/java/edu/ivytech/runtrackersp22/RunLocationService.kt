package edu.ivytech.runtrackersp22

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import edu.ivytech.runtrackersp22.database.RunLocation
import java.util.*

private const val UPDATE_INTERVAL = 5000L
private const val FASTEST_UPDATE_INTERVAL = 2000L
private const val TAG = "RunsService"
class RunLocationService : Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val locationRepository = LocationRepository.get()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                //onLocationChanged(locationResult.lastLocation)
                var runLocation = RunLocation(Date(),
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude)
                locationRepository.addLocation(runLocation)
            }
        }
        try {
            Looper.myLooper()?.let { looper ->
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                    looper
                )
            }
        } catch (ex: SecurityException) {
            Log.e(TAG, ex.message!!)
        }
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}