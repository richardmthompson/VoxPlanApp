package com.voxplanapp.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.R
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.RecurrenceType
import com.voxplanapp.data.TodoItem
import com.voxplanapp.navigation.VoxPlanTopAppBar
import com.voxplanapp.ui.constants.LargeDp
import com.voxplanapp.ui.constants.MediumDp
import com.voxplanapp.ui.constants.PrimaryDarkColor
import com.voxplanapp.ui.constants.PrimaryLightColor
import com.voxplanapp.ui.constants.ToolbarBorderColor
import com.voxplanapp.ui.constants.ToolbarColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.Async.Schedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*  VoxPlanNavHost brings us here with goalId navigation argument, of a TodoItem.
    We use the ViewModel function to reconstruct the GoalWithSubGoal, and use that,
    in case we need to access subgoals later (and for consistency across the app)
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    onNavigateToFocusMode: (Int) -> Unit,
    onNavigateToScheduler: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.goalUiState
    val parentGoalTitle = runBlocking { viewModel.getParentGoalTitle() }
    val scrollState = rememberScrollState()
    var showScheduleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            VoxPlanTopAppBar(
                title = stringResource(R.string.goal_edit_screen),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        // for now, let's just show the desired info and allow ourselves to back out of it too.
        Column(
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                )
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            if (uiState.goal == null) {
                Text("Error loading: ${uiState.error}")
            } else {
                GoalEntryBody(
                    uiState = uiState,
                    parentGoalTitle = parentGoalTitle,
                    onValueChange = { attribute, value -> viewModel.updateGoalAttribute(attribute, value) },
                    onNavigateToFocusMode = { goalId -> onNavigateToFocusMode(goalId) },
                    onQuotaMinutesChanged = viewModel::updateQuotaMinutes,
                    onActiveDaysChanged = viewModel::updateQuotaActiveDays,
                    onRemoveQuota = viewModel::removeQuota,
                    onScheduleClick = { showScheduleDialog = true },
                    onSaveClick = {
                        viewModel.saveGoal()
                        onNavigateUp()
                    }
                )
                Spacer(modifier = Modifier.height(80.dp))

                if (showScheduleDialog) {
                    ScheduleDialog(
                        goalWithSubGoals = uiState.goal,
                        onDismiss = { showScheduleDialog = false },
                        onConfirm = { startDate ->
                            showScheduleDialog = false
                            viewModel.saveGoal()
                            viewModel.scheduleGoal(startDate)
                            // now open the scheduler
                            onNavigateToScheduler(startDate)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun GoalEntryBody(
    uiState: GoalUiState,
    parentGoalTitle: String?,
    onValueChange: (String, Any) -> Unit,
    onSaveClick: () -> Unit,
    onQuotaMinutesChanged: (Int) -> Unit,
    onActiveDaysChanged: (Set<DayOfWeek>) -> Unit,
    onRemoveQuota: () -> Unit,
    onNavigateToFocusMode: (Int) -> Unit,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goal = uiState.goal!!.goal

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ItemIdRow(
            goalId = goal.id,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        /* show position of goal in hierarchy */
        GoalHierarchyDisplay(
            goal = goal,
            parentGoalTitle = parentGoalTitle,
            modifier = Modifier
                .fillMaxWidth()
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = goal.title,
                // updates the Ui State with any changes to the text field as they happen
                onValueChange = { onValueChange("title", it) },
                label = { Text(stringResource(id = R.string.goal_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(modifier = modifier
                .background(color = ToolbarColor, shape = RoundedCornerShape(MediumDp))
                .border(width = 1.dp, ToolbarBorderColor, shape = RoundedCornerShape(MediumDp))
            ) {
                Text(
                    text = "Scheduling details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = modifier.padding(start = 4.dp, top = 4.dp)
                )

                PreferredTimeSelector(
                    preferredTime = goal.preferredTime ?: LocalTime.of(7, 0),
                    onTimeSelected = { onValueChange("preferredTime", it) },
                    modifier = modifier.padding(start = 4.dp, top = 0.dp)
                )

                DurationSelector(
                    duration = goal.estDurationMins ?: 30,
                    onDurationChanged = { onValueChange("estDurationMins", it) },
                    modifier = modifier.padding(start = 4.dp, top = 0.dp)
                )

                FrequencySelector(
                    frequency = goal.frequency,
                    onFrequencyChanged = { onValueChange("frequency", it) },
                    modifier = modifier.padding(start = 4.dp, top = 0.dp)
                )

                Row (modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { onNavigateToFocusMode(goal.id) }
                    ) {
                        Text("Focus Mode!")
                    }
                    Button(
                        onClick = { onScheduleClick() },
                    ) { Text("Schedule!") }
                }
            }

            QuotaSettingsSection(
                quotaMinutes = uiState.quotaMinutes,
                activeDays = uiState.quotaActiveDays,
                onQuotaMinutesChanged = onQuotaMinutesChanged,
                onActiveDaysChanged = onActiveDaysChanged,
                onRemoveQuota = onRemoveQuota
            )

            OutlinedTextField(
                value = goal.notes ?: "",
                onValueChange = { onValueChange("notes", it) },
                label = { Text(stringResource(R.string.goal_notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                singleLine = false
            )

            /* isdone */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = goal.isDone,
                    onCheckedChange = { onValueChange("isDone", it) },
                )
                Text(
                    text = stringResource(id = R.string.is_goal_done),
                    modifier = Modifier.padding(start=8.dp)
                )
            }
        }

        /* save button */
        Button(
            onClick = { onSaveClick() },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.save_button))
        }
    }
}

@Composable
fun ItemIdRow(
    goalId: Int,
    modifier: Modifier = Modifier
) {
    // item id row
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        Text(stringResource(id = R.string.goal_id_label))
        Spacer(modifier = Modifier.padding(end = 16.dp))
        Text(text = goalId.toString())
    }
}

@Composable
fun GoalHierarchyDisplay(
    goal: TodoItem,
    parentGoalTitle: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Goal Hierarchy - ${if (goal.parentId != null) "SubGoal" else "TopLevelGoal"}:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Top Level Goal",
                tint = PrimaryDarkColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (goal.parentId == null) goal.title else "${parentGoalTitle}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (goal.parentId != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Sub Goal",
                    tint = PrimaryLightColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    goalWithSubGoals: GoalWithSubGoals,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val goal = goalWithSubGoals.goal
    var startDate by remember { mutableStateOf(LocalDate.now()) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Goal with Following Parameters?") },
        text = {
            Column {
                Text("Goal: ${goal.title}")
                Text("Time: ${goal.preferredTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "None"}")
                Text("Duration: ${goal.estDurationMins ?: 101} minutes")
                Text("Frequency: ${goal.frequency}")
                Spacer(modifier = Modifier.height(LargeDp))
                Text("Date: ${startDate}.")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(startDate) }) {
                Text("Schedule Now")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }

        }
    )

}


@Composable
fun PreferredTimeSelector(
    preferredTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {

    val initialTime = remember { preferredTime.takeIf { it != LocalTime.of(7, 0) } ?: LocalTime.now() }
    var hours by remember { mutableIntStateOf(initialTime.hour) }
    var minutes by remember { mutableIntStateOf(initialTime.minute)}

    Row(verticalAlignment = Alignment.CenterVertically) {

        Text(text = "Time:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = MediumDp))
        Spacer(modifier = Modifier.width(16.dp))
        TimeUnit(value = hours, range = 1..24 step 1, onValueChange = { hours = it})
        Text(":")
        TimeUnit(value = minutes, range = 0..45 step 15, onValueChange = { minutes = it})
    }

    LaunchedEffect(hours, minutes) { onTimeSelected(LocalTime.of(hours,minutes)) }

}

@Composable
fun DurationSelector(
    duration: Int,
    onDurationChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(verticalAlignment = Alignment.CenterVertically) {

        Text("Est. duration:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = MediumDp))
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = { onDurationChanged(maxOf(duration - 15, 15)) } ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease duration")
        }
        Text("${duration / 60} hrs ${duration %60} mins")
        IconButton(onClick = { onDurationChanged(duration + 15) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase duration")
        }
    }
}

@Composable
fun FrequencySelector(
    frequency: RecurrenceType,
    onFrequencyChanged: (RecurrenceType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row (verticalAlignment = Alignment.CenterVertically) {
        Text("Frequency:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = MediumDp, bottom = MediumDp))
        Spacer(modifier = Modifier.width(16.dp))
        Box (modifier = Modifier.padding(bottom = MediumDp)){
            Text(
                text = frequency.name,
                modifier = Modifier
                    .clickable { expanded = true }
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                RecurrenceType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            onFrequencyChanged(type)
                            expanded = false
                        }
                    )
                } //foreach
            } // ddm
        } //box

    }   //row
}

@Composable
fun TimeUnit(
    value: Int,
    range: IntProgression,
    onValueChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onValueChange(if (value < range.last) value + range.step else range.first) }) {
            Icon(Icons.Default.KeyboardArrowUp, "Increase")
        }
        Text(value.toString().padStart(2, '0'))
        IconButton(onClick = { onValueChange(if (value > range.first) value - range.step else range.last) }) {
            Icon(Icons.Default.KeyboardArrowDown, "Decrease")
        }
    }
}

