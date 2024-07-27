package com.voxplanapp.ui.calendar

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.voxplanapp.data.Event
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DaySchedule(
    date: LocalDate,
    modifier: Modifier = Modifier
    ) {
    val hourHeight = 48.dp
    val startHour = 1
    val endHour = 24
    val scrollToHour = 6

    val initialScrollPosition = ((scrollToHour - startHour) * hourHeight.value).toInt() * 2
    val verticalScrollState = rememberScrollState(initialScrollPosition)

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
                date = date,
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
    date: LocalDate,
    hourHeight: Dp,
    startHour: Int,
    endHour: Int,
    modifier: Modifier = Modifier
) {
    val hourLineColor = Color.LightGray
    val halfHourLineColor = Color.LightGray.copy(alpha = 0.5f)

    val sampleEvent = Event("1", "New event", LocalTime.of(12, 0), LocalTime.of(14, 0))

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
        // Event boxes will be added here
        EventBox(
            event = sampleEvent,
            hourHeight = hourHeight,
        )
    }
}

@Composable
fun EventBox(
    event: Event,
    hourHeight: Dp,
    modifier: Modifier = Modifier
) {
    val eventPadding = 4.dp
    val eventDurationHours = ChronoUnit.HOURS.between(event.startTime, event.endTime).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = eventPadding)
            .height(hourHeight * eventDurationHours - eventPadding * 2)
            .offset(
                y = hourHeight * event.startTime.hour + eventPadding,
                x = eventPadding
            )
            .background(event.color, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text= event.title,
            color = Color.Black,
            fontSize = 16.sp
        )
    }

}