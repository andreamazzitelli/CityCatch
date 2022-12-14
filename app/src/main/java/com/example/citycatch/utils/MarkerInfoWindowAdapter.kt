package com.example.citycatch.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.citycatch.R
import com.example.citycatch.data.model.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {

        val place = marker.tag as? Place ?: return null
        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_textbox_layout, null
        )
        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = place.placeName

        view.findViewById<TextView>(
            R.id.n_visitors
        ).text = "Visited by ${place.n_visitors}"

        val button = view.findViewById<Button>(R.id.button)

        if (place.seen) {
            button.text = "GOT IT!"
            button.isEnabled = false
        } else {
            button.text = "TAKE PHOTO"
        }

        //il controllo del bottone forse va messo qua

        return view

    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}
