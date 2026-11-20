package com.example.hakot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hakot.data.HakotRepository
import com.example.hakot.ui.components.HakotBottomBar
import com.example.hakot.ui.components.HakotTab
import com.example.hakot.ui.screens.AdminScreen
import com.example.hakot.ui.screens.HakotDrawerContent
import com.example.hakot.ui.screens.HomeScreen
import com.example.hakot.ui.screens.ScheduleScreen
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.HakotTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HakotTheme {
                HakotMainApp()
            }
        }
    }
}

@Composable
fun HakotMainApp() {
    // Save tab, search, and admin state across rotation
    var selectedTab by   rememberSaveable { mutableStateOf(HakotTab.MAP) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isAdminMode by rememberSaveable { mutableStateOf<Boolean>(HakotRepository.isAuthenticated) }
    var triggerReportDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle Android Back button to close drawer when open before navigating away
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, // Not draggable when closed; opens exclusively via hamburger button
        drawerContent = {
            HakotDrawerContent(
                isAdminMode = isAdminMode,
                onToggleAdminMode = { enabled ->
                    isAdminMode = enabled
                    scope.launch { drawerState.close() }
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 2.dp) // Prevents map dragging from triggering edge-swipe-to-open
        ) {
            Scaffold(
                bottomBar = {
                    if (!isAdminMode) {
                        HakotBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                when (tab) {
                                    HakotTab.MORE -> {
                                        scope.launch { drawerState.open() }
                                    }
                                    HakotTab.REPORT -> {
                                        selectedTab = HakotTab.MAP
                                        triggerReportDialog = true
                                    }
                                    else -> {
                                        selectedTab = tab
                                    }
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(DarkBackground)
                ) {
                    if (isAdminMode) {
                        AdminScreen(
                            searchQuery = searchQuery,
                            onExitAdminMode = { isAdminMode = false }
                        )
                    } else {
                        val onMenuClick = {
                            scope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                            Unit
                        }

                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { if (targetState.ordinal > initialState.ordinal) it else -it })
                                    .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { if (targetState.ordinal > initialState.ordinal) -it else it })
                            },
                            label = "TabTransitionAnimation"
                        ) { tab ->
                            when (tab) {
                                HakotTab.MAP -> HomeScreen(
                                    searchQuery = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onMenuClick = onMenuClick,
                                    isAdminMode = isAdminMode,
                                    initialShowReportDialog = triggerReportDialog,
                                    onReportDialogDismiss = { triggerReportDialog = false }
                                )
                                HakotTab.SCHEDULE -> ScheduleScreen(
                                    searchQuery = searchQuery,
                                    modifier = Modifier.fillMaxSize()
                                )
                                else -> HomeScreen(
                                    searchQuery = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onMenuClick = onMenuClick,
                                    isAdminMode = isAdminMode,
                                    initialShowReportDialog = triggerReportDialog,
                                    onReportDialogDismiss = { triggerReportDialog = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
