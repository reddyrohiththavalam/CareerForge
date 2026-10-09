/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GapPriority
import com.example.data.model.StudyTimetableSlot
import com.example.ui.components.BionicText
import com.example.ui.components.DailyStudyGoalsModule

@Composable
fun TimetableScreen(
    slots: List<StudyTimetableSlot>,
    isParsing: Boolean,
    onParseTimetable: (String) -> Unit,
    onMarkCompleted: (String) -> Unit,
    onResetDailyGoals: (() -> Unit)? = null
) {
    var activeSubTab by remember { mutableIntStateOf(0) }
    var rawInput by remember { mutableStateOf("") }
    val armedAlertLogs = remember { mutableStateListOf<String>() }

    val currentStreak = slots.maxOfOrNull { it.streakCount } ?: 14
    val completedTodayCount = slots.count { it.isCompletedToday }
    val totalSlotsCount = slots.size
    val progressRatio = if (totalSlotsCount > 0) completedTodayCount.toFloat() / totalSlotsCount.toFloat() else 0f
    val progressPercent = (progressRatio * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF38BDF8),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = Color(0xFF38BDF8)
                )
            }
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Daily Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (progressPercent == 100) Color(0xFF065F46) else Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "$progressPercent%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (progressPercent == 100) Color(0xFF34D399) else Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("tab_daily_goals")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = {
                    Text(
                        text = "Schedule & AI Parser (${slots.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_weekly_schedule")
            )
        }

        if (activeSubTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    DailyStudyGoalsModule(
                        slots = slots,
                        onToggleSlot = onMarkCompleted,
                        onResetDailyGoals = onResetDailyGoals
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Daily Goals Progress Bar Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mini_daily_goals_banner"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Daily Goals Completion",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "$completedTodayCount / $totalSlotsCount Done ($progressPercent%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (progressPercent == 100) Color(0xFF10B981) else Color(0xFF38BDF8),
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                item {
                    // Gamified Streak Container Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_banner_container"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF991B1B), Color(0xFFC2410C), Color(0xFFEA580C))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak Fire",
                                            tint = Color(0xFFFEF08A),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "$currentStreak Day Streak 🔥",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Highest Streak: 28 Days | Unstoppable Momentum",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFED7AA)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "$completedTodayCount / $totalSlotsCount Done",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFFFEF08A),
                            trackColor = Color.White.copy(alpha = 0.25f),
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (progressRatio >= 1.0f) "🎉 Daily target achieved! Placement velocity maxed." else "Keep going! Complete remaining blocks to extend your streak.",
                            fontSize = 11.sp,
                            color = Color(0xFFFFEDD5)
                        )
                    }
                }
            }
        }

        // Gemini AI Timetable Parser Input
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timetable_ai_parser_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Unstructured Timetable Parser",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Describe your desired weekly routine in plain natural language. Gemini 2.5 Flash maps it to conflict-free slots with standard 5-field cron schedules.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timetable_raw_input"),
                        placeholder = {
                            Text(
                                "e.g. DSA 2hrs daily 7-9pm, OS revision on Sat morning, LeetCode contest Sunday 11am...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0B0F19),
                            unfocusedContainerColor = Color(0xFF0B0F19),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                rawInput = "Striver SDE Sheet Arrays & DP daily 19:00-21:00, Computer Networks & OS Saturday 10:00-13:00, Weekly OA Contest simulation Sunday 11:00-12:30."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sample Plan", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }

                        Button(
                            onClick = {
                                if (rawInput.isNotBlank()) {
                                    onParseTimetable(rawInput)
                                    val now = "Armed at " + System.currentTimeMillis() % 10000
                                    armedAlertLogs.add(0, "🔔 [Local Alert Armed] Routine synced to background loops ($now)")
                                }
                            },
                            enabled = !isParsing && rawInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("parse_timetable_button")
                        ) {
                            if (isParsing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parsing...", fontSize = 12.sp, color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parse & Arm Alarms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Local Alert Armed Notification Logs
        if (armedAlertLogs.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    border = BorderStroke(1.dp, Color(0xFF059669))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Local Notification Engine Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFD1FAE5)
                            )
                        }
                        armedAlertLogs.take(3).forEach { log ->
                            Text(
                                text = log,
                                fontSize = 11.sp,
                                color = Color(0xFFA7F3D0),
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Weekly Timetable Slots
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Schedule Slots (${slots.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = "5-Field Cron Synchronized",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // List of Timetable Slots
        items(slots, key = { it.id }) { slot ->
            TimetableSlotCard(
                slot = slot,
                onToggleComplete = { onMarkCompleted(slot.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
fun TimetableSlotCard(
    slot: StudyTimetableSlot,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slot_card_${slot.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isCompletedToday) Color(0xFF132338) else Color(0xFF0F172A)
        ),
        border = BorderStroke(
            1.dp,
            if (slot.isCompletedToday) Color(0xFF0284C7) else Color(0xFF1E293B)
        )
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
                                "CORE_CS" -> Color(0xFF7C3AED)
                                "PROJECT" -> Color(0xFF059669)
                                "CONTEST" -> Color(0xFFD97706)
                                else -> Color(0xFF475569)
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
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = slot.topic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.testTag("toggle_complete_${slot.id}")
                ) {
                    Icon(
                        imageVector = if (slot.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (slot.isCompletedToday) Color(0xFF10B981) else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
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

                // 5-field Cron chip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
