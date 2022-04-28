package edu.ivytech.runtrackersp22

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import edu.ivytech.runtrackersp22.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
                    Log.e("main", "unable to get permision $permissionName")
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLocation()
    }

    private fun getLocation() {
        when {
            (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) -> {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        binding.coordinatesTextView.text = getString(
                            R.string.coordinateDisplay,
                            location.latitude, location.longitude
                        )
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

}