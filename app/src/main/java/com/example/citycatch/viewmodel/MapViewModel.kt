package com.example.citycatch.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.citycatch.data.PlaceRepository
import com.example.citycatch.data.model.Place
import com.google.android.gms.location.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MapViewModel: ViewModel() {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                MapViewModel()
            }
        }
    }

    private val _location: MutableLiveData<Location> = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _landmarks: MutableLiveData<List<Place>> = MutableLiveData<List<Place>>()
    val landmarks: LiveData<List<Place>> = _landmarks

    private var active = false

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            _location.value = locationResult.lastLocation!!

            //Log.i("TAG VM", location.value!!.latitude.toString() + " " + location.value!!.longitude.toString())
        }
    }

    @JvmName("getLocation1")
    fun getLocation(): LiveData<Location> {
        return location
    }

    @SuppressLint("MissingPermission")
    fun startLocalization(fl: FusedLocationProviderClient){
        //Log.i("TAG", "Starting Loc")

        if(!active) {
            //Log.i("TAG", "Not Active")
            active = true
            fusedLocationClient = fl
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            newPosition()
        }
    }

    fun stopLocalization(){
        //Log.i("TAG", "Stop Loc")
        if(fusedLocationClient!=null){
            fusedLocationClient!!.removeLocationUpdates(locationCallback)
            active = false
        }
    }

    private fun newPosition(){

        //Log.i("TAG", "New Position")

        // TODO handle emptylist() from async call
        viewModelScope.launch {
            val reply = async { PlaceRepository.read() }
            _landmarks.value = reply.await()
            Log.i("TAG", _landmarks.value.toString())
        }
    }

    fun reloadPlaces(){
        //Log.i("TAG RELOADING", "Reloading Places")
        newPosition()
    }

}