package com.example.citycatch.utils

import android.util.Log
import com.example.citycatch.data.model.Place
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface APIs{

    @GET("/places")
    suspend fun getPlaces() : Response<List<Place>>

    @GET("/places/{uid}")
    suspend fun getPlacesUser(@Path("uid")userUid: String) : Response<List<Place>>

    @POST("/addUser")
    suspend fun addUser(@Query("mail") mail: String, @Query("id") id: String) : Unit
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