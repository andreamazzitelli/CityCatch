package com.example.citycatch.ui.composables

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import com.example.citycatch.CameraActivity
import com.example.citycatch.R
import com.example.citycatch.data.model.Place
import com.example.citycatch.data.model.PlaceRenderer
import com.example.citycatch.ui.theme.LightOrange
import com.example.citycatch.ui.theme.Orange
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
    val activity = LocalContext.current as Activity

    val tooFarPopUp = remember {
        mutableStateOf(false)
    }

    MapEffect{ map->

        if(clusterManager == null){

            clusterManager = ClusterManager<Place>(context, map)
        }

        vm.landmarks.observe(lifecycle, Observer{
            Log.i("TAG PLACES", "Observing")
            if(it.isNotEmpty()) {
                Log.i("TAG PLACES", "Not Empty")
                landmarks = it
                clusterManager!!.addItems(landmarks)
            }
        })

        clusterManager!!.renderer = PlaceRenderer(context, map, clusterManager!!)

        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))

        clusterManager!!.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(context))

        clusterManager!!.setOnClusterItemInfoWindowClickListener {

            if(!it.seen) {
                val location = vm.getLocation().value

                val placeLocation = Location("")
                placeLocation.latitude = it.lat.toDouble()
                placeLocation.longitude = it.lon.toDouble()

                val distance = location!!.distanceTo(placeLocation)

                if(distance > 500){
                    //Toast.makeText(context, "TOO FAR", Toast.LENGTH_LONG).show()
                    tooFarPopUp.value = true
                    Log.i("TAG BAD", "$distance")
                }
                else {
                    //Toast.makeText(context, "OK", Toast.LENGTH_LONG).show()
                    Log.i("TAG OK", "$distance")
                    val intent = Intent(context, CameraActivity::class.java)
                    intent.putExtra("marker_lat", placeLocation.latitude)
                    intent.putExtra("marker_lon", placeLocation.longitude)
                    intent.putExtra("marker_name", it.placeName)
                    context.startActivity(intent)

                }
            }

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

    if(tooFarPopUp.value){
        TooFarPopUP(tooFarPopUp)
    }
}

@Composable
fun TooFarPopUP(state: MutableState<Boolean>){

    AlertDialog(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        backgroundColor = LightOrange,
        onDismissRequest = {
            state.value = false
        },
        title = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ){
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "TOO FAR!!",
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "You're too far, get closer to the location and try again",
                textAlign = TextAlign.Center
            )
        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
                    onClick = {
                        state.value = false
                    }) {
                    Text(text = "Ok")
                }
            }
        }

    )
}

@Composable
fun ErrorPopUp(){

    val context = LocalContext.current
    val activity = LocalContext.current as Activity

    AlertDialog(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        backgroundColor = Color.White,
        onDismissRequest = {},
        title = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ){
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "PERMISSION NOT GRANTED",
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {

            val image = context.assets.open("permissions.png")
            val bitmap = BitmapFactory.decodeStream(image)
            image.close()

            Column() {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "To work properly this app needs PRECISE LOCATION, please give us the required permission by going Settings->Apps->CityCatch->Permissions or select the following",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    modifier= Modifier
                        .fillMaxWidth()
                        .border(2.dp, LightOrange),
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = ""
                )
            }

        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
                    onClick = {
                        activity.finish()
                    }) {
                    Text(text = "Close Application")
                }
            }
        }

    )


}
