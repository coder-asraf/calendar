package com.example.nepalcalendar.data.repository

import com.example.nepalcalendar.data.local.EventDao
import com.example.nepalcalendar.data.local.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getEventsForMonth(year: Int, month: Int): Flow<List<EventEntity>> {
        return eventDao.getEventsForMonth(year, month)
    }

    fun getEventsForDate(year: Int, month: Int, day: Int): Flow<List<EventEntity>> {
        return eventDao.getEventsForDate(year, month, day)
    }

    fun searchEvents(query: String): Flow<List<EventEntity>> {
        return eventDao.searchEvents(query)
    }

    suspend fun insertEvent(event: EventEntity): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(id)
    }

    suspend fun clearAll() {
        eventDao.clearAllEvents()
    }
}
