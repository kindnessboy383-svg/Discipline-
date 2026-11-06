package com.example.ui.screens.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MotivationQuote
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlotEntity
import com.example.ui.components.DisciplineRing
import com.example.ui.components.LiveClockHeader
import com.example.ui.components.QuoteCard
import com.example.ui.components.TimerDialog
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    score: Int,
    streakDays: Int,
    quote: MotivationQuote,
    onRefreshQuote: () -> Unit,
    tasks: List<TaskEntity>,
    timetableSlots: List<TimetableSlotEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onToggleSlot: (TimetableSlotEntity) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToHabits: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTradingTimer by remember { mutableStateOf(false) }

    val completedTasksCount = tasks.count { it.isCompleted }
    val pendingTasksCount = tasks.count { !it.isCompleted }
    val totalTasks = tasks.size
    val completionPercent = if (totalTasks > 0) ((completedTasksCount.toFloat() / totalTasks.toFloat()) * 100).toInt() else 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Live Clock & Date Header
        item {
            LiveClockHeader(streakDays = streakDays)
        }

        // 2. Discipline Score & Progress Ring Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discipline_score_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DisciplineRing(
                        progress = score / 100f,
                        score = score,
                        title = "DISCIPLINE SCORE",
                        subtext = "SCORE OUT OF 100"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Daily Stats Row (Completion %, Completed, Pending)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill(
                            label = "COMPLETION",
                            value = "$completionPercent%",
                            color = NeonLavender
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(DarkBorder)
                        )
                        StatPill(
                            label = "COMPLETED",
                            value = "$completedTasksCount",
                            color = PriorityLow
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(DarkBorder)
                        )
                        StatPill(
                            label = "PENDING",
                            value = "$pendingTasksCount",
                            color = if (pendingTasksCount > 0) PriorityHigh else TextSecondary
                        )
                    }
                }
            }
        }

        // Official DISCIPLINE X Branding Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("official_logo_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_discipline_logo),
                        contentDescription = "Official Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonLavender, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DISCIPLINE",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "X",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = NeonPurple
                                )
                            )
                        }
                        Text(
                            text = "PLAN • DO • ACHIEVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonLavender,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BE BETTER EVERYDAY",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }
            }
        }

        // 3. Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showTradingTimer = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("start_trading_timer_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("30m Focus", fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                FilledTonalButton(
                    onClick = onNavigateToTasks,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("manage_tasks_quick_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonLavender)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Task", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }

        // 4. Daily Motivation Quote
        item {
            QuoteCard(
                quote = quote,
                onRefresh = onRefreshQuote
            )
        }

        // 5. Today's Routine & Timetable Highlights
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY TIMETABLE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                )
                TextButton(
                    onClick = onNavigateToTimetable,
                    modifier = Modifier.testTag("view_all_timetable_btn")
                ) {
                    Text("View Full Schedule", color = NeonLavender, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Show first 3 timetable slots with interactive checkboxes
        items(timetableSlots.take(3)) { slot ->
            TimetableSlotMiniCard(
                slot = slot,
                onToggle = { onToggleSlot(slot) }
            )
        }

        // 6. High Priority Tasks Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S MISSION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                )
                TextButton(
                    onClick = onNavigateToTasks,
                    modifier = Modifier.testTag("view_all_tasks_btn")
                ) {
                    Text("All Tasks", color = NeonLavender, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending tasks today. Set your high-priority goals!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            }
        } else {
            items(tasks.take(4)) { task ->
                TaskMiniCard(
                    task = task,
                    onToggle = { onToggleTask(task) }
                )
            }
        }
    }

    if (showTradingTimer) {
        TimerDialog(
            initialMinutes = 30,
            title = "Trading Focused Learning",
            subtitle = "30-Minute Chart Study & Risk Management Sprint",
            onDismiss = { showTradingTimer = false },
            onCompleted = {
                // Find and mark trading timetable slot complete
                timetableSlots.firstOrNull { it.category == "Trading" }?.let { onToggleSlot(it) }
            }
        )
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextTertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
fun TimetableSlotMiniCard(
    slot: TimetableSlotEntity,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, if (slot.isCompletedToday) PriorityLow.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(14.dp)
            .testTag("slot_mini_${slot.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = slot.isCompletedToday,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = PriorityLow,
                uncheckedColor = DarkBorder,
                checkmarkColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = slot.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (slot.isCompletedToday) TextTertiary else TextPrimary
                )
            )
            Text(
                text = "${slot.startTime} - ${slot.endTime} • ${slot.category}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NeonLavender,
                    fontSize = 11.sp
                )
            )
        }
        if (slot.focusDurationMinutes > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonPurple.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${slot.focusDurationMinutes}m focus",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonLavender,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun TaskMiniCard(
    task: TaskEntity,
    onToggle: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "HIGH" -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        else -> PriorityLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, if (task.isCompleted) PriorityLow.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(14.dp)
            .testTag("task_mini_${task.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = PriorityLow,
                uncheckedColor = DarkBorder,
                checkmarkColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) TextTertiary else TextPrimary
                )
            )
            Text(
                text = "${task.category} • ${task.priority} Priority",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = priorityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
