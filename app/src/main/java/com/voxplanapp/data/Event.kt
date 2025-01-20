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
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val startDate: LocalDate,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int? = 0,
    val recurrenceEndDate: LocalDate? = null,
    val color: Int? = null,
    val scheduled: Boolean = false,
    val order: Int = 0
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}
