package com.voxplanapp.data

// QuotaEntity.kt
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TodoItem::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Quota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val goalId: Int,
    val dailyMinutes: Int,
    val activeDays: String // "1111100" for Mon-Fri active days
)
