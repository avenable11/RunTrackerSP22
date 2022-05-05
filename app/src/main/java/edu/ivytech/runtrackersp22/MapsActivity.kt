package edu.ivytech.runtrackersp22

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import edu.ivytech.runtrackersp22.database.RunLocation
import edu.ivytech.runtrackersp22.databinding.ActivityMapsBinding
import java.util.*

private const val TAG = "MapsActivity"
private const val INTERVAL_REFRESH = 10 * 1000L
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fused : FusedLocationProviderClient
    private lateinit var locList : List<RunLocation>
    private lateinit var timer : Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val viewModel : MapsViewModel by viewModels()
        viewModel.locationListLiveData.observe(this) { locations ->
            locList = locations
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16.5f))
            updateMap()
            setMapToRefresh()
        } catch(e: SecurityException) {
            Log.e(TAG, e.message!!)
        }
        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
       // mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    @SuppressLint("MissingPermission")
    private fun updateMap() {
        try {
            fused = LocationServices.getFusedLocationProviderClient(applicationContext)
            fused.lastLocation.addOnSuccessListener { location ->
                setCurrentLocationMarker(location)
                displayRun()
            }
        } catch (ex : SecurityException){
            Log.e(TAG, ex.message!!)
        }

    }

    private fun setCurrentLocationMarker(location : Location) {
        val mapZoom = mMap.cameraPosition.zoom
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(location.latitude, location.longitude))
                .zoom(mapZoom)
                .bearing(0f)
                .tilt(25f)
                .build()
        ))
        mMap.clear()
        mMap.addMarker(MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title("You are here"))
    }

    private fun setMapToRefresh() {
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                this@MapsActivity.runOnUiThread {updateMap()}
            }
        }
        timer.schedule(task, INTERVAL_REFRESH, INTERVAL_REFRESH)
    }

    private fun displayRun() {
        if(this::mMap.isInitialized) {
            var polyLine = PolylineOptions()
            if(this::locList.isInitialized) {
                for(l in locList) {
                    var point : LatLng = LatLng(l.latitude, l.longitude)
                    polyLine.add(point)
                }
            }
            polyLine.width(10f)
            polyLine.color(Color.RED)
            mMap.addPolyline(polyLine)
        }
    }
}