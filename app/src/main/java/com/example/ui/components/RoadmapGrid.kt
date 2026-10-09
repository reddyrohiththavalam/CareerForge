/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import com.example.data.model.GapPriority
import com.example.data.model.SkillRoadmapNode
import com.example.data.model.SkillStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoadmapGrid(
    nodes: List<SkillRoadmapNode>,
    selectedSemester: Int?, // null = all semesters
    onSelectSemester: (Int?) -> Unit,
    onToggleStatus: (String, SkillStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyGaps by remember { mutableStateOf(false) }

    val filteredNodes = remember(nodes, selectedSemester, showOnlyGaps) {
        nodes.filter { node ->
            val matchSemester = selectedSemester == null || node.semester == selectedSemester
            val matchGap = !showOnlyGaps || node.isAiGap
            matchSemester && matchGap
        }
    }

    val totalGaps = remember(nodes) { nodes.count { it.isAiGap } }

    Column(modifier = modifier.testTag("roadmap_grid")) {
        // Semester Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSemester == null && !showOnlyGaps,
                onClick = {
                    showOnlyGaps = false
                    onSelectSemester(null)
                },
                label = { Text("All 8 Sems") },
                modifier = Modifier.testTag("filter_all_sems")
            )

            (1..8).forEach { sem ->
                val semGaps = nodes.count { it.semester == sem && it.isAiGap }
                FilterChip(
                    selected = selectedSemester == sem && !showOnlyGaps,
                    onClick = {
                        showOnlyGaps = false
                        onSelectSemester(sem)
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sem $sem")
                            if (semGaps > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }
                        }
                    },
                    modifier = Modifier.testTag("filter_sem_$sem")
                )
            }

            if (totalGaps > 0) {
                FilterChip(
                    selected = showOnlyGaps,
                    onClick = { showOnlyGaps = !showOnlyGaps },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Gaps",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Gaps ($totalGaps)", color = Color(0xFFEF4444))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x33EF4444)
                    ),
                    modifier = Modifier.testTag("filter_ai_gaps")
                )
            }
        }

        // Summary Bar when gaps exist
        if (totalGaps > 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1B2E),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Scanner",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Job Description Analyzer Active",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$totalGaps curriculum nodes flagged as missing for target company placement bar.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of nodes
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredNodes.forEach { node ->
                RoadmapNodeCard(
                    node = node,
                    onToggleStatus = onToggleStatus
                )
            }
        }
    }
}

@Composable
fun RoadmapNodeCard(
    node: SkillRoadmapNode,
    onToggleStatus: (String, SkillStatus) -> Unit
) {
    val borderColor = when {
        node.isAiGap -> Color(0xFFEF4444)
        node.status == SkillStatus.COMPLETED -> Color(0xFF10B981)
        node.status == SkillStatus.IN_PROGRESS -> Color(0xFF06B6D4)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val cardBg = when {
        node.isAiGap -> Color(0xFF1F1218)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_${node.id}"),
        colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
        border = BorderStroke(if (node.isAiGap) 1.5.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Semester + Subject + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "SEM ${node.semester}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${node.subject} • ${node.category}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interactive Status Badge
                Surface(
                    onClick = {
                        val nextStatus = when (node.status) {
                            SkillStatus.NOT_STARTED -> SkillStatus.IN_PROGRESS
                            SkillStatus.IN_PROGRESS -> SkillStatus.COMPLETED
                            SkillStatus.COMPLETED -> SkillStatus.NOT_STARTED
                        }
                        onToggleStatus(node.id, nextStatus)
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = when (node.status) {
                        SkillStatus.COMPLETED -> Color(0xFF065F46)
                        SkillStatus.IN_PROGRESS -> Color(0xFF164E63)
                        SkillStatus.NOT_STARTED -> MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier.testTag("status_toggle_${node.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (node.status) {
                                SkillStatus.COMPLETED -> Icons.Default.CheckCircle
                                SkillStatus.IN_PROGRESS -> Icons.Default.HourglassTop
                                SkillStatus.NOT_STARTED -> Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = when (node.status) {
                                SkillStatus.COMPLETED -> Color(0xFF34D399)
                                SkillStatus.IN_PROGRESS -> Color(0xFF38BDF8)
                                SkillStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (node.status) {
                                SkillStatus.COMPLETED -> "Done"
                                SkillStatus.IN_PROGRESS -> "Learning"
                                SkillStatus.NOT_STARTED -> "Pending"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = node.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description with Bionic text
            BionicText(
                text = node.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // AI Gap Warning Section if flagged
            AnimatedVisibility(visible = node.isAiGap) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF371720))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "AI Gap",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI GAP DETECTED: [Priority: ${node.gapPriority}]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF87171)
                        )
                    }

                    if (node.aiGapReason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Why JD Demands This: ${node.aiGapReason}",
                            fontSize = 11.sp,
                            color = Color(0xFFFCA5A5)
                        )
                    }

                    if (node.actionPlan.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target Action Plan: ${node.actionPlan}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE047)
                        )
                    }
                }
            }
        }
    }
}
