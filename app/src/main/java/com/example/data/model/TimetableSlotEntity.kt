package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderIndex: Int,
    val title: String,
    val startTime: String, // "05:00" in 24-hr format
    val endTime: String,   // "06:00" in 24-hr format
    val category: String,  // "Routine", "Fitness", "School", "Study", "Trading", "Reading"
    val note: String = "",
    val focusDurationMinutes: Int = 0, // e.g., 30 for trading, 15 for reading
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String = "", // "yyyy-MM-dd"
    val isEnabled: Boolean = true
)
