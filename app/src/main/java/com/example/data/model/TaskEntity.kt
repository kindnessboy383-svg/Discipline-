package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val category: String = "General", // "School", "Study", "Gym", "Trading", "Reading", "Personal", "Routine"
    val dueDateMillis: Long,
    val reminderMinutesBefore: Int = 10, // 0, 10, 30, 60, -1 (none)
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val subtasksJson: String = "", // Stored as JSON or serialized string
    val isOverdueFollowupSent: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
