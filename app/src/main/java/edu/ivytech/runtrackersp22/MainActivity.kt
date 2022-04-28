package edu.ivytech.runtrackersp22

import android.Manifest
import android.app.Activity

import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import edu.ivytech.runtrackersp22.databinding.ActivityMainBinding
import java.lang.ClassCastException

private const val UPDATE_INTERVAL = 5000L
private const val FASTEST_UPDATE_INTERVAL = 2000L
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    private val permission = registerForActivityResult(ActivityResultContracts
        .RequestMultiplePermissions()) {
        permissions ->
        for (entry in permissions.entries) {
            val permissionName =entry.key
            val isGranted = entry.value
            when {
                isGranted -> {
                    getLocation()
                }
                !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> openSettings()
                else ->  {
                    Log.e("main", "unable to get permission $permissionName")
                }
            }

        }
    }

    private val highAccuracyGPS = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        result ->
        when(result.resultCode) {
            Activity.RESULT_OK -> {
                Snackbar.make(
                    binding.root,
                    "GPS on High Accuracy",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            Activity.RESULT_CANCELED -> {
                Snackbar.make(
                    binding.root,
                    "Unable to turn on GPS",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Action") { checkGPSAccuracy() }
                    .show()

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onLocationChanged(locationResult.lastLocation)
            }
        }

        getLocation()
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun getLocation() {
        when {
            (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) -> {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        onLocationChanged(location)

                    } else {
                        Log.e("main", "Location is null")
                    }
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(binding.root, R.string.permission_required, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                    .show()


            }
            else -> {
                permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
        checkGPSAccuracy()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.myLooper()!!)
    }
    private fun openSettings() {
        Snackbar.make(binding.root, R.string.permission_denied_rationale, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.open_settings)
            {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", this.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .also {
                it.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setLines(6)
            }
            .show()
    }

    private fun checkGPSAccuracy() {
        val builder : LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val responseTask : Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())
        responseTask.addOnFailureListener{
            exception ->
            if(exception is ResolvableApiException) {
                when(exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            highAccuracyGPS.launch(
                                IntentSenderRequest.Builder(exception.resolution).build()
                            )
                        } catch (e: IntentSender.SendIntentException) {

                        } catch(e: ClassCastException){

                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Snackbar.make(binding.root, R.string.nogps, Snackbar.LENGTH_INDEFINITE).show()
                    }
                }
            }
        }
    }

    private fun onLocationChanged(lastLocation: Location) {
        binding.coordinatesTextView.text = getString(
            R.string.coordinateDisplay,
            lastLocation.latitude, lastLocation.longitude)
    }

}