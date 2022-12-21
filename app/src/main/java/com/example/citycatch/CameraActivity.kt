package com.example.citycatch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.citycatch.ui.composables.CameraView
import com.example.citycatch.viewmodel.FirebaseViewModel
import com.example.citycatch.viewmodel.MapViewModel
import com.example.citycatch.viewmodel.SensorViewModel
import com.google.android.gms.location.LocationServices
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : ComponentActivity() {

    private val svm: SensorViewModel by viewModels {SensorViewModel.Factory}
    private val vm: MapViewModel by viewModels { MapViewModel.Factory}
    private val fm: FirebaseViewModel by viewModels { FirebaseViewModel.Factory }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.i("TAG", "Permission granted")
            } else {
                Log.i("TAG", "Permission denied")
            }
        }

    private lateinit var cameraExecutor: ExecutorService

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val lat = intent.getDoubleExtra("marker_lat", 0.0)
        val lon = intent.getDoubleExtra("marker_lon", 0.0)

        Log.i("TAG INTENT", "$lat $lon")

        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        svm.setSensorManager(sm)
        svm.setMarkerLocation(lat, lon)
        svm.registerListener()

        vm.startLocalization(LocationServices.getFusedLocationProviderClient(this))

        vm.location.observe(this, Observer {
            svm.setUserLocation(it)
        })

        setContent {
            CameraView(
                executor = cameraExecutor,
                sensorsViewModel = svm,
                firebaseViewModel = fm,
                onError = { Log.e("TAG", "View error:", it) }
            )
        }



        requestCameraPermission()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("TAG", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA) -> Log.i("TAG", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        vm.startLocalization(LocationServices.getFusedLocationProviderClient(this))
    }


    override fun onRestart() {
        super.onRestart()
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        svm.setSensorManager(sm)
        svm.registerListener()
        vm.stopLocalization()

    }

    override fun onPause() {
        super.onPause()
        svm.unregisterListener()

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        svm.unregisterListener()
    }


}