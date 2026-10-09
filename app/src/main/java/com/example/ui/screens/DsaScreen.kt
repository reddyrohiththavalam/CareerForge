/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DsaProblem
import com.example.data.model.ProblemDifficulty
import com.example.data.model.ProblemStatus
import com.example.ui.components.BionicText
import java.util.UUID

@Composable
fun DsaScreen(
    problems: List<DsaProblem>,
    onAddProblem: (DsaProblem) -> Unit,
    onToggleStatus: (String, ProblemStatus) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTopicFilter by remember { mutableStateOf<String?>(null) }

    var newTitle by remember { mutableStateOf("") }
    var newPlatform by remember { mutableStateOf("LeetCode") }
    var newTopic by remember { mutableStateOf("Dynamic Programming") }
    var newDifficulty by remember { mutableStateOf(ProblemDifficulty.MEDIUM) }
    var newNotes by remember { mutableStateOf("") }

    val topics = remember(problems) {
        listOf("All") + problems.map { it.topic }.distinct()
    }

    val filteredProblems = remember(problems, selectedTopicFilter) {
        if (selectedTopicFilter == null || selectedTopicFilter == "All") problems
        else problems.filter { it.topic == selectedTopicFilter }
    }

    val solvedCount = problems.count { it.status == ProblemStatus.SOLVED }
    val revisingCount = problems.count { it.status == ProblemStatus.REVISING }
    val stuckCount = problems.count { it.status == ProblemStatus.STUCK }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("dsa_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // DSA Stats Dashboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Striver & NeetCode Grind Tracker",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DsaStatPill("Mastered", solvedCount, Color(0xFF10B981))
                        DsaStatPill("Revising", revisingCount, Color(0xFF06B6D4))
                        DsaStatPill("Stuck", stuckCount, Color(0xFFF43F5E))
                        DsaStatPill("Total", problems.size, Color(0xFF8B5CF6))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Topic Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topics.forEach { topic ->
                    FilterChip(
                        selected = (selectedTopicFilter == null && topic == "All") || selectedTopicFilter == topic,
                        onClick = { selectedTopicFilter = if (topic == "All") null else topic },
                        label = { Text(topic, fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_dsa_${topic.take(6)}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Add Form Button
            Button(
                onClick = { showAddDialog = !showAddDialog },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_add_dsa_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showAddDialog) "Hide Add Problem" else "Log Solved DSA Problem", fontWeight = FontWeight.Bold)
            }

            // Expandable Add Form
            AnimatedVisibility(visible = showAddDialog) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF131A2E))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Log New Problem", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Problem Title (e.g., Longest Substring Without Repeating)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_dsa_title")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newTopic,
                                onValueChange = { newTopic = it },
                                label = { Text("Topic / Pattern") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newPlatform,
                                onValueChange = { newPlatform = it },
                                label = { Text("Platform") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newNotes,
                            onValueChange = { newNotes = it },
                            label = { Text("Algorithmic Invariant / Complexity Note") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    onAddProblem(
                                        DsaProblem(
                                            id = UUID.randomUUID().toString(),
                                            title = newTitle,
                                            platform = newPlatform,
                                            topic = newTopic,
                                            difficulty = newDifficulty,
                                            status = ProblemStatus.SOLVED,
                                            notes = newNotes
                                        )
                                    )
                                    newTitle = ""
                                    newNotes = ""
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_dsa_problem_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Save to Offline CRDT Store", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        items(filteredProblems, key = { it.id }) { problem ->
            DsaProblemCard(problem = problem, onToggleStatus = onToggleStatus)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DsaStatPill(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun DsaProblemCard(
    problem: DsaProblem,
    onToggleStatus: (String, ProblemStatus) -> Unit
) {
    val diffColor = when (problem.difficulty) {
        ProblemDifficulty.EASY -> Color(0xFF10B981)
        ProblemDifficulty.MEDIUM -> Color(0xFFF59E0B)
        ProblemDifficulty.HARD -> Color(0xFFEF4444)
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dsa_card_${problem.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF131A2A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = diffColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = problem.difficulty.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = diffColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${problem.platform} • ${problem.topic}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Interactive Status Pill
                Surface(
                    onClick = {
                        val next = when (problem.status) {
                            ProblemStatus.SOLVED -> ProblemStatus.REVISING
                            ProblemStatus.REVISING -> ProblemStatus.STUCK
                            ProblemStatus.STUCK -> ProblemStatus.SOLVED
                        }
                        onToggleStatus(problem.id, next)
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = when (problem.status) {
                        ProblemStatus.SOLVED -> Color(0xFF065F46)
                        ProblemStatus.REVISING -> Color(0xFF164E63)
                        ProblemStatus.STUCK -> Color(0xFF881337)
                    }
                ) {
                    Text(
                        text = when (problem.status) {
                            ProblemStatus.SOLVED -> "✓ Solved"
                            ProblemStatus.REVISING -> "↻ Revising"
                            ProblemStatus.STUCK -> "! Stuck"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = problem.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (problem.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                BionicText(
                    text = problem.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Time: ${problem.timeComplexity}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "Space: ${problem.spaceComplexity}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFA855F7)
                )
            }
        }
    }
}
