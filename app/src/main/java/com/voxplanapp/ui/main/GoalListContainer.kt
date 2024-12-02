package com.voxplanapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.TodoItem
import com.voxplanapp.model.ActionMode
import com.voxplanapp.ui.constants.MediumDp
import java.time.LocalDate

@Composable
fun GoalListContainer(
    modifier: Modifier = Modifier,
    goalList: List<GoalWithSubGoals>,
    onItemClick: (Int) -> Unit = {},
    saveExpandedSetting: (Int, Boolean) -> Unit,
    onSubGoalsClick: (GoalWithSubGoals) -> Unit = {},
    onEnterFocusMode: (Int) -> Unit,
    onItemDelete: (GoalWithSubGoals) -> Unit = {},
    onItemComplete: (TodoItem) -> Unit,
    onItemReorder: (GoalWithSubGoals) -> Unit,
    actionMode: ActionMode,
    overlappingElementsHeight: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(MediumDp)
    ) {
        items(goalList, key = { goalWithSubGoal -> goalWithSubGoal.goal.id }) { goalWithSubGoals ->

            if ((goalWithSubGoals.goal.completedDate != null && goalWithSubGoals.goal.completedDate == LocalDate.now()) || (goalWithSubGoals.goal.completedDate == null)) {

                GoalItem(
                    goal = goalWithSubGoals,
                    // the item click is a callback to the Edit screen, installed from NavHost
                    onItemClick = onItemClick,
                    saveExpandedSetting = saveExpandedSetting,
                    onSubGoalsClick = onSubGoalsClick,
                    onItemDelete = onItemDelete,
                    onItemComplete = onItemComplete,
                    onItemReorder = onItemReorder,
                    onEnterFocusMode = onEnterFocusMode,
                    actionMode = actionMode
                )
            }
        }

        item { Spacer(modifier = Modifier.height(overlappingElementsHeight)) }

    }
}
/*

@Preview
@Composable
fun TodoItemsContainerPreview() {
    GoalListContainer(
        todos =
            listOf(
                TodoItem(title = "Todo Item 1", isDone = true),
                TodoItem(title = "Todo Item 2"),
                TodoItem(title = "Todo Item 3"),
                TodoItem(title = "Todo Item 4", isDone = true),
            )
    )
}
*/
