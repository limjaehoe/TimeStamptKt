package com.androidkotlin.timestampkt.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.androidkotlin.timestampkt.data.preferences.SettingsManager
import com.androidkotlin.timestampkt.presentation.main.MainScreen
import com.androidkotlin.timestampkt.presentation.main.MainViewModel
import com.androidkotlin.timestampkt.presentation.settings.SettingsScreen

// 네비게이션 경로를 상수로 정의
sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "출근 기록")
    object Calendar : Screen("calendar", Icons.Default.CalendarMonth, "캘린더")
    object Leave : Screen("leave", Icons.Default.EventNote, "연차 관리")
    object Settings : Screen("settings", Icons.Default.Settings, "설정")
}

// 모든 화면 목록
val items = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Leave,
    Screen.Settings
)

@Composable
fun TimeStampNavigation(
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // 네비게이션 중복 방지 및 최적화
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // 같은 항목 재선택 시 중복 스택 방지
                                launchSingleTop = true
                                // 탭 간 상태 유지
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                MainScreen(
                    viewModel = viewModel,
                    settingsManager = settingsManager,
                    // 더 이상 설정 화면으로 이동하는 콜백이 필요 없음
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Calendar.route) {
                // 캘린더 화면 (임시 텍스트)
                Text("캘린더 화면 - 개발 예정")
            }
            composable(Screen.Leave.route) {
                // 연차 관리 화면 (임시 텍스트)
                Text("연차 관리 화면 - 개발 예정")
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    settingsManager = settingsManager,
                    // 뒤로가기 버튼 동작 변경
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}