package com.example.citycatch.data

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.ByteArrayOutputStream


object FirebaseRepository {

    private var firebaseAuth = FirebaseAuth.getInstance()

    private var firebaseStorage = Firebase.storage
    private var storageReference = firebaseStorage.reference

    fun getUser() = firebaseAuth.currentUser
    fun getUserUID() = firebaseAuth.currentUser!!.uid
    fun getAuthInstance() = firebaseAuth
    fun userSignOut() = firebaseAuth.signOut()

    @SuppressLint("RestrictedApi")
    fun addToStorage(user: String, markerName: String, image: ImageProxy): Boolean{

        val imagesRef = storageReference.child("$user/$markerName.jpg")

        val byteArray = ImageUtil.yuvImageToJpegByteArray(
            image,
            Rect(0, 0, image.width, image.height),
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

}