package com.example.citycatch.viewmodel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.math.PI

class SensorViewModel: ViewModel(), SensorEventListener2 {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SensorViewModel()
            }
        }
    }

    private val _direction: MutableLiveData<String> = MutableLiveData<String>()
    val direction: LiveData<String> = _direction

    val CENTER_TAG = "Center"
    val LEFT_TAG = "Left"
    val RIGHT_TAG = "Right"

    private var sensorsManager: SensorManager? = null
    private var rotationVector = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var angles = FloatArray(3)

    private var bearing = 0.0f
    private var roll = 0.0f
    private var a = 0.0f

    private var markerLocation = Location("")
    private var userLocation = Location("")

    fun setUserLocation(loc: Location){
       userLocation = loc
        //Log.i("TAG SVM LOC", userLocation.toString())
    }
    fun setMarkerLocation(lat: Double, lon: Double){
        markerLocation.latitude = lat
        markerLocation.longitude = lon
    }

    fun setSensorManager(sm: SensorManager){
        sensorsManager = sm
    }

    fun registerListener(): Boolean{
        if(sensorsManager == null)return false

        sensorsManager!!.registerListener(this,
            sensorsManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL)
        return true

    }

    fun unregisterListener(): Boolean{
        if (sensorsManager == null)return false
        sensorsManager!!.unregisterListener(this,
            sensorsManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))
        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {


        rotationVector = event?.values!!.clone()
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        SensorManager.getOrientation(rotationMatrix, angles)

        roll = a*roll + (1-a)*angles[2]*180f/ PI.toFloat()

        var turnDirection = bearing - roll
        turnDirection %= 360
        if(turnDirection>180){
            turnDirection-=360
        }
        turnDirection*=-1

        //Log.i("TAG", "$roll")

        if(turnDirection > 20.0f){
            _direction.value = RIGHT_TAG
        }
        else if(turnDirection < -20.0f){
            _direction.value = LEFT_TAG
        }
        else{
            _direction.value = CENTER_TAG
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onFlushCompleted(sensor: Sensor?) {}



}