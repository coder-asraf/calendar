package com.example.nepalcalendar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM calendar_events ORDER BY bsYear ASC, bsMonth ASC, bsDay ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM calendar_events WHERE bsYear = :year AND bsMonth = :month ORDER BY bsDay ASC")
    fun getEventsForMonth(year: Int, month: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM calendar_events WHERE bsYear = :year AND bsMonth = :month AND bsDay = :day")
    fun getEventsForDate(year: Int, month: Int, day: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM calendar_events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAllEvents()
}
