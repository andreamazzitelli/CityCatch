package com.example.citycatch.data

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.example.citycatch.utils.APIs
import com.example.citycatch.utils.WebAPIs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream


object FirebaseRepository {

    private var firebaseAuth = FirebaseAuth.getInstance()

    private var firebaseStorage = Firebase.storage
    private var storageReference = firebaseStorage.reference

    private val realTimeDB = Firebase.database("https://citycatch-default-rtdb.firebaseio.com/")
    private val referenceRealDB = realTimeDB.getReference("scores")

    fun getUser() = firebaseAuth.currentUser
    fun getUserUID() = firebaseAuth.currentUser!!.uid
    fun getAuthInstance() = firebaseAuth
    fun userSignOut() = firebaseAuth.signOut()

    fun getStorageReference() = storageReference
    fun getStorage() = firebaseStorage

    fun getReferenceRDB() = referenceRealDB

    private var webAPIs: APIs = WebAPIs().retrofit.create(APIs::class.java)

    @SuppressLint("RestrictedApi")
    fun addToStorage(user: String, markerName: String, image: ImageProxy): Boolean{

        val imagesRef = storageReference.child("$user/$markerName.jpg")

        val byteArray = ImageUtil.yuvImageToJpegByteArray(
            image,
            null, //Rect(0, 0, image.width, image.height),
            100
        )

        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val matrix = Matrix().apply { postRotate(90f) }
        val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        val stream = ByteArrayOutputStream()
        flipped.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageBytes = stream.toByteArray()


        val metadata = storageMetadata {
            contentType = "image/jpg"
            setCustomMetadata("Location Name", "")
            setCustomMetadata("Latitude", "")
            setCustomMetadata("Longitude", "")
            setCustomMetadata("time", "")
            setCustomMetadata("user", "")

        }

        val uploadTask = imagesRef.putBytes(imageBytes, metadata)

        if(!uploadTask.isSuccessful)return false
        return true
        /*
    .addOnFailureListener{
        Log.i("TAG UPLOAD ERR", "$it")
    }
    .addOnSuccessListener {
        Log.i("TAG UPLOAD SUC", "SUCCESS?")
    }

         */

    }
    fun addProfileToStorage(imageUri: Uri){
        val imageRef = storageReference.child("${firebaseAuth.currentUser!!.uid}/profile.jpg")
        //val file = File(imageUri.path)
        imageRef.putFile(imageUri)
    }
    fun addDefaultToStorage(byteArray: ByteArray){
        val imageRef = storageReference.child("${firebaseAuth.currentUser!!.uid}/profile.jpg")
        imageRef.putBytes(byteArray)
    }
    fun addUserToDB(){

        try {
            runBlocking {
                launch {

                    Log.i("TAG USER", firebaseAuth.currentUser!!.email.toString())
                    Log.i("TAG USER", firebaseAuth.currentUser!!.uid)

                    webAPIs.addUser(firebaseAuth.currentUser!!.email.toString(), firebaseAuth.currentUser!!.uid)
                    Log.i("TAG USER", "Success")
                }
            }

        }
        catch (e: Exception){
            Log.i("TAG USER", "Failed")
        }
    }


}

