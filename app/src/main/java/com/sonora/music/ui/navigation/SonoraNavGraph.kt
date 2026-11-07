package com.sonora.music.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.ui.components.MiniPlayer
import com.sonora.music.ui.screens.downloads.DownloadsScreen
import com.sonora.music.ui.screens.home.HomeScreen
import com.sonora.music.ui.screens.library.LibraryScreen
import com.sonora.music.ui.screens.player.PlayerScreen
import com.sonora.music.ui.screens.search.SearchScreen
import com.sonora.music.ui.screens.storage.StorageDashboardScreen
import com.sonora.music.ui.theme.SonoraBlack
import com.sonora.music.ui.theme.SonoraCharcoal
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary

@Composable
fun SonoraNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val playbackManager = remember { ServiceLocator.providePlaybackManager(context) }

    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()

    var isPlayerExpanded by remember { mutableStateOf(false) }

    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = SonoraBlack,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Persistent Mini Player docked above BottomNav
                if (currentSong != null && !isPlayerExpanded) {
                    MiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        progress = progress,
                        onPlayPause = { playbackManager.playPause() },
                        onNext = { playbackManager.skipToNext() },
                        onExpand = { isPlayerExpanded = true }
                    )
                }

                NavigationBar(
                    containerColor = SonoraCharcoal,
                    contentColor = SonoraTextPrimary,
                    tonalElevation = 8.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SonoraElectricBlue,
                                selectedTextColor = SonoraElectricBlue,
                                unselectedIconColor = SonoraTextMuted,
                                unselectedTextColor = SonoraTextMuted,
                                indicatorColor = SonoraPurple.copy(alpha = 0.25f)
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToStorage = { navController.navigate(Screen.Storage.route) },
                        onNavigateToEqualizer = { isPlayerExpanded = true },
                        currentPlayingSongId = currentSong?.id
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        currentPlayingSongId = currentSong?.id
                    )
                }
                composable(Screen.Library.route) {
                    LibraryScreen(
                        currentPlayingSongId = currentSong?.id
                    )
                }
                composable(Screen.Downloads.route) {
                    DownloadsScreen(
                        currentPlayingSongId = currentSong?.id
                    )
                }
                composable(Screen.Storage.route) {
                    StorageDashboardScreen()
                }
            }

            // Full Player Screen Overlay Animated Transition
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerScreen(
                    onDismiss = { isPlayerExpanded = false }
                )
            }
        }
    }
}

