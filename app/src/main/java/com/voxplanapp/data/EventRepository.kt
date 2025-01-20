package com.voxplanapp.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EventRepository (private val eventDao: EventDao) {

    fun getAllEvents() = eventDao.getAllEvents()

    fun getEventsForDate(date: LocalDate) = eventDao.getEventsForDate(date)

    fun getUnscheduledEventsForDate(date: LocalDate): Flow<List<Event>> =
        eventDao.getUnscheduledEventsForDate(date)

    suspend fun updateEventOrder(eventId: Int, newOrder: Int) =
        eventDao.updateEventOrder(eventId, newOrder)

    suspend fun getEvent(eventId: Int) = eventDao.getEvent(eventId)

    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

}