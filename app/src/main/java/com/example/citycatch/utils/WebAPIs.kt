package com.example.citycatch.utils

import android.util.Log
import com.example.citycatch.data.model.Place
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


interface APIs{
    // the structure here is using @METHOD("path")
    //signature of the function that is something like
    //suspend fun doPost(@Query("time") time:String, @Query("azimuth") azimuth:Float) : Response<String>
    @GET("/places")
    suspend fun getPlaces() : Response<List<Place>>
}

class WebAPIs {

    lateinit var retrofit : Retrofit
    init {
        val baseUrl = "https://alessandromarzilli.pythonanywhere.com"

        try {
            retrofit =
                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON converter to Kotlin object
                    .build()
        }
        catch (e: Exception){
            Log.i("RETROFIT BINDING ERROR", "Failed to Bind Retrofit to Remote Server")
            //TODO Make a Toast
        }

    }
}