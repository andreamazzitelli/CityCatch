package com.example.citycatch.data

import android.util.Log
import com.example.citycatch.data.model.Place
import com.example.citycatch.utils.APIs
import com.example.citycatch.utils.WebAPIs
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object PlaceRepository {

    private var webAPIs: APIs = WebAPIs().retrofit.create(APIs::class.java)

    suspend fun read(): List<Place>{
        return try {
            val reply = this.webAPIs.getPlacesUser(FirebaseRepository.getUserUID())
            //Log.i("TAG PLACES", "Here")
            reply.body()!!
        } catch (e: Exception){
            Log.i("TAG SUSPEND ERROR", "Failed to Retrieve Data from Remote Server")
            emptyList()
        }
    }

    fun addVisitedPlace(locationName: String, uid: String){
        try {
            runBlocking {
                launch {
                    webAPIs.addVisitedPlace(locationName, uid)
                    //Log.i("TAG UPDATE", "Updating")
                }
            }

        }
        catch (e: Exception){
            Log.i("TAG FAIL", "Failed to Add Visited Place")
        }
    }

}