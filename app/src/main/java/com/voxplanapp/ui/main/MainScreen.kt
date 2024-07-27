package com.voxplanapp.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.R
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.model.ActionMode
import com.voxplanapp.navigation.BottomNavigationBar
import com.voxplanapp.navigation.VoxPlanTopAppBar
import com.voxplanapp.shared.SharedViewModel
import com.voxplanapp.ui.constants.ActivatedColor
import com.voxplanapp.ui.constants.OverlappingHeight
import com.voxplanapp.ui.constants.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigateToGoalEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // receive UiState from the ViewModel (as a State, which causes recomposition upon change)
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val actionMode by mainViewModel.actionMode

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                VoxPlanTopAppBar(
                    title = "",
                    canNavigateBack = false,
                    scrollBehavior = scrollBehavior,
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ReorderButtons(
                                onVUpClick = { mainViewModel.toggleUpActive() },
                                onVDownClick = { mainViewModel.toggleDownActive() },
                                onHUpClick = { mainViewModel.toggleHierarchyUp() },
                                onHDownClick = { mainViewModel.toggleHierarchyDown() },
                                currentMode = actionMode
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column {
                TodoInputBar(onAddButtonClick = mainViewModel::addTodo)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {

            BreadcrumbNavigation(
                breadcrumbs = mainUiState.breadcrumbs,
                onMainClick = { mainViewModel.clearBreadcrumbs() },
                onBreadcrumbClick = { clickedGoal ->
                    mainViewModel.navigateToSubGoals(clickedGoal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 2.dp)
            )

            GoalListContainer(
                goalList = mainUiState.goalList,
                onItemClick = navigateToGoalEdit,
                onSubGoalsClick = mainViewModel::navigateToSubGoals,
                onItemDelete = mainViewModel::deleteItem,
                onItemReorder = mainViewModel::reorderItem,
                overlappingElementsHeight = OverlappingHeight,
                // change below to moveActive = vUp, vDown, hUp, hDown
                actionMode = actionMode,
                contentPadding = PaddingValues(
                    top = 0.dp,
                    bottom = it.calculateBottomPadding(),
                    start = it.calculateStartPadding(LocalLayoutDirection.current),
                    end = it.calculateEndPadding(LocalLayoutDirection.current)
                )
            )
            }
    }
}

@Composable
fun ReorderButtons(
    onVUpClick: () -> Unit,
    onVDownClick: () -> Unit,
    onHUpClick: () -> Unit,
    onHDownClick: () -> Unit,
    currentMode: ActionMode
) {
    Row(
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onVUpClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleUp,
                contentDescription = stringResource(id = R.string.reorder_upward),
                tint = if (currentMode == ActionMode.VerticalUp) ActivatedColor else PrimaryColor
            )
        }
        IconButton(onClick = onVDownClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleDown,
                contentDescription = stringResource(id = R.string.reorder_downward),
                tint = if (currentMode == ActionMode.VerticalDown) ActivatedColor else PrimaryColor
            )
        }
        IconButton(onClick = onHUpClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleLeft,
                contentDescription = stringResource(id = R.string.reorder_left),
                tint = if (currentMode == ActionMode.HierarchyUp) ActivatedColor else PrimaryColor
            )
        }
        IconButton(onClick = onHDownClick) {
            Icon(
                imageVector = Icons.Default.ArrowCircleRight,
                contentDescription = stringResource(id = R.string.reorder_right),
                tint = if (currentMode == ActionMode.HierarchyDown) ActivatedColor else PrimaryColor
            )
        }
    }
}
