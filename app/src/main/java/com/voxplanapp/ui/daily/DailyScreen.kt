package com.voxplanapp.ui.daily

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.data.Event
import com.voxplanapp.model.ActionMode
import com.voxplanapp.navigation.ReorderButtons
import com.voxplanapp.navigation.VoxPlanTopAppBar
import com.voxplanapp.ui.constants.ActivatedColor
import com.voxplanapp.ui.constants.PrimaryColor
import com.voxplanapp.ui.constants.TertiaryBorderColor
import com.voxplanapp.ui.constants.ToolbarBorderColor
import com.voxplanapp.ui.constants.ToolbarColor
import com.voxplanapp.ui.constants.TopAppBarBgColor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionMode by viewModel.actionMode
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                    onTaskReorder = viewModel::reorderTask,
                    onTaskSchedule = viewModel::scheduleTask,
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
    Column(
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .border(width = 1.dp, color = ToolbarBorderColor, shape = RoundedCornerShape(4.dp))
            .background(ToolbarColor),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dailies",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousDay) {
                    Icon(Icons.Default.ChevronLeft, "Previous Day")
                }
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMM d")),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onNextDay) {
                    Icon(Icons.Default.ChevronRight, "Next Day")
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
    onTaskReorder: (Event) -> Unit,
    onTaskSchedule: (Event, LocalTime, LocalTime) -> Unit,
    onTaskDelete: (Event) -> Unit,
    actionMode: ActionMode,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            DailyTaskItem(
                task = task,
                onReorder = { onTaskReorder(task) },
                onSchedule = { startTime, endTime -> onTaskSchedule(task, startTime, endTime) },
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
    onDelete: () -> Unit,
    actionMode: ActionMode,
    modifier: Modifier = Modifier
) {
    var showScheduleDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = actionMode != ActionMode.Normal,
                onClick = onReorder
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge
            )

            Row {
                IconButton(onClick = { showScheduleDialog = true }) {
                    Icon(Icons.Default.Schedule, "Schedule Task")
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete Task")
                }
            }
        }
    }

    if (showScheduleDialog) {
        TimeSelectionDialog(
            onDismiss = { showScheduleDialog = false },
            onConfirm = { startTime, endTime ->
                onSchedule(startTime, endTime)
                showScheduleDialog = false
            }
        )
    }
}

@Composable
fun TimeSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime) -> Unit
) {
    var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(10, 0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Task") },
        text = {
            Column {
                // Time selection UI here
                // We can reuse components from other screens or create new ones
                Text("Time selection to be implemented")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(startTime, endTime) }) {
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