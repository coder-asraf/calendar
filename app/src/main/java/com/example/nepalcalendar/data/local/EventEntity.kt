package com.example.nepalcalendar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val bsYear: Int,
    val bsMonth: Int,
    val bsDay: Int,
    val adDateString: String, // YYYY-MM-DD
    val category: String = "Personal", // Personal, Birthday, Anniversary, Meeting, Deadline, Holiday
    val colorHex: String = "#C91D34",
    val isAllDay: Boolean = true,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
