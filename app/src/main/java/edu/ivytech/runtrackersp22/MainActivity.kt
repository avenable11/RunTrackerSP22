package edu.ivytech.runtrackersp22

import android.Manifest
import android.app.Activity
import android.content.Context

import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
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
import java.text.NumberFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var timer : Timer
    private lateinit var prefs : SharedPreferences
    private var startTime = 0L
    private var elapsedTime = 0L
    private var stopWatchOn = false
    private lateinit var serviceIntent : Intent


    private val permission = registerForActivityResult(ActivityResultContracts
        .RequestMultiplePermissions()) {
        permissions ->
        for (entry in permissions.entries) {
            val permissionName =entry.key
            val isGranted = entry.value
            when {
                isGranted -> {
                    Log.d("main", "Permission Granted")
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
        prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        timer = Timer()
        binding.buttonStartStop.setOnClickListener {
            if(stopWatchOn) {
                stop()
            } else {
                start()
            }
        }
        binding.buttonReset.setOnClickListener {
            reset()
        }



        getLocationPermission()
        serviceIntent = Intent(this, RunLocationService::class.java)
    }

    private fun reset() {
        stop()
        elapsedTime = 0L
        updateViews(elapsedTime)
        binding.buttonStartStop.text = getString(R.string.start)
        LocationRepository.get().deleteLocations()
    }

    private fun stop() {
        stopWatchOn = false
        timer.cancel()
        binding.buttonStartStop.text = getString(R.string.resume)
        updateViews(elapsedTime)
        stopService(serviceIntent)
    }

    private fun start() {
        timer.cancel()
        if(!stopWatchOn) {
            startTime = System.currentTimeMillis() - elapsedTime
        }
        stopWatchOn = true
        binding.buttonStartStop.text = getString(R.string.stop)

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                elapsedTime = System.currentTimeMillis() - startTime
                updateViews(elapsedTime)
            }
        }
        timer = Timer(true)
        timer.scheduleAtFixedRate(task, 0, 100)
        startService(serviceIntent)

    }

    private fun updateViews(elapsedTime: Long) {
        val elapsedTenths = (elapsedTime/100) % 10
        val elapsedSecs = (elapsedTime/1000) % 60
        val elapsedMins = (elapsedTime / (60 * 1000) % 60)
        val elapsedHours = (elapsedTime / (60 * 60 * 1000))
        if (elapsedHours > 0) {
            updateView(binding.textViewHoursValue, elapsedHours, 1);
        }
        updateView(binding.textViewMinsValue, elapsedMins, 2);
        updateView(binding.textViewSecsValue, elapsedSecs, 2);
        updateView(binding.textViewTenthsValue, elapsedTenths, 1);
    }

    private fun updateView(textView: TextView, elapsedTime: Long, minDigits: Int) {
        val numberFormat = NumberFormat.getInstance()
        numberFormat.minimumIntegerDigits = minDigits
        textView.post {
            textView.text = numberFormat.format(elapsedTime)
        }
    }





    private fun getLocationPermission() {
        when {
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
        val locationRequest : LocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
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



}