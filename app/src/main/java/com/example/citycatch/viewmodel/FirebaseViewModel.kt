package com.example.citycatch.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.citycatch.data.FirebaseRepository

class FirebaseViewModel : ViewModel() {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                FirebaseViewModel()
            }
        }
    }

    private val _photoList : MutableLiveData<MutableList<String>> = MutableLiveData<MutableList<String>>(mutableListOf())
    val photoList: LiveData<MutableList<String>> = _photoList

    private val _photoNumber : MutableLiveData<Int> = MutableLiveData<Int>(0)
    val photoNumber: LiveData<Int> = _photoNumber

    private val _points : MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    val points: LiveData<Double> = _points

    fun getImages(){

        if (_photoList.value == null){
            _photoList.value = mutableListOf()
        }

        FirebaseRepository.getStorageReference().child(FirebaseRepository.getUserUID())
            .listAll().addOnSuccessListener {list ->

                _photoNumber.value = list.items.size - 1

                    list.items.forEach {
                    if(!it.toString().contains("profile.jpg")){
                        val ref = FirebaseRepository.getStorage().getReferenceFromUrl(it.toString())
                        ref.downloadUrl.addOnSuccessListener { uri ->

                            if(!_photoList.value!!.contains(uri.toString())) {
                                _photoList.value!!.add(uri.toString())
                                Log.i("TAG URI", uri.toString())
                            }
                        }
                    }
                }
            }
    }



}