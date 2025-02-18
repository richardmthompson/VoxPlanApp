package com.voxplanapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val order: Int = 0,
    // existing fields...
    val quotaDuration: Int? = null,     // in minutes, from the quota
    val scheduledDuration: Int? = null,  // in minutes, calculated from schedule times
    val completedDuration: Int? = null,   // in minutes, from time bank entries
    // id of the parent daily.  Dailies are all parents, and children are scheduled events.
    // thus, this field identifies whether this is a Daily or a Scheduled Event - as we share this data structure for both.
    val parentDailyId: Int? = null
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}
