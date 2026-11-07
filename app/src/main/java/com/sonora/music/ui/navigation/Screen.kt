package com.sonora.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Search : Screen("search", "Search", Icons.Rounded.Search)
    object Library : Screen("library", "Library", Icons.Rounded.LibraryMusic)
    object Downloads : Screen("downloads", "Downloads", Icons.Rounded.CloudDownload)
    object Storage : Screen("storage", "Storage", Icons.Rounded.PieChart)
    object Player : Screen("player", "Now Playing", Icons.Rounded.Home)

    companion object {
        val bottomNavItems = listOf(Home, Search, Library, Downloads, Storage)
    }
}

