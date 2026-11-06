package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun TimerDialog(
    initialMinutes: Int = 30,
    title: String = "Trading Focused Learning",
    subtitle: String = "30-Minute Locked-In Chart Analysis & Risk Mastery",
    onDismiss: () -> Unit,
    onCompleted: () -> Unit
) {
    var totalSeconds by remember { mutableIntStateOf(initialMinutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(initialMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                isFinished = true
            }
        }
    }

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_arc")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, NeonLavender.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("timer_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Circular Countdown
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val stroke = 12.dp.toPx()
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        val topLeft = Offset(stroke / 2, stroke / 2)

                        drawArc(
                            color = DarkBorder,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )

                        val brush = Brush.sweepGradient(
                            listOf(NeonPurple, NeonLavender, TechCyan, NeonLavender)
                        )

                        drawArc(
                            brush = brush,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        val formattedTime = String.format("%02d:%02d", mins, secs)

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isFinished) PriorityLow else TextPrimary
                            )
                        )
                        Text(
                            text = if (isFinished) "SESSION COMPLETE!" else if (isRunning) "FOCUS LOCKED" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isFinished) PriorityLow else NeonLavender,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (isFinished) {
                    Button(
                        onClick = {
                            onCompleted()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PriorityLow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("complete_session_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Session Completed (+Discipline)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset button
                        FilledTonalIconButton(
                            onClick = {
                                isRunning = false
                                remainingSeconds = totalSeconds
                            },
                            modifier = Modifier.size(52.dp).testTag("reset_timer_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = DarkSurfaceElevated
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary)
                        }

                        // Play/Pause button
                        Button(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .testTag("toggle_timer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) DarkBorder else NeonLavender
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                tint = if (isRunning) TextPrimary else Color(0xFF1E0A3C)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRunning) "PAUSE" else "START FOCUS",
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) TextPrimary else Color(0xFF1E0A3C)
                            )
                        }

                        // Quick complete
                        FilledTonalIconButton(
                            onClick = {
                                isRunning = false
                                remainingSeconds = 0
                                isFinished = true
                            },
                            modifier = Modifier.size(52.dp).testTag("quick_finish_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = DarkSurfaceElevated
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Finish Now", tint = PriorityLow)
                        }
                    }
                }
            }
        }
    }
}
