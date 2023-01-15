package com.example.citycatch.viewmodel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.data.model.UserScore
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FirebaseViewModel : ViewModel() {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                FirebaseViewModel()
            }
        }
    }

    private val _photoList: MutableLiveData<MutableList<String>> =
        MutableLiveData<MutableList<String>>(mutableListOf())
    val photoList: LiveData<MutableList<String>> = _photoList

    private val _photoNumber: MutableLiveData<Int> = MutableLiveData<Int>(0)
    val photoNumber: LiveData<Int> = _photoNumber

    private val _points: MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    val points: LiveData<Double> = _points

    private val _userScores: MutableLiveData<MutableList<UserScore>> = MutableLiveData<MutableList<UserScore>>(mutableListOf())
    val userScores: LiveData<MutableList<UserScore>> = _userScores

    private val orderedScores = FirebaseRepository.getReferenceRDB().orderByValue()

    private val _imageBitMap: MutableLiveData<Bitmap> = MutableLiveData<Bitmap>(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    val imageBitmap: LiveData<Bitmap> = _imageBitMap

    private var updateImage = true

    init {
        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {

        orderedScores.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var temp = mutableListOf<UserScore>()
                var value = snapshot.getValue().toString()
                value = value.substring(1, value.length - 1)


                value.split(", ").forEach {
                    val splittedS = it.split("=")
                    val mail = splittedS[0].replace("%2E", ".")
                    temp.add(UserScore(mail, splittedS[1].toDouble()))

                    if(mail == FirebaseRepository.getUser()!!.email){
                        _points.value = splittedS[1].toDouble()
                    }

                }
                temp.sortBy { it.score }
                temp.reverse()

                _userScores.value = temp

                //Log.d("TAG", "Value is: " + value)
                //Log.d("TAG", temp.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }


    fun getImages() {

        if (_photoList.value == null) {
            _photoList.value = mutableListOf()
        }

        FirebaseRepository.getStorageReference().child(FirebaseRepository.getUserUID())
            .listAll().addOnSuccessListener { list ->

                list.items.sortBy {
                    it.name.split("-")[0]
                }
                list.items.reverse()

                _photoNumber.value = list.items.size - 1

                list.items.forEach {
                    Log.i("TAG IMAGE", it.toString())
                    if (!it.toString().contains("profile.jpg")) {
                        val ref =
                            FirebaseRepository.getStorage().getReferenceFromUrl(it.toString())
                        ref.downloadUrl.addOnSuccessListener { uri ->

                            if (!_photoList.value!!.contains(uri.toString())) {
                                _photoList.value!!.add(uri.toString())
                                //Log.i("TAG URI", uri.toString())
                            }
                        }.addOnFailureListener {
                                Log.i("TAG IMAGE", it.message.toString())
                            }
                    }
                }
            }

    }

    @SuppressLint("RestrictedApi")
    fun setImageBitmap(imageProxy: ImageProxy){


        if(updateImage){
            //Log.i("TAG IMAGE SET", "Setting")
            val byteArray = ImageUtil.yuvImageToJpegByteArray(imageProxy, null, 100)

            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            val matrix = Matrix().apply { postRotate(90f) }
            val flipped =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            _imageBitMap.value = flipped
        }
    }

    fun updateImageChangeState(){
        Log.i("TAG IMAGE SWITCH", "Change")
        updateImage = !updateImage
    }

}