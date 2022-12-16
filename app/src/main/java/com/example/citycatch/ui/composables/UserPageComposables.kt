package com.example.citycatch.ui.composables

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.citycatch.EntryPointActivity
import com.example.citycatch.R
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.ui.theme.Orange

@Composable
fun UserPage(){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(vertical = 45.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        UpperBlock()
        Spacer(modifier = Modifier.height(15.dp))
        Divider(color = Orange, thickness = 2.dp)
        //CentralIcon()
        ImageBlock()

    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        TopButton()
    }


}

@Composable
fun UpperBlock(){

    val imageUri = rememberSaveable {
            mutableStateOf("")
        }

    val painter =
        rememberImagePainter(
            if (imageUri.value.isEmpty()){
                R.drawable.user
            }else{
                imageUri.value
            }
        )

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ){ uri: Uri? ->
            uri?.let {
                imageUri.value=it.toString()
                FirebaseRepository.addProfileToStorage(it)
            }
         }

    Row(
        modifier = Modifier
            .wrapContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){

        UserImageBlock(
            painter = painter,
            launcher = launcher
        )
        Spacer(modifier = Modifier.width(40.dp))
        ProfileStat(value = "600", name =  "points")
        Spacer(modifier = Modifier.width(40.dp))
        ProfileStat(value = "300", name =  "images")
    }
}

@Composable
fun UserImageBlock(
    painter: Painter,
    launcher: ManagedActivityResultLauncher<String, Uri?>
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
            Image(
                painter = painter,
                contentDescription = "",
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
                    .size(100.dp)
                    .clickable { launcher.launch("image/*") }
            )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "username")
    }
}

@Composable
fun ProfileStat(value: String, name: String){

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text= value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text= name
        )
    }

}

@Composable
fun TopButton(){

    val context = LocalContext.current
    val activity = LocalContext.current as Activity

    Row (
        modifier = Modifier
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.End
    ){
        
        Button(
            onClick = {
                FirebaseRepository.userSignOut()
                val intent = Intent(context, EntryPointActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            },
            border = BorderStroke(2.dp, Orange),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text(text="LogOut", color=Color.Black)
        }

    }

}


@Composable
fun ImageBlock(){

        LazyColumn(
            modifier = Modifier.wrapContentSize()
        ) {
            itemsIndexed((1..20).map { it.toString() }) { _, row ->
                    RowElement()
                }
            }
        }

@Composable
fun RowElement(){
    Row {
        Card(
            backgroundColor = Color.LightGray,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = ""
            )
        }
    }
}