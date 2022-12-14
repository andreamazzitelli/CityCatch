package com.example.citycatch.utils

import com.example.citycatch.R


sealed class BottomNavItem (var title:String, var icon:Int, var screen_route:String){

    object Map : BottomNavItem("Map", R.drawable.center_icon,"map")
    object Leaderboard: BottomNavItem("Leaderboard",R.drawable.leaderboard_icon,"leaderboard")
    object User: BottomNavItem("User",R.drawable.user_icon,"user")

}