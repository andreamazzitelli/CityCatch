package com.example.citycatch.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.example.citycatch.R
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

    @SerializedName("visited")
    val seen: Boolean = false,

    @SerializedName("n_visitators")
    val n_visitors: Int = 0

): ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat.toDouble(), lon.toDouble())
    override fun getTitle(): String = placeName
    override fun getSnippet(): String = ""

}

class PlaceRenderer(
    private var context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Place>
): DefaultClusterRenderer<Place>(context, map, clusterManager){

    override fun shouldRenderAsCluster(cluster: Cluster<Place>): Boolean {
        //super.shouldRenderAsCluster(cluster)
        return cluster.size > 1
    }

    override fun onBeforeClusterItemRendered(item: Place, markerOptions: MarkerOptions) {
        //super.onBeforeClusterItemRendered(item, markerOptions)

        var marker = BitmapDescriptorFactory.defaultMarker()

        val vectorDrawable = if(item.seen){
            ResourcesCompat
                .getDrawable(context.resources, R.drawable.green_marker, null)
            }
            else{
                ResourcesCompat
                    .getDrawable(context.resources, R.drawable.red_marker, null)
                }

        if (vectorDrawable!=null){
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            marker = BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        markerOptions.title(item.title).position(item.position)
            .icon(marker)
    }

    override fun onClusterItemRendered(clusterItem: Place, marker: Marker) {
        //super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem
    }

}
