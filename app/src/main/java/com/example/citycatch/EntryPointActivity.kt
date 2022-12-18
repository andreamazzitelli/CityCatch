package com.example.citycatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.ui.composables.LogInNavigation
import com.example.citycatch.ui.composables.SplashScreen

class EntryPointActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LogInNavigation()
        }
    }
}


