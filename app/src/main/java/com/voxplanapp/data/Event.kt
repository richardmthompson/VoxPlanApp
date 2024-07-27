package com.voxplanapp.data

import androidx.compose.ui.graphics.Color
import com.voxplanapp.ui.constants.EventBoxColor
import java.time.LocalTime

data class Event(
    val id: String = "",
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Color = EventBoxColor
)
