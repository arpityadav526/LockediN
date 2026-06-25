package com.lockedin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lockedin.feature.aichat.AiChatScreen
import com.lockedin.feature.files.FilesScreen
import com.lockedin.feature.files.viewer.ImageViewerScreen
import com.lockedin.feature.files.viewer.PdfViewerScreen
import com.lockedin.feature.home.HomeScreen
import com.lockedin.feature.lock.LockManager
import com.lockedin.feature.settings.SettingsScreen
import com.lockedin.feature.tools.calculator.CalculatorScreen
import com.lockedin.feature.tools.converter.ConverterScreen
import com.lockedin.feature.tools.dictionary.DictionaryScreen
import com.lockedin.feature.tools.formulas.FormulaSheetScreen
import com.lockedin.feature.tools.notes.NotesScreen
import com.lockedin.feature.tools.timer.TimerScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.FILES, "Files", Icons.Filled.Folder, Icons.Outlined.Folder),
    BottomNavItem(Routes.AI_CHAT, "AI Chat", Icons.Filled.Chat, Icons.Outlined.Chat),
    BottomNavItem(Routes.NOTES, "Notes", Icons.Filled.EditNote, Icons.Outlined.EditNote),
)

// Routes that show bottom nav bar
private val bottomNavRoutes = setOf(
    Routes.HOME, Routes.FILES, Routes.AI_CHAT, Routes.NOTES
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    lockManager: LockManager,
    onUnlock: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    navController = navController,
                    onUnlock = onUnlock
                )
            }

            composable(Routes.FILES) {
                FilesScreen(navController = navController)
            }

            composable(
                route = Routes.PDF_VIEWER,
                arguments = listOf(navArgument("fileId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fileId = backStackEntry.arguments?.getLong("fileId") ?: return@composable
                PdfViewerScreen(fileId = fileId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.IMAGE_VIEWER,
                arguments = listOf(navArgument("fileId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fileId = backStackEntry.arguments?.getLong("fileId") ?: return@composable
                ImageViewerScreen(fileId = fileId, onBack = { navController.popBackStack() })
            }

            composable(Routes.AI_CHAT) {
                AiChatScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.CALCULATOR) {
                CalculatorScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.DICTIONARY) {
                DictionaryScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.TIMER) {
                TimerScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.NOTES) {
                NotesScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.CONVERTER) {
                ConverterScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.FORMULAS) {
                FormulaSheetScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    lockManager = lockManager
                )
            }
        }
    }
}
