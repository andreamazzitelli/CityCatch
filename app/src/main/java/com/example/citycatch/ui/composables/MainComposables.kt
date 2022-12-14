package com.example.citycatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.citycatch.data.FirebaseRepository
import com.example.citycatch.ui.theme.Orange
import com.example.citycatch.utils.BottomNavItem
import com.example.citycatch.viewmodel.MapViewModel

@Composable
fun MainScreen(vm: MapViewModel){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) }
    ) {
        Box (
            modifier = Modifier.padding(it)
        ){
            NavigationGraph(
                navController = navController,
                vm = vm
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, vm: MapViewModel) {
    NavHost(navController, startDestination = BottomNavItem.Map.screen_route) {
        composable(BottomNavItem.Leaderboard.screen_route) {
            Left()
        }
        composable(BottomNavItem.Map.screen_route) {
            GoogleMapCluster(vm = vm)
            //Center()
        }
        composable(BottomNavItem.User.screen_route) {
            Right()
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
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Blue)){
        //for testing
        Button(
            onClick = {
                FirebaseRepository.userSignOut()
            }) {
            Text(text = "LogOut")
        }

    }
}
@Composable
fun Center(){
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Green))
}