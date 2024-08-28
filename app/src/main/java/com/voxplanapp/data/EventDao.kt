package com.voxplanapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface EventDao {
    @Query("SELECT * FROM Event")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE startDate = :date")
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE id = :eventId")
    suspend fun getEvent(eventId: Int): Event

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

}


class Converters {
    // For LocalTime
    @TypeConverter
    fun fromTimeString(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? = time?.toString()

    // For LocalDate
    @TypeConverter
    fun fromDateLong(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun dateToLong(date: LocalDate?): Long? = date?.toEpochDay()
}
