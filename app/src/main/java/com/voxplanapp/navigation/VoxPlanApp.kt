package com.voxplanapp.navigation

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Today
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.R
import com.voxplanapp.model.ActionMode
import com.voxplanapp.ui.constants.ActivatedColor
import com.voxplanapp.ui.constants.PrimaryColor
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
                route.startsWith(VoxPlanScreen.Daily.route) -> navigationViewModel.setSelectedItemIndex(1)
                route.startsWith(VoxPlanScreen.Progress.route) -> navigationViewModel.setSelectedItemIndex(2)
                route.startsWith(VoxPlanScreen.DaySchedule.route) -> navigationViewModel.setSelectedItemIndex(3)
            }
        }
    }

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

@Composable
fun ReorderButtons(
    onVUpClick: (() -> Unit)?,
    onVDownClick: (() -> Unit)?,
    onHUpClick: (() -> Unit)?,
    onHDownClick: (() -> Unit)?,
    currentMode: ActionMode
) {
    Row(
        horizontalArrangement = Arrangement.End
    ) {
        if (onVUpClick != null) {
        IconButton(onClick = onVUpClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleUp,
                contentDescription = stringResource(id = R.string.reorder_upward),
                tint = if (currentMode == ActionMode.VerticalUp) ActivatedColor else PrimaryColor
            )
        } }
        if (onVDownClick != null) {
        IconButton(onClick = onVDownClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleDown,
                contentDescription = stringResource(id = R.string.reorder_downward),
                tint = if (currentMode == ActionMode.VerticalDown) ActivatedColor else PrimaryColor
            )
        } }
        if (onHUpClick != null) {
        IconButton(onClick = onHUpClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleLeft,
                contentDescription = stringResource(id = R.string.reorder_left),
                tint = if (currentMode == ActionMode.HierarchyUp) ActivatedColor else PrimaryColor
            )
        } }
        if (onHDownClick != null) {
        IconButton(onClick = onHDownClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleRight,
                contentDescription = stringResource(id = R.string.reorder_right),
                tint = if (currentMode == ActionMode.HierarchyDown) ActivatedColor else PrimaryColor
            )
        } }
    }
}

class ActionModeHandler(
    private val actionModeState: MutableState<ActionMode>
) {
    private fun setActionMode(mode: ActionMode) {
        actionModeState.value = mode
    }

    private fun resetActionMode() {
        actionModeState.value = ActionMode.Normal
    }

    fun toggleUpActive() {
        if (actionModeState.value == ActionMode.VerticalUp) resetActionMode()
        else {
            setActionMode(ActionMode.VerticalUp)
        }
    }

    // toggles the Vertical Down setting & button state
    fun toggleDownActive() {
        if (actionModeState.value == ActionMode.VerticalDown) resetActionMode()
        else {
            setActionMode(ActionMode.VerticalDown)
        }
    }

    fun toggleHierarchyUp() {
        if (actionModeState.value == ActionMode.HierarchyUp) resetActionMode()
        else {
            setActionMode(ActionMode.HierarchyUp)
        }
    }

    fun toggleHierarchyDown() {
        if (actionModeState.value == ActionMode.HierarchyDown) resetActionMode()
        else {
            setActionMode(ActionMode.HierarchyDown)
        }
    }

    fun deactivateButtons() {
        when (actionModeState.value) {
            ActionMode.VerticalUp -> toggleUpActive()
            ActionMode.VerticalDown -> toggleDownActive()
            ActionMode.HierarchyUp -> toggleHierarchyUp()
            ActionMode.HierarchyDown -> toggleHierarchyDown()
            ActionMode.Normal -> {}
        }
    }
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
        title = "Daily",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today,
        route = VoxPlanScreen.Daily.createRouteWithDate()
    ),
    BottomNavigationItem(
        title = "Progress",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline,
        route = VoxPlanScreen.Progress.route
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
