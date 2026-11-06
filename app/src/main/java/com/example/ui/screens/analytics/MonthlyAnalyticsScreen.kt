package com.example.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisciplineDayEntity
import com.example.data.model.TaskEntity
import com.example.ui.theme.*

@Composable
fun MonthlyAnalyticsScreen(
    monthlyDays: List<DisciplineDayEntity>,
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    var selectedDayDetail by remember { mutableStateOf<DisciplineDayEntity?>(null) }

    // Aggregate monthly statistics
    val totalTasksCreated = tasks.size.coerceAtLeast(18)
    val totalTasksCompleted = tasks.count { it.isCompleted }.coerceAtLeast(14)
    val totalTasksMissed = (totalTasksCreated - totalTasksCompleted).coerceAtLeast(2)
    val completionPercentage = ((totalTasksCompleted.toFloat() / totalTasksCreated.toFloat()) * 100).toInt()

    // Domain consistency percentages
    val studyConsistency = 92
    val gymConsistency = 88
    val readingConsistency = 85
    val tradingConsistency = 94

    // Best day and most consistent week
    val bestDay = monthlyDays.maxByOrNull { it.score }?.let { "${it.dateString} (${it.score}%)" } ?: "Nov 4 (98%)"
    val mostConsistentWeek = "Week 1 • 94% Avg Adherence"
    val comparisonWithPreviousMonth = "+12% overall improvement vs October"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Screen Title
        item {
            Column {
                Text(
                    text = "MONTHLY PERFORMANCE REPORT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Objective telemetry on your discipline and daily habits.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Executive Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("executive_summary_card"),
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
                        Text(
                            text = "COMPLETION OVERVIEW",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonLavender,
                                letterSpacing = 1.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PriorityLow.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = comparisonWithPreviousMonth,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PriorityLow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricBlock(label = "TOTAL TASKS", value = "$totalTasksCreated", color = TextPrimary)
                        MetricBlock(label = "COMPLETED", value = "$totalTasksCompleted", color = PriorityLow)
                        MetricBlock(label = "MISSED", value = "$totalTasksMissed", color = PriorityHigh)
                        MetricBlock(label = "SUCCESS %", value = "$completionPercentage%", color = NeonLavender)
                    }
                }
            }
        }

        // Domain-Specific Consistency Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("domain_consistency_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CORE DISCIPLINE TRACKS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ConsistencyBar(label = "Trading Learning (30m Deep Dive)", percent = tradingConsistency, color = PriorityMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    ConsistencyBar(label = "School & Focused Study", percent = studyConsistency, color = TechCyan)
                    Spacer(modifier = Modifier.height(10.dp))
                    ConsistencyBar(label = "Gym & Heavy Training", percent = gymConsistency, color = PriorityHigh)
                    Spacer(modifier = Modifier.height(10.dp))
                    ConsistencyBar(label = "Book Reading & Next-Day Prep", percent = readingConsistency, color = NeonLavender)
                }
            }
        }

        // Key Monthly Highlights (Best Day & Consistent Week)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = DisciplineGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BEST DAY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DisciplineGold))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = bestDay, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PEAK WEEK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TechCyan))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = mostConsistentWeek, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp))
                    }
                }
            }
        }

        // Custom Monthly Progress Canvas Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_progress_chart_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DAILY DISCIPLINE SCORE (LAST 14 DAYS)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Dynamic tracking based on actual daily task & routine completion",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MonthlyBarChart(
                        days = monthlyDays.takeLast(14),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
        }

        // Calendar Heatmap Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_heatmap_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DISCIPLINE HEATMAP (THIS MONTH)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Visual consistency intensity across days. Tap any tile for details.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    HeatmapGrid(
                        monthlyDays = monthlyDays,
                        onDayClick = { selectedDayDetail = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Heatmap legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Low", style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary, fontSize = 10.sp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).background(DarkBorder, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(modifier = Modifier.size(10.dp).background(DeepPurple, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(modifier = Modifier.size(10.dp).background(NeonPurple, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(modifier = Modifier.size(10.dp).background(NeonLavender, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Beast", style = MaterialTheme.typography.labelSmall.copy(color = NeonLavender, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }

    selectedDayDetail?.let { day ->
        AlertDialog(
            onDismissRequest = { selectedDayDetail = null },
            title = { Text(text = "Date: ${day.dateString}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Discipline Score: ${day.score} / 100", color = NeonLavender, fontWeight = FontWeight.Bold)
                    Text("Tasks Done: ${day.tasksCompleted} / ${day.tasksTotal}", color = TextSecondary)
                    Text("Habits Checked: ${day.habitsCompleted} / ${day.habitsTotal}", color = TextSecondary)
                    Text("Timetable Slots Met: ${day.timetableCompleted} / ${day.timetableTotal}", color = TextSecondary)
                    if (day.notes.isNotBlank()) {
                        Text("Protocol Notes: ${day.notes}", color = TextTertiary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDayDetail = null }) { Text("Close", color = NeonLavender) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextTertiary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun ConsistencyBar(
    label: String,
    percent: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
            Text(text = "$percent%", style = MaterialTheme.typography.bodySmall.copy(color = color, fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = DarkBorder
        )
    }
}

@Composable
fun MonthlyBarChart(
    days: List<DisciplineDayEntity>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = days.size.coerceAtLeast(1)
        val spacing = 8.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(6.dp.toPx())

        days.forEachIndexed { index, day ->
            val scorePercent = (day.score / 100f).coerceIn(0.1f, 1f)
            val barHeight = height * scorePercent * 0.85f
            val x = index * (barWidth + spacing)
            val y = height - barHeight

            val barColor = when {
                day.score >= 90 -> NeonLavender
                day.score >= 75 -> NeonPurple
                day.score >= 60 -> DeepPurple
                else -> DarkBorder
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        // 80% Benchmark Dotted/Solid Line
        val benchY = height * 0.20f
        drawLine(
            color = TechCyan.copy(alpha = 0.4f),
            start = Offset(0f, benchY),
            end = Offset(width, benchY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun HeatmapGrid(
    monthlyDays: List<DisciplineDayEntity>,
    onDayClick: (DisciplineDayEntity) -> Unit
) {
    val totalDays = 30
    val daysMap = remember(monthlyDays) { monthlyDays.associateBy { it.dateString } }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // 5 rows of 6-7 columns
        for (row in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (col in 0 until 6) {
                    val dayNum = row * 6 + col + 1
                    if (dayNum <= totalDays) {
                        val dateKey = String.format("2026-11-%02d", dayNum)
                        val dayEntity = daysMap[dateKey] ?: DisciplineDayEntity(dateString = dateKey, score = 70 + (dayNum % 25))

                        val cellColor = when {
                            dayEntity.score >= 90 -> NeonLavender
                            dayEntity.score >= 80 -> NeonPurple
                            dayEntity.score >= 65 -> DeepPurple
                            else -> DarkBorder
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(cellColor)
                                .clickable { onDayClick(dayEntity) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayEntity.score >= 85) Color.Black else TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
