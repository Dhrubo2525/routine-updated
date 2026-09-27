package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AlertToast
import com.example.ui.components.TopNotificationBar
import com.example.ui.screens.*
import com.example.ui.theme.RoutineScrapperTheme
import com.example.ui.viewmodel.AcademicViewModel
import com.example.ui.viewmodel.ActiveZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AcademicViewModel = viewModel()
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

            RoutineScrapperTheme(darkTheme = appSettings.isDarkMode) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeZone by viewModel.activeZone.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()

    // Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Back handling: If on any secondary zone, return to Dashboard
    BackHandler(enabled = activeZone != ActiveZone.DASHBOARD) {
        viewModel.setZone(ActiveZone.DASHBOARD)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        topBar = {
            TopNotificationBar(
                notifications = notifications,
                isDarkMode = appSettings.isDarkMode,
                isDesktopMode = appSettings.isDesktopMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onNotificationClick = { viewModel.onNotificationClicked(it) },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onClearAll = { viewModel.clearNotifications() }
            )
        },
        bottomBar = {
            if (!appSettings.isDesktopMode) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeZone == ActiveZone.STUDENT,
                        onClick = { viewModel.setZone(ActiveZone.STUDENT) },
                        icon = {
                            Icon(
                                if (activeZone == ActiveZone.STUDENT) Icons.Default.Schedule else Icons.Outlined.Schedule,
                                contentDescription = "Routine"
                            )
                        },
                        label = { Text("Routine", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_item_student")
                    )

                    NavigationBarItem(
                        selected = activeZone == ActiveZone.DASHBOARD,
                        onClick = { viewModel.setZone(ActiveZone.DASHBOARD) },
                        icon = {
                            Icon(
                                if (activeZone == ActiveZone.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = activeZone == ActiveZone.TEACHERS,
                        onClick = { viewModel.setZone(ActiveZone.TEACHERS) },
                        icon = {
                            Icon(
                                if (activeZone == ActiveZone.TEACHERS) Icons.Default.People else Icons.Outlined.People,
                                contentDescription = "Teachers"
                            )
                        },
                        label = { Text("Teachers", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_item_teachers")
                    )

                    NavigationBarItem(
                        selected = activeZone == ActiveZone.QUIZ,
                        onClick = { viewModel.setZone(ActiveZone.QUIZ) },
                        icon = {
                            Icon(
                                if (activeZone == ActiveZone.QUIZ) Icons.Default.Quiz else Icons.Outlined.Quiz,
                                contentDescription = "Quiz"
                            )
                        },
                        label = { Text("Quiz", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_item_quiz")
                    )

                    NavigationBarItem(
                        selected = activeZone == ActiveZone.CALENDAR,
                        onClick = { viewModel.setZone(ActiveZone.CALENDAR) },
                        icon = {
                            Icon(
                                if (activeZone == ActiveZone.CALENDAR) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Calendar"
                            )
                        },
                        label = { Text("Calendar", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_item_calendar")
                    )

                    NavigationBarItem(
                        selected = activeZone == ActiveZone.ADMIN,
                        onClick = { viewModel.setZone(ActiveZone.ADMIN) },
                        icon = {
                            Icon(
                                if (activeZone == ActiveZone.ADMIN) Icons.Default.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                                contentDescription = "Admin"
                            )
                        },
                        label = { Text("Admin", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_item_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Desktop Mode Side Navigation Rail (Section 44)
            if (appSettings.isDesktopMode) {
                NavigationRail(
                    modifier = Modifier.testTag("desktop_nav_rail")
                ) {
                    Spacer(Modifier.height(8.dp))
                    NavigationRailItem(
                        selected = activeZone == ActiveZone.STUDENT,
                        onClick = { viewModel.setZone(ActiveZone.STUDENT) },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Routine") },
                        label = { Text("Routine", fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = activeZone == ActiveZone.DASHBOARD,
                        onClick = { viewModel.setZone(ActiveZone.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = activeZone == ActiveZone.TEACHERS,
                        onClick = { viewModel.setZone(ActiveZone.TEACHERS) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Teachers") },
                        label = { Text("Teachers", fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = activeZone == ActiveZone.QUIZ,
                        onClick = { viewModel.setZone(ActiveZone.QUIZ) },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz") },
                        label = { Text("Quiz", fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = activeZone == ActiveZone.CALENDAR,
                        onClick = { viewModel.setZone(ActiveZone.CALENDAR) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                        label = { Text("Calendar", fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = activeZone == ActiveZone.ADMIN,
                        onClick = { viewModel.setZone(ActiveZone.ADMIN) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                        label = { Text("Admin", fontSize = 10.sp) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                // Zone Screen Content with animated transitions
                AnimatedContent(
                    targetState = activeZone,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "zone_transition"
                ) { zone ->
                    when (zone) {
                        ActiveZone.STUDENT -> StudentZoneScreen(viewModel = viewModel)
                        ActiveZone.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        ActiveZone.TEACHERS -> TeachersZoneScreen(viewModel = viewModel)
                        ActiveZone.QUIZ -> QuizZoneScreen(viewModel = viewModel)
                        ActiveZone.CALENDAR -> CalendarZoneScreen(viewModel = viewModel)
                        ActiveZone.ADMIN -> AdminZoneScreen(viewModel = viewModel)
                    }
                }

                // In-app Alert Toast floating on top
                AlertToast(
                    alertData = activeAlert,
                    onDismiss = { viewModel.dismissAlert() }
                )
            }
        }
    }
}
