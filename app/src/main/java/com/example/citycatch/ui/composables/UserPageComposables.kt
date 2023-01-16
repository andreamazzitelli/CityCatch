package com.example.citycatch.ui.composables

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.citycatch.EntryPointActivity
import com.example.citycatch.R
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.ui.theme.LightOrange
import com.example.citycatch.ui.theme.Orange
import com.example.citycatch.viewmodel.FirebaseViewModel

@Composable
fun UserPage(vm: FirebaseViewModel){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(vertical = 45.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        UpperBlock(vm = vm )
        Spacer(modifier = Modifier.height(15.dp))
        Divider(color = Orange, thickness = 2.dp)
        //CentralIcon()
        ImageBlock(vm)

    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        TopButton()
    }


}

@Composable
fun UpperBlock(vm: FirebaseViewModel){

    val imageNumber = vm.photoNumber.observeAsState()
    val points = vm.points.observeAsState()

    Row(
        modifier = Modifier
            .wrapContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){

        UserImageBlock()
        Spacer(modifier = Modifier.width(40.dp))
        ProfileStat(value = points.value.toString(), name =  "points")
        Spacer(modifier = Modifier.width(40.dp))
        ProfileStat(value = imageNumber.value.toString(), name =  "images")
    }
}

@Composable
fun UserImageBlock(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){

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
                    //imageUri.value=it.toString()
                    FirebaseRepository.addProfileToStorage(it)
                }
            }

            AsyncImage(
                model= "https://firebasestorage.googleapis.com/v0/b/citycatch.appspot.com/o/${FirebaseRepository.getUserUID()}%2Fprofile.jpg?alt=media&token=${FirebaseRepository.getUser()!!.getIdToken(false)}",
                //painter = painter,
                fallback = painter,
                placeholder = painter,
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
fun ImageBlock(vm: FirebaseViewModel){

    val urlList = vm.photoList.observeAsState()
    //Log.i("TAG IMAGE", urlList.value!!.isEmpty().toString())

    if(urlList.value!!.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.wrapContentSize()
        ) {
            itemsIndexed(urlList.value!!){ _, el ->
                Log.i("TAG INDEX", el)
                RowElement(link = el)
            }
        }
    }
    else{
        Spacer(modifier = Modifier.height(30.dp))
       Text(
           text = "No Images",
           style = TextStyle(
               fontSize = 40.sp,
               color = Color.LightGray
           )

       )
    }

    vm.getImages()
}

@Composable
fun RowElement(link: String){

    Row {
        Card(
            backgroundColor = Color(0x80E4E4E4),
            modifier = Modifier
                .padding(4.dp)
                .background(LightOrange)
                .fillMaxSize()
        ) {
            AsyncImage(
                modifier = Modifier.height(600.dp),
                model = link,
                contentDescription = "",
                //painter = painterResource(id = R.drawable.logo),
            )
        }
    }
}