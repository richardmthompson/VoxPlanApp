package com.voxplanapp.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.voxplanapp.ui.constants.EventBoxColor
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startDate: LocalDate,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int? = 0,
    val recurrenceEndDate: LocalDate? = null,

    // todo: Add color to event
    val color: Int? = null
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}
