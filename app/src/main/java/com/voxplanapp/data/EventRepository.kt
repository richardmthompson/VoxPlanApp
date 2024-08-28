package com.voxplanapp.data

import java.time.LocalDate

class EventRepository (private val eventDao: EventDao) {

    fun getAllEvents() = eventDao.getAllEvents()

    fun getEventsForDate(date: LocalDate) = eventDao.getEventsForDate(date)

    suspend fun getEvent(eventId: Int) = eventDao.getEvent(eventId)

    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

}