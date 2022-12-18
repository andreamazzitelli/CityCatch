package com.example.citycatch.ui.composables

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.citycatch.CameraActivity
import com.example.citycatch.EntryPointActivity
import com.example.citycatch.MapsActivity
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.ui.theme.Orange
import com.example.citycatch.utils.BottomNavItem
import com.example.citycatch.viewmodel.FirebaseViewModel
import com.example.citycatch.viewmodel.MapViewModel

@Composable
fun MainScreen(vm: MapViewModel, fm: FirebaseViewModel){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) }
    ) {
        Box (
            modifier = Modifier.padding(it)
        ){
            NavigationGraph(
                navController = navController,
                vm = vm,
                fm = fm
            )


        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, vm: MapViewModel, fm: FirebaseViewModel) {
    NavHost(navController, startDestination = BottomNavItem.Map.screen_route) {
        composable(BottomNavItem.Leaderboard.screen_route) {
            Left()
        }
        composable(BottomNavItem.Map.screen_route) {
            GoogleMapCluster(vm = vm)
            //Center()
        }
        composable(BottomNavItem.User.screen_route) {
            UserPage(fm)
            //Right()
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController){

    val items = listOf(
        BottomNavItem.Leaderboard,
        BottomNavItem.Map,
        BottomNavItem.User
    )

    androidx.compose.material.BottomNavigation(
        modifier = Modifier.wrapContentSize(),
        backgroundColor = Orange,
        contentColor = Color.Black
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Black.copy(0.4f),
                alwaysShowLabel = true,
                selected = currentRoute == item.screen_route,
                onClick = {
                    navController.navigate(item.screen_route) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun Left(){
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Red))
}
@Composable
fun Right(){
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Blue)){
        //for testing
        Column() {
            Text(text = FirebaseRepository.getUser()!!.email.toString())
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    FirebaseRepository.userSignOut()
                    val intent = Intent(context, EntryPointActivity::class.java)
                    activity!!.startActivity(intent)
                    activity!!.finish()
                }) {
                Text(text = "LogOut")
            }
            /*
            Button(onClick = {
                val intent = Intent(context, CameraActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(text = "Go to Camera")
            }

             */
        }
    }
}
