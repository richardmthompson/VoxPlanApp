package com.voxplanapp.ui.daily

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.data.Event
import com.voxplanapp.model.ActionMode
import com.voxplanapp.navigation.ReorderButtons
import com.voxplanapp.navigation.VoxPlanTopAppBar
import com.voxplanapp.ui.constants.ActivatedColor
import com.voxplanapp.ui.constants.PrimaryColor
import com.voxplanapp.ui.constants.TertiaryBorderColor
import com.voxplanapp.ui.constants.TitlebarBorderColor
import com.voxplanapp.ui.constants.TitlebarColor
import com.voxplanapp.ui.constants.ToolbarBorderColor
import com.voxplanapp.ui.constants.ToolbarColor
import com.voxplanapp.ui.constants.TopAppBarBgColor
import com.voxplanapp.ui.goals.TimeUnit
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionMode by viewModel.actionMode
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // state variable for managing delete dialog
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()

    showDeleteConfirmation?.let { task ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Daily Task") },
            text = { Text("This will also delete all scheduled events associated with this task. Do you want to continue?") },
            confirmButton = {
                Button(onClick = { viewModel.confirmDelete(task) }) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                // Date Navigation
                DailyHeader(
                    date = uiState.date,
                    onPreviousDay = { viewModel.updateDate(uiState.date.minusDays(1)) },
                    onNextDay = { viewModel.updateDate(uiState.date.plusDays(1)) },
                )

                VoxPlanTopAppBar(
                title = "Dailies",
                canNavigateBack = false,
                scrollBehavior = scrollBehavior,
                actions = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DailyActionButtons(
                            onTodayClick = { viewModel.updateDate(LocalDate.now()) },
                            onAddQuotasClick = { viewModel.addQuotaTasks() },
                            onVUpClick = { viewModel.actionModeHandler.toggleUpActive() },
                            onVDownClick = { viewModel.actionModeHandler.toggleDownActive() },
                            currentMode = actionMode
                        )
                    }
                }
            )
        }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ToolbarColor)
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                DailyTaskList(
                    tasks = uiState.dailyTasks,
                    eventNeedingDuration = uiState.eventNeedingDuration,
                    onTaskReorder = viewModel::reorderTask,
                    onTaskSchedule = viewModel::scheduleTask,
                    onTaskDuration = viewModel::setTaskDuration,
                    onTaskDelete = viewModel::deleteTask,
                    actionMode = actionMode
                )
            }
        }
    }
}

@Composable
fun DailyHeader(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier
) {

    val dateText = getRelativeDayText(date)

    Column(
        modifier = modifier
            .fillMaxWidth()
            //.border(width = 1.dp, color = TitlebarBorderColor)
            .background(TitlebarColor),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dailies",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousDay,
                    modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Default.ChevronLeft, "Previous Day")
                }
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMM d")),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onNextDay,
                    modifier = Modifier.size(42.dp)
                    ) {
                    Icon(Icons.Default.ChevronRight, "Next Day")
                }
            }
        }
    }
}

fun getRelativeDayText(date: LocalDate): String {
    val today = LocalDate.now()

    // Get reference points for weeks
    val startOfThisWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val startOfLastWeek = startOfThisWeek.minusWeeks(1)
    val startOfNextWeek = startOfThisWeek.plusWeeks(1)
    val startOfWeekAfterNext = startOfNextWeek.plusWeeks(1)

    return when {
        // Immediate cases
        date == today -> ": (today)"
        date == today.plusDays(1) -> ": (tomorrow)"
        date == today.minusDays(1) -> ": (yesterday)"

        else -> {
            val dayName = date.format(DateTimeFormatter.ofPattern("EE"))

            when {
                // This week (both past and future)
                date.isAfter(startOfThisWeek.minusDays(1)) &&
                        !date.isAfter(startOfNextWeek.minusDays(1)) ->
                    ": (this $dayName)"

                // Last week
                date.isAfter(startOfLastWeek.minusDays(1)) &&
                        !date.isAfter(startOfThisWeek.minusDays(1)) ->
                    ": (last $dayName)"

                // Next week
                date.isAfter(startOfNextWeek.minusDays(1)) &&
                        !date.isAfter(startOfWeekAfterNext.minusDays(1)) ->
                    ": (next $dayName)"

                // Further in past
                date.isBefore(today) -> {
                    val weeks = ChronoUnit.WEEKS.between(date, today)
                    ": ($dayName $weeks weeks ago)"
                }

                // Further in future
                else -> {
                    val weeks = ChronoUnit.WEEKS.between(today, date)
                    ": ($dayName in $weeks weeks)"
                }
            }
        }
    }
}

@Composable
fun DailyActionButtons(
    onTodayClick: () -> Unit,
    onAddQuotasClick: () -> Unit,
    onVUpClick: () -> Unit,
    onVDownClick: () -> Unit,
    currentMode: ActionMode
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side utility buttons
        Row(
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onTodayClick) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = "Go to Today",
                    tint = PrimaryColor
                )
            }
            IconButton(onClick = onAddQuotasClick) {
                Icon(
                    Icons.Default.AddTask,
                    contentDescription = "Add Quota Tasks",
                    tint = PrimaryColor
                )
            }
        }

        // Right side reorder buttons
        Row(
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onVUpClick) {
                Icon(
                    imageVector = Icons.Default.ArrowCircleUp,
                    contentDescription = "Reorder upward",
                    tint = if (currentMode == ActionMode.VerticalUp) ActivatedColor else PrimaryColor
                )
            }
            IconButton(onClick = onVDownClick) {
                Icon(
                    imageVector = Icons.Default.ArrowCircleDown,
                    contentDescription = "Reorder downward",
                    tint = if (currentMode == ActionMode.VerticalDown) ActivatedColor else PrimaryColor
                )
            }
        }
    }
}

