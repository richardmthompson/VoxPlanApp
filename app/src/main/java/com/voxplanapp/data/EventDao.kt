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

    /** Dailies */
    // get all dailies for a specfic date
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND parentDailyId IS NULL
        ORDER BY `order`
    """)
    fun getDailiesForDate(date: LocalDate): Flow<List<Event>>

    // get all scheduled blocks for a date
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND parentDailyId IS NOT NULL
        ORDER BY startTime
    """)
    fun getScheduledBlocksForDate(date: LocalDate): Flow<List<Event>>

    // get scheduled blocks for a specific daily
    @Query("SELECT * FROM Event WHERE parentDailyId = :dailyId")
    fun getScheduledBlocksForDaily(dailyId: Int): Flow<List<Event>>

    // Basic operations
    @Query("SELECT * FROM Event")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE startDate = :date")
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE id = :eventId")
    suspend fun getEvent(eventId: Int): Event

    @Query("SELECT * FROM Event WHERE parentDailyId = :parentId")
    fun getEventsWithParentId(parentId: Int): Flow<List<Event>>

    @Query("UPDATE Event SET `order` = :newOrder WHERE id = :id")
    suspend fun updateEventOrder(id: Int, newOrder: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long     // we define a return value of Long which returns event id

    @Update
    suspend fun updateEvent(event: Event)

    @Query("DELETE FROM Event WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Int)
}
