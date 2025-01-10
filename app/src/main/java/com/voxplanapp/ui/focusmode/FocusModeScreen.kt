package com.voxplanapp.ui.focusmode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.data.Event
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.ui.constants.EventBoxColor
import com.voxplanapp.ui.constants.FocusColorRest
import com.voxplanapp.ui.constants.FocusColorRestText
import com.voxplanapp.ui.constants.FocusColorWork
import com.voxplanapp.ui.constants.FocusColorWorkText
import com.voxplanapp.ui.constants.FocusIconSize
import com.voxplanapp.ui.constants.LargeDp
import com.voxplanapp.ui.constants.MediumDp
import com.voxplanapp.ui.constants.SmallDp
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun FocusModeScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = viewModel(factory = AppViewModelProvider.Factory)
    ) {

    // set up ui state variables
    val goalUiState = viewModel.goalUiState
    val eventUiState = viewModel.eventUiState
    val focusUiState = viewModel.focusUiState
    val timerSettingsState = viewModel.timerSettingsState

    when {
        focusUiState.isLoading -> {
            Column (
                modifier = modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Button(onClick = onNavigateUp) {
                    Text("Go Back")
                }
            }
        }
        focusUiState.error != null -> {
            Column(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${focusUiState.error}")
                Button(onClick = onNavigateUp) {
                    Text("Go Back")
                }
            }
        }
        else -> {

            FocusModeTheme(currentTheme = focusUiState.currentTheme) {

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(MediumDp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // title box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = MediumDp)
                            .border(2.dp, Color(0xFF2C742E), RoundedCornerShape(SmallDp))
                            .background(Color(0xFF002702))
                            .padding(MediumDp)

                    ) {
                        Text(
                            text = "FOCUS MODE",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // task headline
                    Text(
                        text = "\"${goalUiState?.goal?.title}\"",
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    // event box indicating length of current event
                    EventBox(
                        focusUiState = focusUiState,
                        goalUiState = goalUiState,
                        eventUiState = eventUiState,
                        modifier = modifier
                    )

                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp)
                    ) {
                        Text(
                            text = "Time Bank \nMode",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = focusUiState.isDiscreteMode,
                            onCheckedChange = { viewModel.toggleFocusMode() },
                            modifier = modifier.padding(horizontal = MediumDp)
                        )
                        Text(
                            text = "Discrete Task \nMode",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(SmallDp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color(0xFF2C742E), RoundedCornerShape(MediumDp))
                            .background(Color(0xFF002702))
                            .padding(MediumDp)
                    ) {
                        Column {
                            // Timer headline
                            Text(
                                text = if (focusUiState.isDiscreteMode) "Discrete Task Mode"
                                        else if (timerSettingsState.usePomodoro) "Pomodoro Mode"
                                        else "Timed Task Mode",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(MediumDp))

                            // Timer clock & buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                //Timer display
                                Box(
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .padding(start = LargeDp)
                                ) {
                                    if (focusUiState.isDiscreteMode) {
                                        DiscreteTaskClock(
                                            state = focusUiState.discreteTaskState,
                                            level = focusUiState.currentTaskLevel,
                                            progress = focusUiState.clockProgress,
                                            onPress = viewModel::startDiscreteTask,
                                            onRelease = viewModel::stopDiscreteTask
                                        )
                                    } else {
                                        TimerDisplay(
                                            focusUiState,
                                            timerSettingsState
                                        )
                                    }

                                }
                                Column(
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,

                                    ) {
                                    if (focusUiState.isDiscreteMode) {
                                        if (focusUiState.discreteTaskState != DiscreteTaskState.IDLE) {
                                            Text(
                                                text = focusUiState.currentTaskLevel.description,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.padding(top = LargeDp)
                                            )
                                        }
                                    } else {
                                        TimerControls(
                                            focusUiState = focusUiState,
                                            onToggleTimer = { viewModel.toggleTimer() },
                                            onResetTimer = { viewModel.resetTimer() },
                                            onBankTime = { viewModel.bankTimer() },
                                            modifier = modifier
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row {
                        // Pomodoro control
                        PomodoroControls(
                            timerSettingsState = timerSettingsState,
                            onIncrementRatio = { viewModel.incrementPomodoroRatio() },
                            onDecrementRatio = { viewModel.decrementPomodorRatio() })

                        // Timer capacity buttons
                        if (!focusUiState.isDiscreteMode) {
                            clockFaceMinutesButtons(
                                currentMinutes = focusUiState.clockFaceMins,
                                onClockFaceMinutesChanged = { newMinutes ->
                                    viewModel.updateClockFaceMinutes(newMinutes)
                                }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    MedalDisplay(
                        medals = focusUiState.medals,
                        modifier = Modifier.height(100.dp)
                    )

                    // Bottom buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = MediumDp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RetroButton(onClick = {
                            viewModel.bankTime()
                        },
                            buttonText = "Bank Medals")

                        RetroButton(
                            onClick = {
                                viewModel.onExit()
                                onNavigateUp()
                            },
                            buttonText = "Quit")
                    } // bottom buttons row
                } // focus mode screen vertical layout column
            } // theme composable
        } // main block (else)
    } // when
}

@Composable
fun DiscreteTaskClock(
    state: DiscreteTaskState,
    level: DiscreteTaskLevel,
    progress: Float,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(level.color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            strokeWidth = 8.dp
            )

        Text(
            text = when (state) {
                DiscreteTaskState.IDLE -> "PRESS ME"
                DiscreteTaskState.COMPLETING -> level.text
                DiscreteTaskState.COMPLETED -> "COMPLETED"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black

        )
    }
}

@Composable
fun clockFaceMinutesButtons(
    currentMinutes: Float,
    onClockFaceMinutesChanged: (Float) -> Unit
) {
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    val buttonValues = listOf(1, 3, 5, 10, 15, 30, 45, 0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // First row of buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            buttonValues.take(4).forEach { minutes ->
                TimedGoalButton(minutes.toFloat(), currentMinutes, onClockFaceMinutesChanged)
            }
        }

        // Second row of buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            buttonValues.takeLast(4).forEach { minutes ->
                TimedGoalButton(
                    minutes.toFloat(), currentMinutes, onClockFaceMinutesChanged,
                    onCustomDurationClick = { if (minutes == 0) showCustomDurationDialog = true }
                )
            }
        }
    }

    if (showCustomDurationDialog) {
        TimeConfigurationDialog(
            initialMinutes = currentMinutes,
            initialSeconds = 0,
            onDismiss = { showCustomDurationDialog = false },
            onConfirm = { minutes, seconds ->
                showCustomDurationDialog = false
                onClockFaceMinutesChanged(minutes + (seconds / 60.0f))
            }
        )
    }
}

@Composable
private fun TimedGoalButton(
    minutes: Float,
    currentMinutes: Float,
    onClockFaceMinutesChanged: (Float) -> Unit,
    onCustomDurationClick: () -> Unit = {}
) {
    val borderColor = if (currentMinutes == minutes) Color(0xFF007FFF) else Color(0xFF20409A)
    val containerColor = if (currentMinutes == minutes) Color(0xFF20409A) else MaterialTheme.colorScheme.background

    Button(
        onClick = {
            if (minutes == 0f) onCustomDurationClick()
            else onClockFaceMinutesChanged(minutes.toFloat())
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        border = BorderStroke(4.dp, borderColor),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(horizontal = 2.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (minutes == 0f) "?" else minutes.toString(),
            modifier = Modifier.padding(0.dp)
        )
    }
}

@Composable
fun EventBox(
    focusUiState: FocusUiState,
    goalUiState: GoalWithSubGoals?,
    eventUiState: Event?,
    modifier: Modifier = Modifier
) {

    //Log.d("FocusModeScreen","printing eventBox. start time: ${focusUiState.startTime}, end time: ${focusUiState.endTime}")
    Column (modifier = modifier.fillMaxWidth()) {
        Text(
            text = focusUiState.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .align(Alignment.Start)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .background(EventBoxColor, shape = RoundedCornerShape(MediumDp))
                .padding(MediumDp)
        ) {
            Text(
                text = goalUiState?.goal?.title ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        Text(
            text = eventUiState?.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .fillMaxWidth()
                .align(Alignment.Start)
        )
    }
}

@Composable
fun PomodoroControls(
    timerSettingsState: TimerSettingsState,
    onIncrementRatio: () -> Unit,
    onDecrementRatio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        IconButton(onClick = onIncrementRatio) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                tint = EventBoxColor,
                contentDescription = "Increase ratio",
                modifier = Modifier.size(FocusIconSize)
            )
        }
        Text(
            if (timerSettingsState.usePomodoro) {
                "${timerSettingsState.workDuration}:${timerSettingsState.restDuration}"
            } else {
                "\u221E:0"
            },
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = onDecrementRatio) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                tint = EventBoxColor,
                contentDescription = "Decrease ratio",
                modifier = Modifier.size(FocusIconSize)
            )
        }
    }
}

