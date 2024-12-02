package com.voxplanapp.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.R
import com.voxplanapp.data.FULLBAR_MINS
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
    onEnterFocusMode: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // receive UiState from the ViewModel (as a State, which causes recomposition upon change)
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    val todayTotalTime by mainViewModel.todayTotalTime.collectAsState()

    val actionMode by mainViewModel.actionMode
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                if (mainUiState.breadcrumbs.isEmpty()) {
                    PowerBar(
                        totalMinutes = todayTotalTime,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

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
                saveExpandedSetting = mainViewModel::saveExpandedSetting,
                onSubGoalsClick = mainViewModel::navigateToSubGoals,
                onEnterFocusMode = onEnterFocusMode,
                onItemDelete = mainViewModel::deleteItem,
                onItemComplete = mainViewModel::completeItem,
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
fun PowerBar(totalMinutes: Int, modifier: Modifier = Modifier) {
    val remainingMinutes = totalMinutes % 60
    val diamondThreshold = FULLBAR_MINS * 4
    val diamonds = totalMinutes / diamondThreshold      // calculate number of diamonds
    val hasDiamond = diamonds > 0
    val showBars = totalMinutes >= diamondThreshold * (diamonds + 0.25)     // show bars after 1/4 of next diamond worth

    // if no diamond, just show 4 bars.
    // if diamond, show diamond

    // produces a map of which bars have how many mins on them
    val bars = if (showBars) {
        val remainingTime = totalMinutes - (diamondThreshold * diamonds)        // removes diamond amounts from mins
        (0..3).map { minOf(FULLBAR_MINS, maxOf(0, remainingTime - it * FULLBAR_MINS)) }
    } else if (!hasDiamond) {
        (0..3).map { minOf(FULLBAR_MINS, maxOf(0, totalMinutes - it * FULLBAR_MINS)) }
    } else {
        emptyList()
    }

    Row(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.width(120.dp)) {
            Text(
                text = "POWER:",
                style = TextStyle(
                    color = Color(0xFFFF5722),
                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.padding(end = 16.dp)
            )
            // show a small diamond and a x 1 if there is a diamond
            // if minutes > bars x 5, show diamond under power label and show bars in middle
            if (showBars && hasDiamond) {
                Spacer(modifier = Modifier.height(8.dp))
                Row (horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Diamond(size = 26.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "x ${diamonds}",
                        style = TextStyle(color = Color(0xFF4CAF50), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        if (hasDiamond && !showBars) {
            // show only diamond
            Row(horizontalArrangement = Arrangement.Center) {
                Diamond(size = 40.dp)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bars.forEach { fillAmount ->
                    OneBar(fillAmount)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Coin display
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFFFD700), CircleShape)
                .border(2.dp, Color(0xFFFF9800), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$remainingMinutes",
                style = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
fun Diamond(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .rotate(45f)
            .background(Color(0xFF9C27B0))
            .border(2.dp, Color(0xFFBA68C8))
    )

}

@Composable
fun OneBar(fillAmount: Int) {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(60.dp)
            .border(
                2.dp,
                if (fillAmount == 60) {
                    Color(0xFF1BA821)
                } else {
                    Color(0xFF3F51B5)
                }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(fillAmount / 60f)
                .background(
                    if (fillAmount == 60) Color(0xFF13D31B)
                    else Color(0xFFFF0000)
                )
        )
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
