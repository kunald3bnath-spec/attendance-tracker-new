package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AttendanceDatabase
import com.example.data.AttendanceRepository
import com.example.data.ThemePreferences
import com.example.ui.AttendanceViewModel
import com.example.ui.AttendanceViewModelFactory
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MarkAttendanceScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core data and preferences bootstrapping
        val database = AttendanceDatabase.getDatabase(applicationContext)
        val repository = AttendanceRepository(database)
        val themePreferences = ThemePreferences(applicationContext)

        val viewModelFactory = AttendanceViewModelFactory(application, repository, themePreferences)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AttendanceViewModel::class.java]

        setContent {
            val isDarkThemePref by viewModel.isDarkTheme.collectAsState()
            val useDarkTheme = isDarkThemePref ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDarkTheme) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

data class NavigationBarItemInfo(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppContainer(
    viewModel: AttendanceViewModel
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            viewModel = viewModel,
            onDismiss = { viewModel.completeOnboarding() }
        )
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val navigationItems = listOf(
            NavigationBarItemInfo(
                title = "Projects",
                route = "projects",
                selectedIcon = Icons.Filled.Folder,
                unselectedIcon = Icons.Outlined.Folder,
                testTag = "tab_projects"
            ),
            NavigationBarItemInfo(
                title = "Mark",
                route = "mark_attendance",
                selectedIcon = Icons.Filled.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                testTag = "tab_mark"
            ),
            NavigationBarItemInfo(
                title = "History",
                route = "history",
                selectedIcon = Icons.Filled.History,
                unselectedIcon = Icons.Outlined.History,
                testTag = "tab_history"
            ),
            NavigationBarItemInfo(
                title = "Overview",
                route = "overview",
                selectedIcon = Icons.Filled.Assessment,
                unselectedIcon = Icons.Outlined.Assessment,
                testTag = "tab_overview"
            )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ) {
                    navigationItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(text = item.title, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "projects",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("projects") {
                    ProjectsScreen(viewModel = viewModel)
                }
                composable("mark_attendance") {
                    MarkAttendanceScreen(viewModel = viewModel)
                }
                composable("history") {
                    HistoryScreen(viewModel = viewModel)
                }
                composable("overview") {
                    OverviewScreen(viewModel = viewModel)
                }
            }
        }
    }
}
