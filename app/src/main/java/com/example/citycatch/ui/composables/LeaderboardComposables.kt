package com.example.citycatch.ui.composables


import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citycatch.R
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.data.model.UserScore
import com.example.citycatch.ui.theme.LightOrange
import com.example.citycatch.viewmodel.FirebaseViewModel

@Composable
fun Leaderboard(vm: FirebaseViewModel) {

    val userScores = vm.userScores.observeAsState()
    val listState = rememberLazyListState()

    val config = LocalConfiguration.current
    val maxWidth = config.screenWidthDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightOrange),
        verticalArrangement = Arrangement.Center,

    ){
        Row(
            modifier = Modifier
                .padding(top = 3.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Image(
                modifier = Modifier.size(70.dp, 70.dp),
                painter = painterResource(id = R.drawable.score),
                contentDescription = ""
            )


            
            Text(
                text = "LEADERBOARD",
                modifier = Modifier.padding(top = 20.dp),
                style = TextStyle(
                    fontSize = 35.sp,
                    fontWeight = FontWeight.W900,
                    //textAlign = TextAlign.Center
                )
            )
        }

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                //.border(3.dp, Color.Black)
        ){
            var user = 0
            var gotUser = remember {
                mutableStateOf(false)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                content = {

                    itemsIndexed(userScores.value!!) { i, el ->
                        //Log.i("TAG COL", "Here")

                        if (el.mail == FirebaseRepository.getUser()!!.email) {
                            user = i
                            gotUser.value = true
                        }else{
                            gotUser.value = false
                        }
                        LeaderboardEntry(i, el, maxWidth, gotUser.value)
                    }
                }
            )

            LaunchedEffect(
                key1 = gotUser.value,
                block = {
                    //Log.i("TAG SCROLL", "Scrolled")
                    //Log.i("TAG SCROLL", "$user")
                    listState.animateScrollToItem(index = user)
                }
            )

        }
    }
}

@Composable
fun LeaderboardEntry(index: Int, userScoreData: UserScore, width: Dp, isUser: Boolean) {

    val medal = when(index){
        0 -> R.drawable.gold
        1 -> R.drawable.silver
        2 -> R.drawable.bronze
        else -> R.drawable.user
    }

    val textStyle = if (isUser) {
        FontWeight.ExtraBold
    }else{
        FontWeight.Normal
    }

    Card(
        modifier = Modifier
            .padding(start = width / 20, top = 15.dp)
            .width(width = width * 18 / 20)
            .wrapContentWidth(),
        //elevation = 10.dp,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(3.dp, Color(0xFFFF9B07))

    ){
        Row(
            modifier = Modifier
                .padding(
                    top = 5.dp,
                    bottom = 5.dp,
                    start = 5.dp,
                    end = 5.dp
                )
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {


            if(index <= 2) {
                Image(
                    modifier = Modifier.size(60.dp, 60.dp),
                    painter = painterResource(id = medal),
                    contentDescription = ""
                )
            }
            else{
                Text(
                    modifier = Modifier.padding(start = 15.dp),
                    text = "${index +1}. ",
                    fontSize = 30.sp
                )
            }

            Text(
                modifier = Modifier,
                text = userScoreData.mail,
                fontSize = 30.sp,
                fontWeight = textStyle
            )
            Spacer(modifier = Modifier.weight(2f))
            Text(
                modifier =  Modifier.padding(end = 30.dp),
                text = "${userScoreData.score}p",
                fontSize = 25.sp,
                color = Color.LightGray
            )

        }
    }
}


