package com.voxplanapp.ui.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voxplanapp.R
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.TodoItem
import com.voxplanapp.model.ActionMode
import com.voxplanapp.ui.constants.*

@Composable
fun GoalItem(
    goal: GoalWithSubGoals,
    onItemClick: (Int) -> Unit = {},
    onSubGoalsClick: (GoalWithSubGoals) -> Unit = {},
    onItemDelete: (GoalWithSubGoals) -> Unit = {},
    actionMode: ActionMode,
    onItemReorder: (GoalWithSubGoals) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    val backgroundColor = if (goal.goal.isDone) TodoItemBackgroundColor.copy(alpha = 0.5f) else TodoItemBackgroundColor
    val textColor = if (goal.goal.isDone) TodoItemTextColor.copy(alpha = 0.5f) else TodoItemTextColor
    val textDecoration = if (goal.goal.isDone) TextDecoration.LineThrough else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = TopLevelGoalBorderColor,
                shape = RoundedCornerShape(size = MediumDp)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(TodoItemHeight),
            elevation = CardDefaults.cardElevation(defaultElevation = LargeDp),
            shape = RoundedCornerShape(size = MediumDp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TodoItemBackgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true)
                    ) {
                        when (actionMode) {
                            ActionMode.VerticalUp, ActionMode.VerticalDown,
                            ActionMode.HierarchyDown, ActionMode.HierarchyUp -> onItemReorder(goal)

                            else -> onItemClick(goal.goal.id)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {

                // show sub-goals expand icon
                var frontPadding = 16.dp
                if (goal.subGoals.isNotEmpty()) {
                    frontPadding = 0.dp
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(TodoItemActionButtonRippleRadius)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = TodoItemIconColor
                        )
                    }
                }

                // show goal card
                Text(
                    text = goal.goal.title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = frontPadding),
                    style = TodoItemTitleTextStyle.copy(color = textColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                HasSubGoalsIcon(goal = goal, onSubGoalsClick = onSubGoalsClick)

                TickBoxIcon(goal = goal)

                TrashIcon(goal = goal, onItemDelete = onItemDelete)
            }
        }
    }

    if (expanded && goal.subGoals.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = MediumDp, end = MediumDp, bottom = SmallDp)
                .clip(RoundedCornerShape(bottomStart = MediumDp, bottomEnd = MediumDp))
                .background(SubGoalItemBackGroundColor)
        ) {

            goal.subGoals.forEachIndexed { index, subGoal ->
                SubGoalItem(
                    subGoal = subGoal,
                    onSubItemEdit = onItemClick,
                    onSubGoalsClick = onSubGoalsClick,
                    onSubItemDelete = onItemDelete,
                    onSubItemReorder = onItemReorder,
                    actionMode = actionMode,
                    modifier = Modifier.padding(start = MediumDp)
                )

                if (index < goal.subGoals.lastIndex) {
                    androidx.compose.material3.Divider(
                        modifier = Modifier.padding(start = MediumDp, end = MediumDp),
                        color = DividerColor,
                        thickness = DividerThickness
                    )
                }
            }
        }
    }
}

@Composable
fun SubGoalItem(
    subGoal: GoalWithSubGoals,
    onSubItemEdit: (Int) -> Unit,
    onSubGoalsClick: (GoalWithSubGoals) -> Unit,
    onSubItemDelete: (GoalWithSubGoals) -> Unit,
    actionMode: ActionMode,
    onSubItemReorder: (GoalWithSubGoals) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                when (actionMode) {
                    ActionMode.VerticalUp, ActionMode.VerticalDown,
                    ActionMode.HierarchyUp, ActionMode.HierarchyDown -> {
                        onSubItemReorder(subGoal)
                        Log.d("goalItem", "SubGoalItem: Reordering subgoal: ${subGoal.goal.title}")
                    }

                    else -> onSubItemEdit(subGoal.goal.id)
                }
            }
            .padding(MediumDp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = subGoal.goal.title,
            style = TodoSubItemTitleTextStyle.copy(color = SubGoalItemTextColor),
            modifier = Modifier
                .weight(1f),
            color = SubGoalItemTextColor
        )

        HasSubGoalsIcon(goal = subGoal, onSubGoalsClick = onSubGoalsClick)

        TickBoxIcon(goal = subGoal)

        TrashIcon(
            goal = subGoal,
            onItemDelete = onSubItemDelete
        )
    }
}

@Composable
fun HasSubGoalsIcon(goal: GoalWithSubGoals, onSubGoalsClick: (GoalWithSubGoals) -> Unit) {

    if (goal.subGoals.isNotEmpty()) {
        IconButton(
            onClick = { onSubGoalsClick(goal) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Has Sub-goals",
                tint = TodoItemIconColor.copy(alpha = if (goal.goal.isDone) 0.5f else 1f)
            )
        }
    }
}

@Composable
fun TickBoxIcon(goal: GoalWithSubGoals) {

    Image(
        painter = painterResource(id = if (goal.goal.isDone) R.drawable.ic_selected_check_box else R.drawable.ic_empty_check_box),
        contentDescription = null,
        modifier = Modifier
            .padding(end = SmallDp)
            .size(SubGoalItemIconSize),
        colorFilter = ColorFilter.tint(SubGoalItemIconColor)
    )
}
@Composable
fun TrashIcon(
    goal: GoalWithSubGoals,
    onItemDelete: (GoalWithSubGoals) -> Unit
) {
    IconButton(
        onClick = { onItemDelete(goal) },
        modifier = Modifier.size(SubGoalItemActionButtonRippleRadius)
    ) {
        Icon(
            modifier = Modifier.size(SubGoalItemIconSize),
            painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = null,
            tint = SubGoalItemIconColor
        )
    }

}