package com.example.spendwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spendwise.navigation.Screen
import com.example.spendwise.navigation.SpendWiseNavHost
import com.example.spendwise.ui.theme.SpendWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendWiseTheme {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route

                // Các tab hiển thị BottomBar
                val bottomNavItems = listOf(
                    BottomNavItem("Tổng quan", Screen.Home.route,    Icons.Default.Home),
                    BottomNavItem("Lịch sử",   Screen.History.route, Icons.Default.List),
                    BottomNavItem("Thống kê",  Screen.Stats.route,   Icons.Default.BarChart),
                    BottomNavItem("Ngân sách", Screen.Budget.route,  Icons.Default.Wallet),
                )

                // Ẩn BottomBar trên màn thêm/sửa
                val showBottomBar = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.History.route,
                    Screen.Stats.route,
                    Screen.Budget.route
                )

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentRoute == item.route,
                                        onClick  = {
                                            navController.navigate(item.route) {
                                                // Tránh tạo nhiều bản sao trong back stack
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState    = true
                                            }
                                        },
                                        icon  = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    SpendWiseNavHost(
                        navController = navController,
                        modifier      = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
