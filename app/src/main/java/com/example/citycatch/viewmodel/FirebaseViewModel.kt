package com.example.citycatch.viewmodel

import android.util.Log
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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

    init {
        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {

        orderedScores.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("TAG", "Test")
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

                _userScores.value = temp

                Log.d("TAG", "Value is: " + value)
                Log.d("TAG", temp.toString())
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

                _photoNumber.value = list.items.size - 1

                list.items.forEach {
                    if (!it.toString().contains("profile.jpg")) {
                        val ref =
                            FirebaseRepository.getStorage().getReferenceFromUrl(it.toString())
                        ref.downloadUrl.addOnSuccessListener { uri ->

                            if (!_photoList.value!!.contains(uri.toString())) {
                                _photoList.value!!.add(uri.toString())
                                Log.i("TAG URI", uri.toString())
                            }
                        }
                    }
                }
            }
    }

}