@Composable
fun TimerDisplay(
    focusUiState: FocusUiState,
    timerSettingsState: TimerSettingsState,
    modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .size(180.dp)
            .drawBehind {
                val strokeWidth = 6.dp.toPx()
                val size = Size(size.width - strokeWidth, size.height - strokeWidth)

                if (timerSettingsState.usePomodoro) {
                    val totalMinutes = focusUiState.clockFaceMins
                    val cycleLength = timerSettingsState.workDuration + timerSettingsState.restDuration
                    val workMinutes = (totalMinutes * timerSettingsState.workDuration) / cycleLength.toFloat()
                    val workAngle = 360f * (workMinutes / totalMinutes.toFloat())
                    val restAngle = 360f - workAngle

                    drawArc(
                        color = Color(0xFF1A245C),
                        startAngle = -90f,
                        sweepAngle = workAngle,
                        useCenter = true,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = size,
                        style = Fill
                    )

                    // Draw rest period arc
                    drawArc(
                        color = Color(0xFF3F51B5),
                        startAngle = -90f + workAngle,
                        sweepAngle = restAngle,
                        useCenter = true,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = size,
                        style = Fill
                    )
                }

                drawArc(
                    color = if (focusUiState.isRestPeriod) Color(0xFF2196F3)
                            else Color(0xFF008B00),
                    startAngle = -90f,
                    sweepAngle = 360f * focusUiState.clockProgress,
                    useCenter = true,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = size,
                    style = Fill
                )

                // draw outline of clock
                drawCircle(
                    color = Color(0xFF000000),
                    style = Stroke(width = strokeWidth + SmallDp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFFC107),
                    style = Stroke(width = strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // display central timer
        Column (horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(
                text = formatTime(focusUiState.currentTime),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            if (timerSettingsState.usePomodoro) {
                Text(
                    text = if (focusUiState.isRestPeriod) "REST" else "WORK",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        // display quarter time markers
        val quarterTimes: List<Double> = listOf(1.0, 0.25, 0.5, 0.75)

        quarterTimes.forEachIndexed { index, fraction ->
            val time = calculateQuarterTime(focusUiState.clockFaceMins, fraction)
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(
                        when (index) {
                            0 -> Alignment.TopCenter
                            1 -> Alignment.CenterEnd
                            2 -> Alignment.BottomCenter
                            3 -> Alignment.CenterStart
                            else -> Alignment.Center
                        }
                    )
                    .offset(
                        x = when (index) {
                            1 -> (0).dp
                            3 -> (0).dp
                            else -> 0.dp
                        },
                        y = when (index) {
                            0 -> 10.dp
                            2 -> (-10).dp
                            else -> 0.dp
                        }
                    )
            )
        }
    }
}

@Composable
fun TimerControls(
    focusUiState: FocusUiState,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onBankTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        RetroButton(
            onClick = onToggleTimer,
            buttonText =
                if (focusUiState.timerState == TimerState.PAUSED || focusUiState.timerState == TimerState.IDLE) "START"
                else "STOP",
            )
        RetroButton(
            onClick = onResetTimer,
            buttonText = "Reset",
        )
        RetroButton(
            onClick = onBankTime,
            buttonText = "Bank Timer"
        )
    }
}

@Composable
fun RetroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonText: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = if (isPressed) Color(0xFF002200) else Color(0xFF001100)
    val contentColor = Color(0xFF00FF00)
    val borderColor = if (isPressed) Color(0xFF00FFFF) else Color(0xFF00FF00)

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(2.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(4.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = buttonText,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp
        )
    }
}

@Composable
fun MedalDisplay(medals: List<Medal>, modifier: Modifier = Modifier) {
    Text(
        "Time Vault:",
        style = MaterialTheme.typography.headlineMedium,
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        medals.forEach { medal ->
            MedalItem(medal)
            Spacer(modifier = Modifier.width(MediumDp))
        }
    }
}

@Composable
fun MedalItem(medal: Medal) {
    val (backgroundColor, borderColor, textColor) = when (medal.type) {
        MedalType.MINUTES -> Triple(Color(0xFFFFD700), Color(0xFFFF9800), Color.Black)
        MedalType.HOURS -> Triple(Color(0xFF4CAF50), Color(0xFF2E7D32), Color.White)
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(if (medal.type == MedalType.HOURS) RoundedCornerShape(8.dp) else CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = if (medal.type == MedalType.HOURS) RoundedCornerShape(8.dp) else CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${medal.value}${if (medal.type == MedalType.HOURS) "h" else "m"}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

// dialogue box for configurable time

@Composable
fun TimeConfigurationDialog(
    initialMinutes: Float,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Float, Int) -> Unit
) {
    var minutes by remember { mutableStateOf(initialMinutes) }
    var seconds by remember { mutableStateOf(initialSeconds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Timer Duration") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minutes
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Minutes")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (minutes > 0) minutes-- }) {
                            Icon(Icons.Default.Remove, "Decrease minutes")
                        }
                        Text(
                            text = minutes.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        IconButton(onClick = { minutes++ }) {
                            Icon(Icons.Default.Add, "Increase minutes")
                        }
                    }
                }

                Text(":", style = MaterialTheme.typography.headlineMedium)

                // Seconds
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seconds")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (seconds > 0) seconds--
                            else if (minutes > 0) {
                                minutes--
                                seconds = 59
                            }
                        }) {
                            Icon(Icons.Default.Remove, "Decrease seconds")
                        }
                        Text(
                            text = seconds.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        IconButton(onClick = {
                            if (seconds < 59) seconds++
                            else {
                                minutes++
                                seconds = 0
                            }
                        }) {
                            Icon(Icons.Default.Add, "Increase seconds")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(minutes, seconds) }) {
                Text("Set")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



// typography and theming functions

@Composable
fun FocusModeTheme(
    currentTheme: ColorScheme,
    content: @Composable () -> Unit
) {
    val colors = when (currentTheme) {
        ColorScheme.WORK -> darkColorScheme(
            background = FocusColorWork,
            onBackground = FocusColorWorkText,
            tertiary = FocusColorWork
        )
        ColorScheme.REST -> darkColorScheme(
            background = FocusColorRest,
            onBackground = FocusColorRestText,
            tertiary = FocusColorRest
        )
    }

    val typography = FocusModeTypography.copy(
        headlineLarge = FocusModeTypography.headlineLarge.copy(color = colors.onBackground),
        headlineMedium = FocusModeTypography.headlineMedium.copy(color = colors.onBackground),
        headlineSmall = FocusModeTypography.headlineMedium.copy(color = colors.onBackground),
        bodyLarge = FocusModeTypography.bodyLarge.copy(color = colors.onBackground),
        bodyMedium = FocusModeTypography.bodyMedium.copy(color = colors.onBackground),
        bodySmall = FocusModeTypography.bodySmall.copy(color = colors.onBackground),
        labelMedium = FocusModeTypography.labelMedium.copy(color = colors.tertiary)
    )

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}


val FocusModeTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 22.sp,
    ),
    headlineSmall = TextStyle(
        fontSize = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
    ),
    labelMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
)


// time formatting functions

fun formatTime(timeInMillis: Long): String {
    val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (timeInMillis % (1000 * 60)) / 1000
    return String.format("%02d:%02d", minutes, seconds)
}

fun convertMillistoMins(timeinMillis: Long): Int {
    val minutes = (timeinMillis % (1000 * 60 * 60)) / (1000 * 60)
    return minutes.toInt()
}

fun convertMinstoMillis(timeinMins: Int): Long {
    val millis = timeinMins * 60000L
    return millis
}

fun formatDuration(start: LocalTime?, end: LocalTime?): String {
    if (start != null && end != null) {
        val duration = Duration.between(start, end)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        return String.format("%d hours %d mins", hours, minutes)
    }
    return "[missing data]"
}

fun calculateQuarterTime(totalMinutes: Float, fraction: Double): String {
    val minutes = (totalMinutes * fraction).toInt()
    val seconds = ((totalMinutes * fraction * 60) % 60).toInt()
    return if (seconds == 0) {
        minutes.toString()
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

fun formatQuarterTime(time: Double): String {
    return if (time %1 == 0.0) {
        time.toInt().toString()
    } else {
        time.toDouble().toString()
    }
}
