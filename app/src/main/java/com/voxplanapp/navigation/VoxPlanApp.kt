package com.voxplanapp.navigation

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.ui.constants.TertiaryBorderColor
import com.voxplanapp.ui.constants.TopAppBarBgColor
import java.time.LocalDate

/**
 *  VoxPlanApp overall nav controller function.  Central navigation control for screen switching.
 */

@Composable
fun VoxPlanApp(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            when {
                route.startsWith(VoxPlanScreen.Main.route) -> navigationViewModel.setSelectedItemIndex(0)
                route.startsWith(VoxPlanScreen.DaySchedule.route) -> navigationViewModel.setSelectedItemIndex(1)
            }
        }
    }


    /*
        navBackStackEntry?.let { entry ->

            // debug loggin
            val route = entry.destination.route
            Log.d("Navigation", "VoxPlanApp (post-nav-update): Current route: $route")

            val backStack = buildBackStackInfo(navController)
            Log.d("Navigation", "VoxPlanApp (post-nav-update): Current backstack: ${backStack}")

            // update selected index in viewModel
        }

     */

    // prevent bottom bar from showing if we're in focus mode.
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute?.contains("focus_mode", ignoreCase = true) != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController, navigationViewModel)
            }
        }
    ) { innerPadding ->
        VoxPlanNavHost(navController = navController, innerPadding = innerPadding)
    }

}

fun buildBackStackInfo(navController: NavHostController): String {

    val backStackInfo = mutableListOf<String>()
    var currentEntry = navController.currentBackStackEntry

    while (currentEntry != null) {
        currentEntry.destination.route?.let { route ->
            backStackInfo.add(0,route)
        }
        currentEntry = navController.previousBackStackEntry
    }

    return backStackInfo.joinToString(" -> ")

}

/**
 * App bar to display title and conditionally display the back navigation.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoxPlanTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        modifier = modifier
            .padding(bottom = 0.dp)
            .border(width = 1.dp, color = TertiaryBorderColor, shape = RoundedCornerShape(4.dp)),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = TopAppBarBgColor
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions,
        windowInsets = WindowInsets(0, 0, 0, 0),
    )
}


data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

private val items = listOf(
    BottomNavigationItem(
        title = "Goals",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = VoxPlanScreen.Main.route
    ),
    BottomNavigationItem(
        title = "Schedule",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        route = VoxPlanScreen.DaySchedule.createRouteWithDate()
    )
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    viewModel: NavigationViewModel
) {

    val selectedItemIndex by viewModel.selectedItemIndex.collectAsState()

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    Log.d("Navigation", "BottomNavBar: Clicked on ${item.title}")
                    navController.navigate(item.route) {
                        // clear back stack and ensure we are saving states
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // prevent re-launching screen, restoring original screen state if necessary
                        launchSingleTop = true
//                        restoreState = true
                    }
                    Log.d("Navigation", "BottomNavBar: (popUpTo done) Navigated to ${item.route}")
                },
                icon = {
                    Icon(
                        imageVector =
                        if (index == selectedItemIndex) item.selectedIcon
                        else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}
