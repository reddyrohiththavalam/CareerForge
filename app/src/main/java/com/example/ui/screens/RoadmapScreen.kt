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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobDescriptionAnalysisResult
import com.example.data.model.SkillRoadmapNode
import com.example.data.model.SkillStatus
import com.example.ui.components.BionicText
import com.example.ui.components.RoadmapGrid

@Composable
fun RoadmapScreen(
    roadmapNodes: List<SkillRoadmapNode>,
    selectedSemester: Int?,
    isAnalyzingJd: Boolean,
    jdAnalysisResult: JobDescriptionAnalysisResult?,
    onSelectSemester: (Int?) -> Unit,
    onToggleStatus: (String, SkillStatus) -> Unit,
    onAnalyzeJd: (String) -> Unit
) {
    var showJdModal by remember { mutableStateOf(false) }
    var sampleJdText by remember {
        mutableStateOf(
            """
                Company: Top-Tier Fintech Unicorn (Tier 1 Product)
                Role: Backend Systems Engineer - SDE 1
                Requirements:
                * Strong fundamentals in Data Structures, OS, and multithreaded concurrency.
                * Hands-on experience with Kafka event streaming, Redis caching, and PostgreSQL B-tree indexing.
                * Familiarity with Docker, Kubernetes, and CI/CD pipelines.
                * Bonus: Microservice architecture and sub-millisecond API optimization.
            """.trimIndent()
        )
    }

    val completedCount = roadmapNodes.count { it.status == SkillStatus.COMPLETED }
    val inProgressCount = roadmapNodes.count { it.status == SkillStatus.IN_PROGRESS }
    val progressPercent = if (roadmapNodes.isNotEmpty()) (completedCount * 100) / roadmapNodes.size else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("roadmap_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BTech 8-Semester Milestone Velocity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$completedCount Completed • $inProgressCount Learning • ${roadmapNodes.size - completedCount - inProgressCount} Pending",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        Text(
                            text = "$progressPercent%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1F2937)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // JD Gap Analyzer Trigger Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E1B4B)),
                border = BorderStroke(1.dp, Color(0xFF818CF8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gemini JD Gap Mapper",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFA5B4FC)
                            )
                        }

                        Button(
                            onClick = { showJdModal = !showJdModal },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("toggle_jd_analyzer_button")
                        ) {
                            Text(
                                text = if (showJdModal) "Hide Analyzer" else "Scan Target JD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (jdAnalysisResult != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Target Match: ${jdAnalysisResult.matchPercentage}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF34D399)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• Identified: ${jdAnalysisResult.targetRoleIdentified}",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = jdAnalysisResult.summary,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Expandable JD Input Box
            AnimatedVisibility(visible = showJdModal) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Paste Job Description to Map Missing Skills to Semester Graph:",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = sampleJdText,
                            onValueChange = { sampleJdText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("jd_input_field"),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("Paste Job Description here...", fontSize = 11.sp) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onAnalyzeJd(sampleJdText) },
                            enabled = !isAnalyzingJd && sampleJdText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_jd_analysis_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isAnalyzingJd) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini Cross-Referencing 8 Semesters...")
                            } else {
                                Icon(imageVector = Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Map Missing Nodes to Curriculum", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            RoadmapGrid(
                nodes = roadmapNodes,
                selectedSemester = selectedSemester,
                onSelectSemester = onSelectSemester,
                onToggleStatus = onToggleStatus
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
