package com.example.citycatch.data

import android.util.Log
import com.example.citycatch.data.model.Place
import com.example.citycatch.utils.APIs
import com.example.citycatch.utils.WebAPIs

class PlaceRepository {

    private var webAPIs: APIs = WebAPIs().retrofit.create(APIs::class.java)

    suspend fun read(): List<Place>{
        return try {
            val reply = this.webAPIs.getPlacesUser(FirebaseRepository.getUserUID())
            Log.i("TAG PLACES", "Here")
            reply.body()!!
        } catch (e: Exception){
            Log.i("TAG SUSPEND ERROR", "Failed to Retrieve Data from Remote Server")
            emptyList()
        }
    }
}