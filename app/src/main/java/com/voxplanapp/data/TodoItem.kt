package com.voxplanapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var parentId: Int? = null,
    var order: Int = 0,
    var notes: String? = null,
    var isDone: Boolean = false,
    var preferredTime: LocalTime? = null,
    var estDurationMins: Int? = null,
    var frequency: RecurrenceType = RecurrenceType.NONE,
    var expanded: Boolean = true,
    var completedDate: LocalDate? = null
)

// remove isDone, add completedDate.
