package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "Discipline", // "Fitness", "Study", "Mindset", "Health", "Trading"
    val targetDaysPerWeek: Int = 7,
    val colorHex: String = "#C084FC",
    val iconKey: String = "bolt", // "fitness", "book", "alarm", "school", "chart", "bolt"
    val isSystemHabit: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "dateString"])
data class HabitLogEntity(
    val habitId: Long,
    val dateString: String, // "yyyy-MM-dd"
    val completed: Boolean = true,
    val timestampMillis: Long = System.currentTimeMillis()
)
