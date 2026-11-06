package com.example.ui.screens.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitTrackerScreen(
    habits: List<HabitEntity>,
    todayLogs: List<HabitLogEntity>,
    onToggleHabit: (Long) -> Unit,
    onAddHabit: (String, String, String, String, String) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val completedTodayCount = habits.count { h -> todayLogs.any { it.habitId == h.id && it.completed } }
    val totalCount = habits.size
    val progress = if (totalCount > 0) completedTodayCount.toFloat() / totalCount.toFloat() else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header Card with Habit Stats
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habits_header_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HABIT PROTOCOLS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Daily non-negotiables that forge identity.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.2f))
                                .border(1.dp, NeonLavender, CircleShape)
                                .testTag("add_habit_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = NeonLavender)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Discipline Completion",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                        )
                        Text(
                            text = "$completedTodayCount of $totalCount Done (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (progress == 1f) PriorityLow else NeonLavender,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress == 1f) PriorityLow else NeonLavender,
                        trackColor = DarkBorder
                    )
                }
            }
        }

        // List of Habits
        items(habits) { habit ->
            val isDoneToday = todayLogs.any { it.habitId == habit.id && it.completed }
            HabitCard(
                habit = habit,
                isDoneToday = isDoneToday,
                onToggle = { onToggleHabit(habit.id) },
                onDelete = { onDeleteHabit(habit.id) }
            )
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, desc, cat, color, icon ->
                onAddHabit(name, desc, cat, color, icon)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    isDoneToday: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habitColor = when (habit.category) {
        "Fitness" -> PriorityHigh
        "Trading" -> PriorityMedium
        "Study" -> TechCyan
        "Mindset", "Reading" -> NeonLavender
        else -> NeonPurple
    }

    val iconVector = when (habit.iconKey) {
        "alarm" -> Icons.Default.Alarm
        "fitness" -> Icons.Default.FitnessCenter
        "school" -> Icons.Default.School
        "chart" -> Icons.Default.TrendingUp
        "book" -> Icons.Default.AutoStories
        else -> Icons.Default.Bolt
    }

    val cardBorderColor by animateColorAsState(
        targetValue = if (isDoneToday) PriorityLow.copy(alpha = 0.5f) else DarkBorder,
        label = "habit_border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.15f))
                        .border(1.dp, habitColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = iconVector, contentDescription = null, tint = habitColor, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDoneToday) PriorityLow else TextPrimary
                        )
                    )
                    Text(
                        text = if (habit.description.isNotBlank()) habit.description else "${habit.category} Protocol",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }

                // Check-in button
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDoneToday) PriorityLow else DarkSurfaceElevated,
                        contentColor = if (isDoneToday) Color.Black else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("toggle_habit_${habit.id}")
                ) {
                    Icon(
                        imageVector = if (isDoneToday) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDoneToday) "DONE" else "EXECUTE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Past 7 Days Visual Matrix (Simulated / Realistic week matrix)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                daysOfWeek.forEachIndexed { index, day ->
                    val isPastDone = (index < 5 || (index == 6 && isDoneToday))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isPastDone) habitColor else DarkBorder)
                        )
                    }
                }

                if (!habit.isSystemHabit) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Discipline") }
    val categories = listOf("Discipline", "Fitness", "Study", "Trading", "Mindset", "Health")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Habit Protocol", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name (e.g. Cold Shower)") },
                    modifier = Modifier.fillMaxWidth().testTag("habit_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Protocol / Target") },
                    modifier = Modifier.fillMaxWidth().testTag("habit_desc_input")
                )
                Text("Category", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, description, category, "#C084FC", "bolt")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLavender),
                modifier = Modifier.testTag("save_custom_habit_btn")
            ) {
                Text("Save Protocol", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
