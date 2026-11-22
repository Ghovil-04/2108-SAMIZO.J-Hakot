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
import com.example.hakot.ui.screens.ReportScreen
import com.example.hakot.ui.screens.ScheduleScreen
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.hakot.ui.screens.MunicipalitySelectionModal
import com.example.hakot.ui.screens.NotificationScreen
import com.example.hakot.ui.theme.DarkBackground
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.hakot.ui.theme.HakotTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
    val context = LocalContext.current
    remember(context) {
        HakotRepository.initSavedMunicipality(context)
        true
    }
    var selectedTab by rememberSaveable { mutableStateOf(HakotTab.MAP) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isAdminMode by rememberSaveable { mutableStateOf<Boolean>(HakotRepository.isAuthenticated) }
    var showMunicipalityModal by rememberSaveable {
        mutableStateOf(HakotRepository.getSavedMunicipalityId(context) == null)
    }
    var showNotificationScreen by rememberSaveable { mutableStateOf(false) }

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
                onChangeMunicipality = {
                    scope.launch { drawerState.close() }
                    showMunicipalityModal = true
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
                                    onNotificationClick = { showNotificationScreen = true },
                                    isAdminMode = isAdminMode
                                )
                                HakotTab.SCHEDULE -> ScheduleScreen(
                                    searchQuery = searchQuery,
                                    modifier = Modifier.fillMaxSize()
                                )
                                HakotTab.REPORT -> ReportScreen(
                                    searchQuery = searchQuery,
                                    modifier = Modifier.fillMaxSize()
                                )
                                else -> HomeScreen(
                                    searchQuery = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onMenuClick = onMenuClick,
                                    isAdminMode = isAdminMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMunicipalityModal) {
        MunicipalitySelectionModal(
            onDismiss = { showMunicipalityModal = false },
            onMunicipalitySelected = { id, name, lat, lng ->
                HakotRepository.saveSelectedMunicipality(context, id, name, lat, lng)
                Toast.makeText(context, "Active Municipality set to $name", Toast.LENGTH_SHORT).show()
                showMunicipalityModal = false
            }
        )
    }

    if (showNotificationScreen) {
        NotificationScreen(
            onDismiss = { showNotificationScreen = false }
        )
    }
}
