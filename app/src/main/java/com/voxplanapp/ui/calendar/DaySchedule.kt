package com.voxplanapp.ui.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.voxplanapp.ui.constants.EventIconSize
import com.voxplanapp.ui.constants.LargeDp
import com.voxplanapp.ui.constants.MediumDp
import com.voxplanapp.ui.constants.SmallDp
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// ScheduleParams used as CompositionLocal, locally scoped variables for elements
// of these screens
data class ScheduleParams(
    val hourHeight: Dp,
    val startHour: Int,
    val endHour: Int
)

private val LocalScheduleParams = compositionLocalOf<ScheduleParams> {
    error("ScheduleParams not provided")
}

@Composable
fun DaySchedule(
    modifier: Modifier = Modifier,
    viewModel: SchedulerViewModel = viewModel(factory = AppViewModelProvider.Factory)
    ) {
    val hourHeight = 48.dp      // the size of the hourly intervals on screen
    val startHour = 1       // the hour of the day the schedule starts
    val endHour = 24        // when it ends
    // set up the constant params that sub composables need for layout
    val scheduleParams = ScheduleParams(hourHeight, startHour, endHour)

    val scrollToHour = 6    // what time to start the schedule screen scrolled to
    val initialScrollPosition = ((scrollToHour - startHour) * hourHeight.value).toInt() * 2
    val verticalScrollState = rememberScrollState(initialScrollPosition)

    // if the date or the event list change, we need to recompose this screen
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
            CompositionLocalProvider(LocalScheduleParams provides scheduleParams) {
                ScheduleSideBar()
                BasicSchedule(
                    events = events,
                    onEventUpdated = { updatedEvent -> viewModel.updateEvent(updatedEvent) },
                    onEventDeleted = { deletedEvent -> viewModel.deleteEvent(deletedEvent) },
                    modifier = Modifier
                        .weight(1f)
                )
            }
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
fun ScheduleSideBar(modifier: Modifier = Modifier) {
    val scheduleParams = LocalScheduleParams.current

    Column(modifier = modifier) {
        for (hour in scheduleParams.startHour until scheduleParams.endHour ) {
            Box(modifier = Modifier.height(scheduleParams.hourHeight)) {
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
    onEventDeleted: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // recover scheduleParams from CompositionLocal provider
    val scheduleParams = LocalScheduleParams.current
    val startHour = scheduleParams.startHour
    val endHour = scheduleParams.endHour
    val hourHeight = scheduleParams.hourHeight

    val hourLineColor = Color.LightGray
    val halfHourLineColor = Color.LightGray.copy(alpha = 0.5f)

    var selectedEventId by remember { mutableStateOf<Int?>(null)}
    Log.d("BasicSchedule","--------- recomp --------")

    Box(

        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {       // de-select any selected event boxes when background clicked
                detectTapGestures(
                    onTap = { selectedEventId = null }
                )
            }
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

        // selectedEventId is set to the event that's selected on select.
        // isSelected is also provided as an argument to the EventBox.
        // so the EventBox's state management is done here so we can position the icons
        // in this composable.  so let's make sure the icons look good from here.

        // sample event
        events.forEach { event ->

            Log.d("Scheduler","about to load Eventbox for: ${event.id} / goal ${event.goalId}")

            EventBox(
                event = event,
                isSelected = selectedEventId == event.id,
                onSelect = { selectedEventId = event.id
                           Log.d("BasicSchedule","Callback: selectedEventId changed to $selectedEventId" +
                           ", event ${event.title}")},
                onDeselect = {
                    selectedEventId = null
                    Log.d("BasicSchedule","Callback: DE-SELECTING event: selectedEventId = $selectedEventId")
                             },
                onEventUpdated = { updatedEvent ->
                    onEventUpdated(updatedEvent)
                },
                onEventDeleted = { deletedEvent ->
                    onEventDeleted(deletedEvent)
                }
            )

            // if selectedEventId not null, an event has been selected
            // calculate coordinates of event box
            // use coords to calculate position of icon box
            // draw icon box
            Log.d("BasicSchedule","pre-EventActions: selected Event Id = $selectedEventId")
            if (selectedEventId != null) {
                EventActions(
                    event = event,
                    hourHeight = hourHeight,
                    onEventUpdated = { updatedEvent ->
                        onEventUpdated(updatedEvent)
                    },
                    onEventDeleted = { deletedEvent ->
                        onEventDeleted(deletedEvent)
                    }
                )
            }

        }


    }
}

fun calculateEventPosition(
    event: Event,
    hourHeight: Dp,
    startHour: Int
): Pair<Dp, Dp> {
    val eventPadding = 2.dp
    val yPos = hourHeight * (event.startTime.hour - startHour) +
            (hourHeight * event.startTime.minute / 60f) +
            eventPadding
    val xPos = eventPadding
    return Pair(xPos, yPos)
}

@Composable
fun EventBox(
    event: Event,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    isSelected: Boolean,
    onEventUpdated: (Event) -> Unit,
    onEventDeleted: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // recover scheduleParams from CompositionLocal provider
    val scheduleParams = LocalScheduleParams.current
    val startHour = scheduleParams.startHour
    val endHour = scheduleParams.endHour
    val hourHeight = scheduleParams.hourHeight

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val currentIsSelected by remember(isSelected) { mutableStateOf(isSelected) }

    // layout calcs & vars
    val density = LocalDensity.current
    val eventPadding = 2.dp
    val eventDurationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime).toInt()
    // converts minutes into hours as float number.
    val eventDurationHours = eventDurationMinutes / 60f
    val height = hourHeight * eventDurationHours - eventPadding * 2
    val pixelsPerMinute = with(density) { hourHeight.toPx() / 60 }

    Log.d("BasicSchedule", "BUILDING EVENTBOX ${event.title}: ${event.startTime} -> ${event.endTime} isSelected $isSelected")

    val (initialXPos, initialYPos) = remember(event.startTime) {
        calculateEventPosition(event = event, hourHeight = hourHeight, startHour = startHour)
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = eventPadding)
            .height(height)
            .offset(
                y = initialYPos + eventPadding + with(density) { dragOffset.y.toDp() },
                x = initialXPos
            )
            .background(
                if (isDragging) Color.Gray.copy(alpha = 0.5f) else EventBoxColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if ((isDragging) || (isSelected)) 2.dp else 0.dp,
                color = if (isDragging) Color.Gray else if (isSelected) Color.Blue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .pointerInput(event.id, currentIsSelected) {
                detectTapGestures(
                    onTap = {
                        if (isSelected) {
                            Log.d(
                                "BasicSchedule",
                                "onTap: Event ${event.id} currently selected, de-selecting now..."
                            )
                            onDeselect()
                        } else {
                            Log.d(
                                "BasicSchedule",
                                "onTap: Event ${event.id} currently NOT selected, RE-selecting now..."
                            )
                            onSelect()
                        }
                    }
                )
            }
            .pointerInput(event.id, event.startTime) {
                detectDragGestures(
                    onDragStart = {
                        Log.d("BasicSchedule", "Detecting a drag start")
                        if (isSelected) {
                            onDeselect()
                        }      // just in case it was selected
                        isDragging = true
                        Log.d("BasicSchedule", "Drag start for ${event.title}: ${event.startTime}")
                    },
                    onDrag = { pointerInputChange, dragAmount ->
                        Log.d("BasicSchedule", "Detecting a drag")
                        //Log.d("BasicSchedule", "pointer changed: dragOffset: $dragOffset dragAmount: $dragAmount")
                        pointerInputChange.consume()
                        dragOffset += dragAmount
                    },
                    onDragEnd = {
                        isDragging = false
                        val draggedMinutes = (dragOffset.y / pixelsPerMinute).roundToInt()
                        val roundedMinutes = draggedMinutes.roundToNearest15Minutes()
                        val newStartTime = event.startTime.plusMinutes(roundedMinutes.toLong())
                        val duration = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                        val newEndTime = newStartTime.plusMinutes(duration)

                        Log.d(
                            "BasicSchedule",
                            "finished dragging: dragOffset.y ${dragOffset.y} dragged minutes: $draggedMinutes, rounded $roundedMinutes"
                        )
                        Log.d(
                            "BasicSchedule",
                            "dragged event by $draggedMinutes mins, from ${event.startTime}, proposed new start time = $newStartTime"
                        )
                        Log.d(
                            "BasicSchedule",
                            "New start time: $newStartTime, new End time: $newEndTime"
                        )
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

@Composable
fun EventActions(
    event: Event,
    hourHeight: Dp,
    onEventUpdated: (Event) -> Unit,
    onEventDeleted: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // recover scheduleParams from CompositionLocal provider
    val scheduleParams = LocalScheduleParams.current
    val startHour = scheduleParams.startHour
    val endHour = scheduleParams.endHour
    val hourHeight = scheduleParams.hourHeight

    val (xPos, yPos) = calculateEventPosition(event, hourHeight, startHour)
    val iconRowHeight = EventIconSize + MediumDp

    Log.d("BasicSchedule", "EventActions triggered @ $xPos, $yPos")

    Box(
        modifier = Modifier
            .offset(x= xPos, y = yPos - iconRowHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .background(Color.White, RoundedCornerShape(MediumDp))
                .border(2.dp, Color.Gray, RoundedCornerShape(MediumDp))
                .padding(vertical = SmallDp, horizontal = LargeDp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // edit event button
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(EventIconSize)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit event",
                    modifier = Modifier
                        .size(EventIconSize)
                        .padding(0.dp)
                )
            }
            // - time duration
            IconButton(onClick = {
                val duration = Duration.between(event.startTime, event.endTime).toMinutes()
                if (duration > 15) {
                    val newEndTime = event.endTime.minusMinutes(15)
                    onEventUpdated(event.copy(endTime = newEndTime))
                }
            }, modifier = Modifier
                .size(EventIconSize)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Shorten duration",
                    modifier = Modifier
                        .size(EventIconSize)
                        .padding(0.dp)
                )
            }
            // + time duration
            IconButton(
                onClick = {
                    val newEndTime = event.endTime.plusMinutes(15)
                    onEventUpdated(event.copy(endTime = newEndTime))
                },
                modifier = Modifier
                    .size(EventIconSize)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Extend duration",
                    modifier = Modifier
                        .size(EventIconSize)
                        .padding(0.dp)
                )
            }
            // trash it
            IconButton(
                onClick = { onEventDeleted(event) },
                modifier = Modifier
                    .size(EventIconSize)
            ) {
                Icon(
                    Icons.Default.Recycling,
                    contentDescription = "Delete event",
                    modifier = Modifier
                        .size(EventIconSize)
                        .padding(0.dp)

                )
            }
        }
    }
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
