package com.example.citycatch.ui.composables

import android.content.Intent
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.citycatch.MapsActivity
import com.example.citycatch.R
import com.example.citycatch.data.FirebaseRepository
import kotlinx.coroutines.delay

@Composable
fun LogInNavigation(){

    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = "splash_screen"
    ) {

        composable("splash_screen") {
            SplashScreen(navController = navController)
        }

        composable("log_in") {
            LoginPage(navController = navController)
        }

        composable("sign_up"){
            RegistrationPage(navController = navController)
        }

    }

}



@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            // tween Animation
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(4f).getInterpolation(it)
                })
        )
        // Customize the delay time
        Log.i("TAG DELAY", "Before")
        delay(2000L)
        Log.i("TAG DELAY", "After")

        if(FirebaseRepository.getUser() == null){
            navController.navigate("log_in")
        }
        else{
            //intent to map
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }

    }

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()) {

        Image(painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value))
    }
}

@Composable
fun LoginPage(navController: NavHostController) {

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        ClickableText(
            text = AnnotatedString("Sign up here"),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            onClick = {
                      navController.navigate("sign_up")
            },
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily.Default,
                textDecoration = TextDecoration.Underline
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {

        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        val colorProperties = TextFieldDefaults.outlinedTextFieldColors(
            textColor =  LocalContentColor.current.copy(LocalContentAlpha.current),
            backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity),
            cursorColor = Color(255, 153, 0),
            focusedBorderColor = Color(255, 153, 0),
            focusedLabelColor = Color(255, 153, 0),
        )

        TextField(
            label = { Text(text = "Username") },
            value = username.value,
            colors = colorProperties,
            onValueChange = { username.value = it })


        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            label = { Text(text = "Password") },
            value = password.value,
            colors = colorProperties,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            },
            onValueChange = { password.value = it })


        Spacer(modifier = Modifier.height(50.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    val email = username.value.text
                    val passwordP = password.value.text

                    if (email.isNotEmpty() && passwordP.isNotEmpty()){
                        FirebaseRepository.getAuthInstance().signInWithEmailAndPassword(email, passwordP)
                            .addOnSuccessListener {
                                Log.i("TAG LOGIN", "SUCCESS")
                                Log.i("TAG LOGIN", FirebaseRepository.getUserUID())
                                Log.i("TAG LOGIN", FirebaseRepository.getUser()!!.email.toString())
                                val intent = Intent(context, MapsActivity::class.java)
                                context.startActivity(intent)
                            }
                            .addOnFailureListener {
                                Log.i("TAG LOGIN", it.message.toString())
                            }

                    }else{
                        Log.i("TAG LOGIN", "Empty Fields Are not Allowed !!")
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(255, 153, 0))
            ) {
                Text(text = "Login")
            }
        }
    }


    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "logo",
        //modifier = Modifier.background(Color.Red)
    )

}

@Composable
fun RegistrationPage(navController: NavHostController){

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {

        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val confirmPassword = remember { mutableStateOf(TextFieldValue())}


        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

        val colorProperties = TextFieldDefaults.outlinedTextFieldColors(
            textColor =  LocalContentColor.current.copy(LocalContentAlpha.current),
            backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity),
            cursorColor = Color(255, 153, 0),
            focusedBorderColor = Color(255, 153, 0),
            focusedLabelColor = Color(255, 153, 0),
        )


        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            colors = colorProperties,
            label = { Text(text = "Username") },
            value = username.value,
            onValueChange = { username.value = it })


        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            colors = colorProperties,
            label = { Text(text = "Password") },
            value = password.value,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            },
            onValueChange = { password.value = it })

        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            colors = colorProperties,
            label = { Text(text = "Confirm Password") },
            value = confirmPassword.value,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (confirmPasswordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (confirmPasswordVisible) "Hide password" else "Show password"

                IconButton(onClick = {confirmPasswordVisible = !confirmPasswordVisible}){
                    Icon(imageVector  = image, description)
                }
            },
            onValueChange = { confirmPassword.value = it })


        Spacer(modifier = Modifier.height(30.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {

                    val email = username.value.text
                    val passwordL = password.value.text
                    val confirmPasswordL = confirmPassword.value.text

                    if(email.isNotEmpty() && passwordL.isNotEmpty() && confirmPasswordL.isNotEmpty()){
                        if(passwordL == confirmPasswordL){
                            FirebaseRepository.getAuthInstance().createUserWithEmailAndPassword(email, passwordL)
                                .addOnSuccessListener {
                                    Log.i("TAG REGISTER", "Registered Successful")
                                    Log.i("TAG REGISTER", FirebaseRepository.getUser()!!.email.toString())

                                    val intent = Intent(context, MapsActivity::class.java)
                                    context.startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Log.i("TAG REGISTER", it.message.toString())
                                }
                        }

                    }else{
                        Log.i("TAG REGISTER", "Empty Fields")
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(255, 153, 0))
            ) {
                Text(text = "Register")
            }
        }
    }


    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "logo",
        //modifier = Modifier.background(Color.Red)
    )
}

