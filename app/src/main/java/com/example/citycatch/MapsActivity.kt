package com.example.citycatch

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.citycatch.ui.composables.MainScreen
import com.example.citycatch.viewmodel.FirebaseViewModel
import com.example.citycatch.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices

class MapsActivity: ComponentActivity(){

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                Log.i("TAG PERMISSION", "Granted")
                vm.startLocalization(LocationServices.getFusedLocationProviderClient(this))
                setContent{
                    MainScreen(vm = vm, fm = fm)
                }
            }
            else{
                Log.i("TAG PERMISSION", "Denied")
            }
        }

    private val vm: MapViewModel by viewModels {MapViewModel.Factory}
    private val fm: FirebaseViewModel by viewModels {FirebaseViewModel.Factory}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fm.getImages()
        /*
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
            Log.i("TAG PERM", "Permission Request")
            requestPermission()
        }
        */
        requestLocationPermissions()

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

    private fun requestLocationPermissions(){
        when{
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED )
            && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) -> {
                    Log.i("TAG PERMISSION", "Permission previously granted")

                vm.startLocalization(LocationServices.getFusedLocationProviderClient(this))
                setContent{
                    MainScreen(vm = vm, fm = fm)
                }
                }

            ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION) -> Log.i("TAG PERMISSION", "Show Localization Permission Dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }



}
