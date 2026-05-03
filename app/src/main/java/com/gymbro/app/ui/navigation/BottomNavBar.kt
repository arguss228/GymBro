package com.gymbro.app.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Workouts.route,
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter,
        contentDescription = "Тренировки",
    ),
    BottomNavItem(
        route = Screen.Progress.route,
        selectedIcon = Icons.Filled.ShowChart,
        unselectedIcon = Icons.Outlined.ShowChart,
        contentDescription = "Прогресс",
    ),
    BottomNavItem(
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.LocalFireDepartment,
        unselectedIcon = Icons.Outlined.LocalFireDepartment,
        contentDescription = "Главная",
    ),
    BottomNavItem(
        route = Screen.BodyAnalysisTab.route,
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        contentDescription = "Анализ тела",
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Профиль",
    ),
)

// Routes where bottom bar should be hidden
val bottomBarHiddenRoutes = setOf(
    Screen.Splash.route,
    Screen.Enter1Rm.route,
    Screen.WorkoutSession.route.substringBefore("{"),
    "workout/",
)

@Composable
fun GymBroBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(72.dp),
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val isCenter = item.route == Screen.Home.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid backstack accumulation
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (isCenter) {
                        // Special center button
                        CenterNavIcon(
                            selected = selected,
                            icon = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.contentDescription,
                        )
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.contentDescription,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                },
                label = null, // No text labels
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isCenter) Color.Unspecified
                                        else MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = if (isCenter) Color.Transparent
                                     else MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun CenterNavIcon(
    selected: Boolean,
    icon: ImageVector,
    contentDescription: String,
) {
    val size by animateDpAsState(
        targetValue = if (selected) 56.dp else 48.dp,
        animationSpec = tween(200),
        label = "center_icon_size",
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}

fun shouldShowBottomBar(route: String?): Boolean {
    if (route == null) return false
    if (route == Screen.Splash.route) return false
    if (route == Screen.Enter1Rm.route) return false
    if (route.startsWith("workout/")) return false
    return true
}