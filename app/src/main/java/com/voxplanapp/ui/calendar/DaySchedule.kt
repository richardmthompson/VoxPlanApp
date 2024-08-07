package com.voxplanapp.ui.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.data.Event
import com.voxplanapp.ui.constants.EventBoxColor
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun DaySchedule(
    modifier: Modifier = Modifier,
    viewModel: SchedulerViewModel = viewModel(factory = AppViewModelProvider.Factory)
    ) {
    val hourHeight = 48.dp
    val startHour = 1
    val endHour = 24
    val scrollToHour = 6

    val initialScrollPosition = ((scrollToHour - startHour) * hourHeight.value).toInt() * 2
    val verticalScrollState = rememberScrollState(initialScrollPosition)

    val date by viewModel.currentDate.collectAsState()
    val events by viewModel.eventsForCurrentDate.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Yellow.copy(alpha = 0.1f))
    ) {
        DayHeader(date = date)
        Row(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(verticalScrollState)
        ) {
            ScheduleSideBar(
                hourHeight = hourHeight,
                startHour = startHour,
                endHour = endHour,
            )
            BasicSchedule(
                events = events,
                onEventUpdated = { updatedEvent -> viewModel.updateEvent(updatedEvent) },
                hourHeight = hourHeight,
                startHour = startHour,
                endHour = endHour,
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
fun DayHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Text(
        text = date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ScheduleSideBar(
    hourHeight: Dp,
    startHour: Int,
    endHour: Int,
    modifier: Modifier = Modifier
    ) {
    Column(modifier = modifier
    ) {
        for (hour in startHour until endHour) {
            Box(modifier = Modifier.height(hourHeight)) {
                Text(
                    text = LocalTime.of(hour % 24, 0).format(DateTimeFormatter.ofPattern("h a")),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BasicSchedule(
    events: List<Event>,
    onEventUpdated: (Event) -> Unit,
    hourHeight: Dp,
    startHour: Int,
    endHour: Int,
    modifier: Modifier = Modifier
) {
    val hourLineColor = Color.LightGray
    val halfHourLineColor = Color.LightGray.copy(alpha = 0.5f)

    var draggedEventId by remember { mutableStateOf<Int?>(null) }

    //Log.d("BasicSchedule", "dragged Event: $draggedEventId")

    Box(

        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val hourLineStrokeWidth = 1.dp.toPx()
                val halfHourLineStrokeWidth = 0.5.dp.toPx()

                for (hour in startHour until endHour) {
                    val y = (hour - startHour) * hourHeight.toPx()

                    // Draw hour lines
                    drawLine(
                        color = hourLineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = hourLineStrokeWidth
                    )

                    // Draw half-hour lines
                    if (hour < endHour - 1) {
                        val halfHourY = y + (hourHeight.toPx() / 2)
                        drawLine(
                            color = halfHourLineColor,
                            start = Offset(0f, halfHourY),
                            end = Offset(size.width, halfHourY),
                            strokeWidth = halfHourLineStrokeWidth
                        )
                    }
                }
            }
    ) {

        Log.d("Scheduler","about to list events.. here we go.....")

        // sample event
        events.forEach { event ->

            var isDragging by remember { mutableStateOf(false) }
            var dragOffset by remember { mutableStateOf(Offset.Zero) }

            Log.d("Scheduler","event: ${event.id} references goal ${event.goalId}")

            EventBox(
                event = event,
                hourHeight = hourHeight,
                startHour = startHour,
                endHour = endHour,
                isDragging = event.id == draggedEventId,
                onDragStart = {
                    isDragging = true
                    draggedEventId = event.id
                },
                onDragEnd = {
                    isDragging = false
                    draggedEventId = null
                },
                onEventUpdated = { updatedEvent ->
                    onEventUpdated(updatedEvent)
                }
            )
        }
    }
}

@Composable
fun EventBox(
    event: Event,
    hourHeight: Dp,
    startHour: Int,
    endHour: Int,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onEventUpdated: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val eventPadding = 2.dp
    val eventDurationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime).toInt()
    // converts minutes into hours as float number.
    val eventDurationHours = eventDurationMinutes / 60f

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val pixelsToDpScale = 2.5
    val density = LocalDensity.current
    val pixelsPerMinute = with(density) { hourHeight.toPx() / 60 }

    Log.d("BasicSchedule", "EVENT ${event.title}: ${event.startTime} -> ${event.endTime}")

    // layout calcs
    val height = hourHeight * eventDurationHours - eventPadding * 2
    val initialYPos by remember(event.startTime) {
        mutableStateOf(
            hourHeight * (event.startTime.hour - startHour) +
            // the minutes
            (hourHeight * event.startTime.minute / 60f))
    }
    val xPos = eventPadding + with(density) { dragOffset.x.toDp() }

    //Log.d("BasicSchedule", "Calculated initialYPos for ${event.title}: $initialYPos")
    //Log.d("BasicSchedule", "eventDurMins: $eventDurationMinutes, eventDurHrs: $eventDurationHours, hourHeight: $hourHeight, height of box: $height, startHour: $startHour, endHour: $endHour")

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = eventPadding)
            .height(height)
            .offset(
                y = initialYPos + eventPadding + with(density) { dragOffset.y.toDp() },
                x = xPos
            )
            .background(
                if (isDragging) Color.Gray.copy(alpha = 0.5f) else EventBoxColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isDragging) 2.dp else 0.dp,
                color = if (isDragging) Color.Gray else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .pointerInput(event.id, event.startTime) {
                detectDragGestures(
                    onDragStart = {
                        onDragStart()
                        Log.d("BasicSchedule", "Drag start for ${event.title}: ${event.startTime}")
                    },
                    onDrag = { pointerInputChange, dragAmount ->
                        //Log.d("BasicSchedule", "pointer changed: dragOffset: $dragOffset dragAmount: $dragAmount")
                        pointerInputChange.consume()
                        dragOffset += dragAmount
                    },
                    onDragEnd = {
                        onDragEnd()

                        val draggedMinutes = (dragOffset.y / pixelsPerMinute).roundToInt()
                        val roundedMinutes = draggedMinutes.roundToNearest15Minutes()
                        Log.d("BasicSchedule", "finished dragging: dragOffset.y ${dragOffset.y} dragged minutes: $draggedMinutes, rounded $roundedMinutes")

                        val newStartTime = event.startTime.plusMinutes(roundedMinutes.toLong())

                        Log.d(
                            "BasicSchedule",
                            "dragged event by $draggedMinutes mins, from ${event.startTime}, proposed new start time = $newStartTime"
                        )
                        val duration = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                        val newEndTime = newStartTime.plusMinutes(duration)

                        Log.d("EventBox", "New start time: $newStartTime, new End time: $newEndTime")
                        onEventUpdated(event.copy(startTime = newStartTime, endTime = newEndTime))
                        dragOffset = Offset.Zero

                    }
                )
            }
    ) {
        Text(
            text = event.title,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
    //Log.d("Scheduler", "EventBox composable complete.  box should be drawN!")
}

fun Int.roundToNearest15Minutes(): Int {
    val isNegative = (this < 0)

    val neutralThis = this.absoluteValue
    val hours = neutralThis / 60
    val mod60 = neutralThis % 60                   // get minute remainder after hours

    val roundedMins = ((mod60 +7)/15) * 15      // round the minutes

    Log.d("BasicSchedule", "this = $this:: hours = $hours / mins = $mod60 / roundedmins = $roundedMins total = ${(hours * 60)+roundedMins}")

    val total = (hours*60) + roundedMins
    return if (isNegative) -(total) else total

}
