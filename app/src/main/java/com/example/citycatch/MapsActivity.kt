package com.example.citycatch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.example.citycatch.ui.composables.GoogleMapCluster
import com.example.citycatch.ui.composables.MainScreen
import com.example.citycatch.viewmodel.FirebaseViewModel
import com.example.citycatch.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices

class MapsActivity: ComponentActivity(){

    private val vm: MapViewModel by viewModels {MapViewModel.Factory}
    private val fm: FirebaseViewModel by viewModels {FirebaseViewModel.Factory}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fm.getImages()

        if(checkPermissions()){
            if(isLocationEnabled()){
                if(checkNotPermissions()){
                    requestPermission()
                    return
                }
                vm.startLocalization(LocationServices.getFusedLocationProviderClient(this))
                setContent {
                    MainScreen(vm = vm, fm = fm)
                    //GoogleMapCluster(vm = vm)
                }
            }else{
                Toast.makeText(this, "Turn on Locations", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) vm.startLocalization(LocationServices.getFusedLocationProviderClient(this))
    }

    override fun onPause() {
        super.onPause()
        vm.stopLocalization()
    }

    private fun isLocationEnabled():  Boolean{
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), 100) //il request code è meglio metterlo in una variabile
    }

    private fun checkPermissions(): Boolean {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {return true}
        return false
    }

    private fun checkNotPermissions(): Boolean{
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        {return true}
        return false
    }



}