@Composable
fun DailyTaskList(
    tasks: List<Event>,
    eventNeedingDuration: Int?,
    onTaskReorder: (Event) -> Unit,
    onTaskSchedule: (Event, LocalTime, LocalTime) -> Unit,
    onTaskDuration: (Event, Int) -> Unit,
    onTaskDelete: (Event) -> Unit,
    actionMode: ActionMode,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            Log.d("Daily", "matching task id ${task.id} with event id ${eventNeedingDuration}")

            DailyTaskItem(
                task = task,
                needsDuration = task.id == eventNeedingDuration,
                onReorder = { onTaskReorder(task) },
                onSchedule = { startTime, endTime -> onTaskSchedule(task, startTime, endTime) },
                onSetDuration = { duration -> onTaskDuration(task, duration) },
                onDelete = { onTaskDelete(task) },
                actionMode = actionMode
            )
        }
    }
}

@Composable
fun DailyTaskItem(
    task: Event,
    onReorder: () -> Unit,
    onSchedule: (LocalTime, LocalTime) -> Unit,
    onSetDuration: (Int) -> Unit,
    onDelete: () -> Unit,
    actionMode: ActionMode,
    needsDuration: Boolean,
    modifier: Modifier = Modifier
) {
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(needsDuration) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = actionMode != ActionMode.Normal,
                onClick = onReorder
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 2.dp, bottom = 2.dp)
                .fillMaxWidth(),
        ) {
            // Title and icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                // Icons
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            "Schedule Task",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete Task",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            // Progress row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 1st Q
                Spacer(modifier = Modifier.weight(1f))

                // 2nd Q
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    QuotaProgressIndicator(
                        quotaDuration = task.quotaDuration,
                        scheduledDuration = task.scheduledDuration,
                        completedDuration = task.completedDuration,
                        modifier = Modifier
                            .padding(top = 0.dp)
                    )
                }

                // 3rd Q
                if (task.quotaDuration != null) {
                    val completed = task.completedDuration ?: 0
                    val quota = task.quotaDuration
                    Text(
                        text = when {
                            quota >= 60 -> "${completed / 60}/${quota / 60}h"
                            else -> "$completed/$quota m"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                // 4th Q
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Dialogs remain unchanged
    if (showDurationDialog) {
        DurationSelectionDialog(
            onDismiss = { showDurationDialog = false },
            onConfirm = { duration -> onSetDuration(duration) }
        )
    }
    if (showScheduleDialog) {
        TimeSelectionDialog(
            duration = task.quotaDuration ?: 60,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { startTime, endTime ->
                onSchedule(startTime, endTime)
                showScheduleDialog = false
            }
        )
    }
}

@Composable
fun QuotaProgressIndicator(
    quotaDuration: Int?,
    scheduledDuration: Int?,
    completedDuration: Int?,
    modifier: Modifier = Modifier
) {
    if (quotaDuration == null) return
    val scheduled = scheduledDuration ?: 0
    val completed = completedDuration ?: 0

    // Calculate number of boxes needed
    val boxes = if (quotaDuration < 60) {
        1 // Show one box for durations less than an hour
    } else {
        quotaDuration / 60 // Convert minutes to hours for larger durations
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(boxes) { index ->
            val boxColor = when {
                quotaDuration < 60 -> {
                    // For sub-hour tasks, use completion percentage for the single box
                    when {
                        completed >= quotaDuration -> Color.Green // Fully completed
                        scheduled >= quotaDuration -> Color(0xFFFFA500) // Fully scheduled
                        else -> Color.LightGray // Not scheduled/completed
                    }
                }
                else -> {
                    // For hour-based tasks, use the existing logic
                    when {
                        index < completed/60 -> Color.Green  // Completed
                        index < scheduled/60 -> Color(0xFFFFA500) // Orange for scheduled
                        else -> Color.LightGray // Remaining
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(boxColor, RoundedCornerShape(2.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun DurationSelectionDialog(
    initialDuration: Int = 60,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var duration by remember { mutableStateOf(initialDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Task Duration") },
        text = {
            Column {
                Text("How long should this task take?")
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { if (duration > 15) duration -= 15 }) {
                        Icon(Icons.Default.Remove, "Decrease duration")
                    }
                    Text("${duration / 60}h ${duration % 60}m")
                    IconButton(onClick = { duration += 15 }) {
                        Icon(Icons.Default.Add, "Increase duration")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(duration) }) {
                Text("Set Duration")
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
fun TimeSelectionDialog(
    duration: Int,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime) -> Unit
) {
    var startTime by remember { mutableStateOf(LocalTime.of(LocalTime.now().hour, LocalTime.now().minute)) }
    var duration by remember { mutableStateOf(duration) } // Default 60 minutes

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time selector
                Text("Start Time")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeUnit(
                        value = startTime.hour,
                        range = 0..23,
                        onValueChange = { startTime = startTime.withHour(it) }
                    )
                    Text(":")
                    TimeUnit(
                        value = startTime.minute,
                        range = 0..45 step 15,
                        onValueChange = { startTime = startTime.withMinute(it) }
                    )
                }

                // Duration selector
                Text("Duration")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (duration > 15) duration -= 15 }) {
                        Icon(Icons.Default.Remove, "Decrease duration")
                    }
                    Text("${duration / 60}h ${duration % 60}m")
                    IconButton(onClick = { duration += 15 }) {
                        Icon(Icons.Default.Add, "Increase duration")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    startTime,
                    startTime.plusMinutes(duration.toLong())
                )
            }) {
                Text("Schedule")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}