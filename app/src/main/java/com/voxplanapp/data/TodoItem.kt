package com.voxplanapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var isDone: Boolean = false,
    var parentId: Int? = null,
    var order: Int = 0,
    var notes: String? = null,
    var preferredTime: LocalTime? = null,
    var estDurationMins: Int? = null,
    var frequency: RecurrenceType = RecurrenceType.NONE,
    var expanded: Boolean = true
)
