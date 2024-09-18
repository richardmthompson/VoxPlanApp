package com.voxplanapp.ui.focusmode

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.voxplanapp.ui.constants.LargeDp
import com.voxplanapp.ui.constants.MediumDp
import com.voxplanapp.ui.constants.SmallDp
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.times


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
                            .border(2.dp, Color.Green, RoundedCornerShape(MediumDp))
                            .padding(MediumDp)
                    ) {
                        Text(
                            text = "FOCUS MODE",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Log.d("FocusModeScreen", "goalUiState: $goalUiState")
                    Log.d("FocusModeScreen", "goal title: ${goalUiState?.goal?.title}")

                    // task headline
                    Text(
                        text = "\"${goalUiState?.goal?.title}\"",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // event box indicating length of current event
                    EventBox(
                        focusUiState = focusUiState,
                        goalUiState = goalUiState,
                        eventUiState = eventUiState,
                        modifier = modifier
                    )

                    /*
                    Spacer(modifier = Modifier.height(20.dp))

                    PomodoroSettings(
                        focusUiState = focusUiState,
                        eventUiState = eventUiState,
                        timerSettingsState = timerSettingsState,
                        onSettingsChange = { /* todo */ },
                        modifier = modifier
                        )

                     */

                    Spacer(modifier = Modifier.height(20.dp))
                    // timer headline
                    Text(
                        text = "Time Bank",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer clock & buttons
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        //Timer display
                        Box(modifier = Modifier
                            .weight(0.6f)
                            .padding(start = LargeDp)
                        ) {
                            TimerDisplay(focusUiState)
                        }
                        Column(modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,

                        ) {
                            TimerControls(
                                focusUiState = focusUiState,
                                onToggleTimer = { viewModel.toggleTimer() },
                                onResetTimer = { viewModel.resetTimer() },
                                onBankTime = { viewModel.bankTimer() },
                                modifier = modifier
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // clock face minutes boxes to change total clockface minutes
                    clockFaceMinutesButtons(
                        currentMinutes = focusUiState.clockFaceMins,
                        onClockFaceMinutesChanged = { newMinutes ->
                            viewModel.updateClockFaceMinutes(newMinutes)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    MedalDisplay(medals = focusUiState.medals)

                    Spacer(modifier = Modifier.weight(1f))

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

                        RetroButton(onClick = onNavigateUp,
                            buttonText = "Quit")
                    }
                }
            }
        } // main block (else)
    } // when
}

@Composable
fun clockFaceMinutesButtons(
    currentMinutes: Int,
    onClockFaceMinutesChanged: (Int) -> Unit
) {
    val buttonValues = listOf(3, 5, 10, 15, 30, 45)

    Text (
        text = "Timer Capacity:",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        buttonValues.forEach { minutes ->
            val borderColor = if (currentMinutes == minutes) Color(0xFF007FFF)
            else Color(0xFF20409A)

            val containerColor = if (currentMinutes == minutes) Color(0xFF20409A)
                else MaterialTheme.colorScheme.background

            Button(
                onClick = { onClockFaceMinutesChanged(minutes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor
                ),
                border = BorderStroke(4.dp, borderColor),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(horizontal= 0.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                Text(text = minutes.toString(), modifier = Modifier.padding(0.dp))
            }
        }
    }
}

@Composable
fun EventBox(
    focusUiState: FocusUiState,
    goalUiState: GoalWithSubGoals?,
    eventUiState: Event?,
    modifier: Modifier = Modifier
) {

    Log.d("FocusModeScreen","printing eventBox. start time: ${focusUiState.startTime}, end time: ${focusUiState.endTime}")
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
fun PomodoroSettings(
    focusUiState: FocusUiState,
    eventUiState: Event?,
    timerSettingsState: TimerSettingsState,
    onSettingsChange: (TimerSettingsState) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("FocusModeScreen","")

    Column (horizontalAlignment = Alignment.Start){
        Text("Work / Rest Intervals for Focus Timer:",
            style = MaterialTheme.typography.bodyMedium)
        Text("Total: ${formatDuration(focusUiState.startTime, eventUiState?.endTime)}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Row {
            Text("Work period:",
                style = MaterialTheme.typography.bodyMedium,
            )
            TextField(
                value = timerSettingsState.workDuration.toString(),
                onValueChange = {
                    onSettingsChange(
                        timerSettingsState.copy(workDuration = it.toIntOrNull() ?: 0)
                    )
                },
                modifier = modifier.width(50.dp)
            )
            Text("mins",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row {
            Text("Rest period:",
                style = MaterialTheme.typography.bodyMedium,
            )
            TextField(
                value = timerSettingsState.restDuration.toString(),
                onValueChange = {
                    onSettingsChange(
                        timerSettingsState.copy(restDuration = it.toIntOrNull() ?: 0)
                    )
                },
                modifier = modifier.width(50.dp)
            )
            Text("mins",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row {
            Text("Blocks",
                style = MaterialTheme.typography.bodyMedium,
            )
            TextField(
                value = timerSettingsState.numWorkBlocks?.toString() ?: "",
                onValueChange = {
                    onSettingsChange(timerSettingsState.copy(numWorkBlocks = it.toIntOrNull()))
                },
                modifier = modifier.width(50.dp)
            )
        }
    }
}


@Composable
fun TimerDisplay(focusUiState: FocusUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                val size = Size(size.width - strokeWidth, size.height - strokeWidth)

                drawArc(
                    color = Color(0xFF008B00), // dark green
                    startAngle = -90f,
                    sweepAngle = 360f * focusUiState.clockProgress,
                    useCenter = true,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = size,
                    style = Fill
                )

                drawCircle(
                    color = Color.Gray,
                    style = Stroke(width = strokeWidth)
                )
            }
    ) {
        // display central timer
        Text(
            text = formatTime(focusUiState.currentTime),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.Center)
        )
        // display quarter time markers
        val quarterTimes: List<Double> = listOf(1.0, 0.25, 0.5, 0.75)

        quarterTimes.forEachIndexed { index, fraction ->
            val time = calculateQuarterTime(focusUiState.clockFaceMins, fraction)
            Text(
                text = formatQuarterTime(time),
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
            buttonText = "Bank Minutes"
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Yellow, CircleShape)
                    .border(2.dp, Color(0xFFB38106), CircleShape),
            ) {
                Text(
                    text = "${medal.minutes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(MediumDp))
        }
    }
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

fun formatDuration(start: LocalTime?, end: LocalTime?): String {
    if (start != null && end != null) {
        val duration = Duration.between(start, end)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        return String.format("%d hours %d mins", hours, minutes)
    }
    return "[missing data]"
}

fun calculateQuarterTime(totalMinutes: Int, fraction: Double): Double {
    return totalMinutes.toDouble() * fraction
}

fun formatQuarterTime(time: Double): String {
    return if (time %1 == 0.0) {
        time.toInt().toString()
    } else {
        time.toDouble().toString()
    }
}
