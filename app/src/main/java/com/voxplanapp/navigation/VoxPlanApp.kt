package com.voxplanapp.navigation

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
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.ui.constants.TertiaryBorderColor
import com.voxplanapp.ui.constants.TopAppBarBgColor

/**
 *  VoxPlanApp overall nav controller function.  Central navigation control for screen switching.
 */

@Composable
fun VoxPlanApp(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, navigationViewModel) }
    ) { innerPadding ->
        VoxPlanNavHost(navController = navController, innerPadding = innerPadding)
    }

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

private val items = listOf(
    BottomNavigationItem(
        title = "Goals",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = VoxPlanScreen.Main
    ),
    BottomNavigationItem(
        title = "Schedule",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        route = VoxPlanScreen.DaySchedule
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
                    viewModel.setSelectedItemIndex(index)
                    navController.navigate(item.route.route)
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

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: VoxPlanScreen
)
