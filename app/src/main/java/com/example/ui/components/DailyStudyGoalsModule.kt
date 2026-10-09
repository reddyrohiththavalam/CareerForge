/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GapPriority
import com.example.data.model.StudyTimetableSlot

data class CategoryGoalProgress(
    val category: String,
    val title: String,
    val completedCount: Int,
    val totalCount: Int,
    val progressRatio: Float,
    val color: Color,
    val icon: ImageVector
)

private fun parseMinutes(timeStr: String): Int {
    val parts = timeStr.trim().split(":")
    if (parts.size >= 2) {
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }
    return 0
}

private fun calculateSlotDurationMinutes(slot: StudyTimetableSlot): Int {
    val start = parseMinutes(slot.startTime)
    val end = parseMinutes(slot.endTime)
    return if (end > start) end - start else 90 // default 90 mins if cross midnight or invalid
}

@Composable
fun DailyStudyGoalsModule(
    slots: List<StudyTimetableSlot>,
    onToggleSlot: (String) -> Unit,
    onResetDailyGoals: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val totalSlotsCount = slots.size
    val completedSlotsCount = slots.count { it.isCompletedToday }
    val overallRatio = if (totalSlotsCount > 0) completedSlotsCount.toFloat() / totalSlotsCount.toFloat() else 0f
    val overallPercentage = (overallRatio * 100).toInt()

    // Focus time calculations
    val totalMinutes = slots.sumOf { calculateSlotDurationMinutes(it) }
    val completedMinutes = slots.filter { it.isCompletedToday }.sumOf { calculateSlotDurationMinutes(it) }
    val totalHoursStr = String.format("%.1f", totalMinutes / 60.0)
    val completedHoursStr = String.format("%.1f", completedMinutes / 60.0)

    val currentStreak = slots.maxOfOrNull { it.streakCount } ?: 14

    // Group categories
    val dsaSlots = slots.filter { it.activityType.equals("DSA", ignoreCase = true) }
    val coreCsSlots = slots.filter { it.activityType.equals("CORE_CS", ignoreCase = true) }
    val projectSlots = slots.filter { it.activityType.equals("PROJECT", ignoreCase = true) }
    val contestSlots = slots.filter { it.activityType.equals("CONTEST", ignoreCase = true) }
    val otherSlots = slots.filter {
        !it.activityType.equals("DSA", ignoreCase = true) &&
        !it.activityType.equals("CORE_CS", ignoreCase = true) &&
        !it.activityType.equals("PROJECT", ignoreCase = true) &&
        !it.activityType.equals("CONTEST", ignoreCase = true)
    }

    val categories = remember(slots) {
        val list = mutableListOf<CategoryGoalProgress>()
        if (dsaSlots.isNotEmpty()) {
            val done = dsaSlots.count { it.isCompletedToday }
            list.add(
                CategoryGoalProgress(
                    category = "DSA",
                    title = "Data Structures & Algorithms",
                    completedCount = done,
                    totalCount = dsaSlots.size,
                    progressRatio = done.toFloat() / dsaSlots.size.toFloat(),
                    color = Color(0xFF0284C7),
                    icon = Icons.Default.Code
                )
            )
        }
        if (coreCsSlots.isNotEmpty()) {
            val done = coreCsSlots.count { it.isCompletedToday }
            list.add(
                CategoryGoalProgress(
                    category = "CORE_CS",
                    title = "Core CS Fundamentals",
                    completedCount = done,
                    totalCount = coreCsSlots.size,
                    progressRatio = done.toFloat() / coreCsSlots.size.toFloat(),
                    color = Color(0xFF8B5CF6),
                    icon = Icons.Default.Computer
                )
            )
        }
        if (projectSlots.isNotEmpty()) {
            val done = projectSlots.count { it.isCompletedToday }
            list.add(
                CategoryGoalProgress(
                    category = "PROJECT",
                    title = "System Projects & Dev",
                    completedCount = done,
                    totalCount = projectSlots.size,
                    progressRatio = done.toFloat() / projectSlots.size.toFloat(),
                    color = Color(0xFF10B981),
                    icon = Icons.Default.FitnessCenter
                )
            )
        }
        if (contestSlots.isNotEmpty()) {
            val done = contestSlots.count { it.isCompletedToday }
            list.add(
                CategoryGoalProgress(
                    category = "CONTEST",
                    title = "Contests & Timed OAs",
                    completedCount = done,
                    totalCount = contestSlots.size,
                    progressRatio = done.toFloat() / contestSlots.size.toFloat(),
                    color = Color(0xFFF59E0B),
                    icon = Icons.Default.Assessment
                )
            )
        }
        if (otherSlots.isNotEmpty()) {
            val done = otherSlots.count { it.isCompletedToday }
            list.add(
                CategoryGoalProgress(
                    category = "OTHER",
                    title = "General Engineering Tasks",
                    completedCount = done,
                    totalCount = otherSlots.size,
                    progressRatio = done.toFloat() / otherSlots.size.toFloat(),
                    color = Color(0xFFEC4899),
                    icon = Icons.Default.EmojiEvents
                )
            )
        }
        list
    }

    // Filtered slot items
    val displayedSlots = remember(slots, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> slots.filter { !it.isCompletedToday }
            "COMPLETED" -> slots.filter { it.isCompletedToday }
            "DSA" -> slots.filter { it.activityType.equals("DSA", ignoreCase = true) }
            "CORE_CS" -> slots.filter { it.activityType.equals("CORE_CS", ignoreCase = true) }
            "PROJECT" -> slots.filter { it.activityType.equals("PROJECT", ignoreCase = true) }
            else -> slots
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_study_goals_module"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Primary Goals Executive Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_goals_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "DAILY EXECUTION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today's Study Goals",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Track your daily engineering milestones and placement velocity",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (overallPercentage == 100) Color(0xFF065F46) else Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (overallPercentage == 100) Color(0xFF10B981) else Color(0xFF334155)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (overallPercentage == 100) Icons.Default.EmojiEvents else Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (overallPercentage == 100) Color(0xFF34D399) else Color(0xFF38BDF8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (overallPercentage == 100) "GOAL MET 🎉" else "$overallPercentage% DONE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (overallPercentage == 100) Color(0xFF34D399) else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar with Animated Radial/Linear Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(68.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { overallRatio },
                            modifier = Modifier.size(68.dp),
                            color = when {
                                overallPercentage == 100 -> Color(0xFF10B981)
                                overallPercentage >= 50 -> Color(0xFF38BDF8)
                                else -> Color(0xFFF59E0B)
                            },
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round,
                            trackColor = Color(0xFF1E293B)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallPercentage%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tasks Completed",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$completedSlotsCount / $totalSlotsCount Tasks",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { overallRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("daily_goals_overall_progress_bar"),
                            color = when {
                                overallPercentage == 100 -> Color(0xFF10B981)
                                overallPercentage >= 50 -> Color(0xFF38BDF8)
                                else -> Color(0xFFF59E0B)
                            },
                            trackColor = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$completedHoursStr / $totalHoursStr hrs focus",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$currentStreak Days Streak",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFED7AA)
                                )
                            }
                        }
                    }
                }

                // Motivational Feedback Banner
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                overallPercentage == 100 -> "Outstanding execution! You hit 100% of your daily engineering roadmap targets. Your streak bonus is locked."
                                overallPercentage >= 70 -> "Almost there! Complete remaining tasks to maximize placement readiness and earn daily streak points."
                                overallPercentage >= 30 -> "Good momentum. Prioritize your high-impact DSA & Core CS problem sets."
                                else -> "Start your first study block today to protect your placement streak and build engineering grit."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFE0F2FE),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 2. Multi-Category Engineering Track Progress Bars
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("engineering_tracks_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENGINEERING TRACK BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${categories.size} ACTIVE DOMAINS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (categories.isEmpty()) {
                    Text(
                        text = "No study slots configured. Use the AI Parser to add routine blocks.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                } else {
                    categories.forEach { cat ->
                        val catPercent = (cat.progressRatio * 100).toInt()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .testTag("category_progress_${cat.category}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = cat.color.copy(alpha = 0.2f),
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                tint = cat.color,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${cat.completedCount}/${cat.totalCount}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (catPercent == 100) Color(0xFF065F46) else Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = "$catPercent%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (catPercent == 100) Color(0xFF34D399) else cat.color,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { cat.progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = cat.color,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf(
                "ALL" to "All Tasks ($totalSlotsCount)",
                "PENDING" to "Pending (${totalSlotsCount - completedSlotsCount})",
                "COMPLETED" to "Completed ($completedSlotsCount)",
                "DSA" to "DSA Only (${dsaSlots.size})",
                "CORE_CS" to "Core CS (${coreCsSlots.size})"
            )

            chips.forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("filter_chip_$key")
                )
            }
        }

        // 4. Daily Engineering Tasks Interactive Checklist
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIONABLE ENGINEERING TASKS (${displayedSlots.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF94A3B8)
                )

                if (onResetDailyGoals != null && completedSlotsCount > 0) {
                    Text(
                        text = "Reset Today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier
                            .clickable { onResetDailyGoals() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("reset_daily_goals_button")
                    )
                }
            }

            if (displayedSlots.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks found matching filter '$selectedFilter'",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                displayedSlots.forEach { slot ->
                    DailyTaskGoalCard(
                        slot = slot,
                        onToggle = { onToggleSlot(slot.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTaskGoalCard(
    slot: StudyTimetableSlot,
    onToggle: () -> Unit
) {
    val durationMins = calculateSlotDurationMinutes(slot)
    val durationText = "${durationMins / 60}h ${durationMins % 60}m"

    val isDone = slot.isCompletedToday

    val borderAnim by animateColorAsState(
        targetValue = if (isDone) Color(0xFF0284C7) else Color(0xFF1E293B),
        animationSpec = tween(250),
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_task_card_${slot.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFF0B192C) else Color(0xFF0F172A)
        ),
        border = BorderStroke(1.dp, borderAnim)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (slot.activityType) {
                                "DSA" -> Color(0xFF0284C7)
                                "CORE_CS" -> Color(0xFF8B5CF6)
                                "PROJECT" -> Color(0xFF10B981)
                                "CONTEST" -> Color(0xFFF59E0B)
                                else -> Color(0xFF64748B)
                            }
                        ) {
                            Text(
                                text = slot.activityType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (slot.priority) {
                                GapPriority.HIGH -> Color(0xFFDC2626)
                                GapPriority.MEDIUM -> Color(0xFFD97706)
                                else -> Color(0xFF475569)
                            }
                        ) {
                            Text(
                                text = slot.priority.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = durationText,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = slot.topic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDone) Color(0xFFE2E8F0) else Color.White,
                        textDecoration = if (isDone) TextDecoration.None else TextDecoration.None
                    )
                }

                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.testTag("daily_task_toggle_${slot.id}")
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Goal Complete",
                        tint = if (isDone) Color(0xFF10B981) else Color(0xFF64748B),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${slot.dayOfWeek} • ${slot.startTime} - ${slot.endTime}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = slot.cronSchedule,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            if (slot.rationale.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                BionicText(
                    text = slot.rationale,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    boldWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
