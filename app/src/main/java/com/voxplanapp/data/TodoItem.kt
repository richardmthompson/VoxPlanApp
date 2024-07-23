package com.voxplanapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var isDone: Boolean = false,
    var parentId: Int? = null,
    var order: Int = 0,
    var notes: String? = null,
)
