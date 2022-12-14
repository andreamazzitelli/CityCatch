package com.example.citycatch.data.model

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


data class Place(
    @SerializedName("name")
    val placeName: String,

    @SerializedName("lat")
    val lat: String,

    @SerializedName("lon")
    val lon: String,
    val seen: Boolean = false

): ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat.toDouble(), lon.toDouble())
    override fun getTitle(): String = placeName
    override fun getSnippet(): String = ""

}

class PlaceRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Place>
): DefaultClusterRenderer<Place>(context, map, clusterManager){

    override fun shouldRenderAsCluster(cluster: Cluster<Place>): Boolean {
        //super.shouldRenderAsCluster(cluster)
        return cluster.size > 1
    }

    override fun onBeforeClusterItemRendered(item: Place, markerOptions: MarkerOptions) {
        //super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions.title(item.title).position(item.position)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
    }

    override fun onClusterItemRendered(clusterItem: Place, marker: Marker) {
        //super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem
    }

}
