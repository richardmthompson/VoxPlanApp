package com.voxplanapp.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voxplanapp.data.Event
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.ui.goals.DurationSelector
import java.time.LocalDate
import java.time.LocalTime

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickScheduleScreen(
    goalId: Int,
    onSchedule: (Event) -> Unit,
    onDismiss: () -> Unit
) {
    var startTimeState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute
    )
    var duration by remember { mutableStateOf(60) }  // Default 1 hour in minutes
    var endTimeState = rememberTimePickerState(
        initialHour = LocalTime.now().plusMinutes(duration.toLong()).hour,
        initialMinute = LocalTime.now().plusMinutes(duration.toLong()).minute
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Quick Schedule: ${goal.goal.title}", style = MaterialTheme.typography.headlineMedium)

        Text("Start Time")
        TimePicker(state = startTimeState)

        DurationSelector(
            duration = duration,
            onDurationChanged = {
                duration = it
                val newEndTime = LocalTime.of(startTimeState.hour, startTimeState.minute).plusMinutes(it.toLong())
                endTimeState = rememberTimePickerState(newEndTime.hour, newEndTime.minute)
            }
        )

        Text("End Time")
        TimePicker(state = endTimeState)

        Button(onClick = {
            val event = Event(
                goalId = goal.goal.id,
                title = goal.goal.title,
                startTime = LocalTime.of(startTimeState.hour, startTimeState.minute),
                endTime = LocalTime.of(endTimeState.hour, endTimeState.minute),
                startDate = LocalDate.now(),
                // ... other necessary fields
            )
            onSchedule(event)
        }) {
            Text("Schedule")
        }

        Button(onClick = onDismiss) {
            Text("Cancel")
        }
    }
}

 */