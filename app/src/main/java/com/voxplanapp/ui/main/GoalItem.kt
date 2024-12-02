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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Stream
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
import androidx.compose.ui.graphics.Color
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
import java.time.LocalDate

@Composable
fun GoalItem(
    goal: GoalWithSubGoals,
    onItemClick: (Int) -> Unit = {},
    onItemComplete: (TodoItem) -> Unit,
    saveExpandedSetting: (Int, Boolean) -> Unit,
    onSubGoalsClick: (GoalWithSubGoals) -> Unit,
    onItemDelete: (GoalWithSubGoals) -> Unit,
    onEnterFocusMode: (Int) -> Unit,
    actionMode: ActionMode,
    onItemReorder: (GoalWithSubGoals) -> Unit
) {
    var expanded by remember { mutableStateOf(goal.goal.expanded) }
    var showDropdown by remember { mutableStateOf(false) }

    // todo: set these when refactoring goalitem & subgoalitems
    //val backgroundColor = if (goal.goal.isDone) TodoItemBackgroundColor.copy(alpha = 0.5f) else TodoItemBackgroundColor
    val textColor = if (goal.goal.completedDate != null) TodoItemTextColor.copy(alpha = 0.5f) else TodoItemTextColor
    val textDecoration = if (goal.goal.completedDate != null) TextDecoration.LineThrough else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = TopLevelGoalBorderColor,
                shape = RoundedCornerShape(size = MediumDp)
            )
    ) {
        Column {
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
                                ActionMode.HierarchyDown, ActionMode.HierarchyUp -> onItemReorder(
                                    goal
                                )

                                else -> {
                                    showDropdown = !showDropdown
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    // show sub-goals expand icon
                    var frontPadding = 16.dp
                    if (goal.subGoals.isNotEmpty()) {
                        frontPadding = 0.dp
                        IconButton(
                            onClick = {
                                expanded = !expanded
                                // now also modify the database so the setting is saved.
                                saveExpandedSetting(goal.goal.id, expanded)
                            },
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
                            .padding(start = frontPadding, bottom = SmallDp),
                        style = TodoItemTitleTextStyle.copy(color = textColor),
                        textDecoration = textDecoration
                        //maxLines = 1,
                        //overflow = TextOverflow.Ellipsis,
                    )

                    HasSubGoalsIcon(goal = goal, onSubGoalsClick = onSubGoalsClick)

                    FocusModeIcon(goal = goal, onEnterFocusMode = onEnterFocusMode)
                    //TickBoxIcon(goal = goal)

                }   // item row
            }   // item card
        }
    }

    if (showDropdown) {
        Column {
            IconDropDownMenu(
                goal = goal,
                onEditClick = { onItemClick(goal.goal.id) },
                onCompleteClick = onItemComplete,
                onDeleteClick = onItemDelete
            )
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
                if ((subGoal.goal.completedDate != null && subGoal.goal.completedDate == LocalDate.now()) || (subGoal.goal.completedDate == null)) {
                    SubGoalItem(
                        subGoal = subGoal,
                        onSubItemEdit = onItemClick,
                        onSubGoalsClick = onSubGoalsClick,
                        onEnterFocusMode = onEnterFocusMode,
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
}

@Composable
fun IconDropDownMenu(
    goal: GoalWithSubGoals,
    onEditClick: () -> Unit,
    onCompleteClick: (TodoItem) -> Unit,
    onDeleteClick: (GoalWithSubGoals) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(start = MediumDp)
            .background(
                Color(0xFFFFF8DC),
                shape = RoundedCornerShape(bottomStart = MediumDp, bottomEnd = MediumDp)
            )
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(bottomStart = MediumDp, bottomEnd = MediumDp)
            )
            .padding(vertical = 0.dp, horizontal = 0.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = TodoItemIconColor,
                modifier = Modifier.size(SubGoalItemIconSize),
            )
        }

        IconButton(onClick = {
            onCompleteClick(goal.goal)
            Log.d("mainscreen","complete icon just got clicked biiiitch")
        } ) {
            Icon(
                painter =
                    if (goal.goal.completedDate != null) painterResource(id = R.drawable.ic_selected_check_box)
                    else painterResource(R.drawable.ic_empty_check_box),
                contentDescription = if (goal.goal.completedDate != null) "Mark Incomplete" else "Mark Complete",
                tint = TodoItemIconColor,
                modifier = Modifier
                    .size(SubGoalItemIconSize),
            )
        }

        IconButton(
            onClick = {
                onDeleteClick(goal)
                Log.d("mainscreen","DELETE icon just got clicked on \"${goal.goal.title}\"")
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
                tint = TodoItemIconColor,
                modifier = Modifier.size(SubGoalItemIconSize),
            )
        }
    }
}

@Composable
fun SubGoalItem(
    subGoal: GoalWithSubGoals,
    onSubItemEdit: (Int) -> Unit,
    onSubGoalsClick: (GoalWithSubGoals) -> Unit,
    onEnterFocusMode: (Int) -> Unit,
    onSubItemDelete: (GoalWithSubGoals) -> Unit,
    actionMode: ActionMode,
    onSubItemReorder: (GoalWithSubGoals) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (subGoal.goal.completedDate != null) TodoItemTextColor.copy(alpha = 0.5f) else TodoItemTextColor
    val textDecoration = if (subGoal.goal.completedDate != null) TextDecoration.LineThrough else null

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
            .padding(vertical = 0.dp, horizontal = MediumDp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = subGoal.goal.title,
            style = TodoItemTitleTextStyle.copy(color = textColor),
            textDecoration = textDecoration,
            modifier = Modifier.weight(1f),
        )

        HasSubGoalsIcon(goal = subGoal, onSubGoalsClick = onSubGoalsClick)

        FocusModeIcon(goal = subGoal, onEnterFocusMode = onEnterFocusMode)

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
                tint = TodoItemIconColor.copy(alpha = if (goal.goal.completedDate != null) 0.5f else 1f)
            )
        }
    }
}

@Composable
fun FocusModeIcon(
    goal: GoalWithSubGoals,
    onEnterFocusMode: (Int) -> Unit,
    ) {
    IconButton(
        onClick = { onEnterFocusMode(goal.goal.id) }
        ) {
            Icon(
                Icons.Default.Stream,
                contentDescription = "Focus Mode",
                modifier = Modifier
                    .size(SubGoalItemIconSize)
                    .padding(0.dp)
            )
        }
}
