package com.example.boardshelf.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "collection") {
        composable("collection") {
            CollectionScreen(onOpenGame = { nav.navigate("game/$it") })
        }
        composable(
            "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) {
            GameDetailScreen(onBack = { nav.popBackStack() })
        }
    }
}
