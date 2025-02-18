package com.voxplanapp.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EventRepository (private val eventDao: EventDao) {

    fun getDailiesForDate(date: LocalDate) = eventDao.getDailiesForDate(date)

    fun getEventsForDate(date: LocalDate) = eventDao.getEventsForDate(date)

    fun getEventsWithParentId(parentId: Int): Flow<List<Event>> =
        eventDao.getEventsWithParentId(parentId)

    fun getScheduledBlocksForDate(date: LocalDate) = eventDao.getScheduledBlocksForDate(date)

    fun getScheduledBlocksForDaily(dailyId: Int) = eventDao.getScheduledBlocksForDaily(dailyId)

    suspend fun updateEventOrder(eventId: Int, newOrder: Int) =
        eventDao.updateEventOrder(eventId, newOrder)

    suspend fun getEvent(eventId: Int) = eventDao.getEvent(eventId)

    suspend fun insertEvent(event: Event): Int {
        return eventDao.insertEvent(event).toInt()      // we convert the Long to Int as that's what our navigation expects for event ID
    }

    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    suspend fun deleteEvent(eventId: Int) = eventDao.deleteEvent(eventId)

}