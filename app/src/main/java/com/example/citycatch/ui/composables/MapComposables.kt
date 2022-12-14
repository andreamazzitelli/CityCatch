package com.example.citycatch.ui.composables

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Observer
import com.example.citycatch.R
import com.example.citycatch.data.model.Place
import com.example.citycatch.data.model.PlaceRenderer
import com.example.citycatch.utils.MarkerInfoWindowAdapter
import com.example.citycatch.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.compose.*

@Composable
fun GoogleMapCluster(vm: MapViewModel){

    // Initialization
    var firstCheck = true
    val location by vm.location.observeAsState()
    //var location: Location? = null

    /*
    vm.location.observe(LocalLifecycleOwner.current, Observer {
        location = it
    })

     */

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(isBuildingEnabled = false, isMyLocationEnabled = true)
        )
    }
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(zoomControlsEnabled = true, indoorLevelPickerEnabled = false, mapToolbarEnabled = false)
        )
    }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState{}

    // This is needed for clustering
    val context = LocalContext.current
    val clusterManager by remember {
        mutableStateOf<ClusterManager<Place>?>(null)
    }




    // Map Creation
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = mapProperties,
        uiSettings = mapUiSettings,
        cameraPositionState = cameraPositionState,
    ){



        // Camera Animation
        if(firstCheck && location != null){
            LaunchedEffect(key1 = true, block = {
                val latLng = LatLng(location!!.latitude, location!!.longitude)
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(latLng).zoom(17f).tilt(45f).build()
                    )
                )
                firstCheck = false
            })
        }

        MarkerClustering(
            context = context,
            vm = vm,
            clusterM = clusterManager,
        )

        LaunchedEffect(key1 = cameraPositionState.isMoving) {
            if (!cameraPositionState.isMoving) {
                clusterManager?.onCameraIdle()
            }
        }

    }
}


@OptIn(MapsComposeExperimentalApi::class)
@SuppressLint("PotentialBehaviorOverride")
@Composable
fun MarkerClustering(context: Context, vm: MapViewModel, clusterM: ClusterManager<Place>?){

    var clusterManager = clusterM

    var landmarks: List<Place> = emptyList()
    val lifecycle = LocalLifecycleOwner.current

    MapEffect{ map->

        if(clusterManager == null){

            clusterManager = ClusterManager<Place>(context, map)
        }

        vm.landmarks.observe(lifecycle, Observer{
            landmarks = it
            clusterManager!!.addItems(landmarks)
        })

        clusterManager!!.renderer = PlaceRenderer(context, map, clusterManager!!)

        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))

        clusterManager!!.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(context))

        clusterManager!!.setOnClusterItemInfoWindowClickListener {

            val location = vm.getLocation().value

            val placeLocation = Location("")
            placeLocation.latitude = it.lat.toDouble()
            placeLocation.longitude = it.lon.toDouble()


            val distance = location!!.distanceTo(placeLocation)

            //TODO Logica dello scattare o meno

            Log.i("TAG C", "$distance")

        }

        clusterManager!!.setOnClusterClickListener { cluster ->
            Log.i("TAG C", "Clicked")
            val builder = LatLngBounds.Builder()
            for(place in cluster.items){
                builder.include(LatLng(place.lat.toDouble(), place.lon.toDouble()))
            }

            //map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 3000, null)
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(builder.build().center).tilt(45f).zoom(16.5f).build()
                )
            )
            true
        }

        //while (landmarks ==null) continue
        clusterManager!!.addItems(landmarks)


    }
}
