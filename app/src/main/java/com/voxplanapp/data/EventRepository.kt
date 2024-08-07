package com.voxplanapp.data

import java.time.LocalDate

class EventRepository (private val eventDao: EventDao) {

    fun getAllEvents() = eventDao.getAllEvents()

    fun getEventsForDate(date: LocalDate) = eventDao.getEventsForDate(date)

    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

}