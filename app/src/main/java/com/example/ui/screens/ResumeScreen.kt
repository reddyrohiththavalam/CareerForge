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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtsEvaluationResult
import com.example.data.model.ResumeAtsFeedback
import com.example.data.model.ResumeVersion
import com.example.ui.components.BionicText

@Composable
fun ResumeScreen(
    currentResume: ResumeVersion?,
    isEvaluatingAts: Boolean,
    atsResult: AtsEvaluationResult?,
    onOptimizeResume: (String, String) -> Unit // resumeContent, targetRole
) {
    var resumeContent by remember(currentResume) {
        mutableStateOf(currentResume?.rawText ?: "")
    }
    var targetRole by remember { mutableStateOf("Backend Systems SDE (FAANG / Product)") }

    val displayScore = atsResult?.overallScore ?: currentResume?.atsScore ?: 75

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("resume_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Score Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ATS Keyword & Impact Score",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Audits for Google XYZ Formula & High-Impact Active Verbs.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Score Circle
                    Surface(
                        shape = CircleShape,
                        color = when {
                            displayScore >= 80 -> Color(0xFF10B981).copy(alpha = 0.2f)
                            displayScore >= 65 -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        },
                        border = BorderStroke(
                            2.dp,
                            when {
                                displayScore >= 80 -> Color(0xFF10B981)
                                displayScore >= 65 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$displayScore",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Target Role Input
            OutlinedTextField(
                value = targetRole,
                onValueChange = { targetRole = it },
                label = { Text("Target Role / Company Calibration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_role_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Raw Resume Content
            Text(
                text = "Resume Content / Bullet Points Draft:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCBD5E1)
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = resumeContent,
                onValueChange = { resumeContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("resume_content_input"),
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onOptimizeResume(resumeContent, targetRole) },
                enabled = !isEvaluatingAts && resumeContent.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_ats_optimizer_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isEvaluatingAts) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini Recruiter Bot Auditing...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audit & Generate XYZ Optimizations", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Suggestions List
        atsResult?.optimizations?.let { suggestions ->
            item {
                Text(
                    text = "AI OPTIMIZATION RECOMMENDATIONS (${suggestions.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF818CF8)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(suggestions.size) { idx ->
                val item = suggestions[idx]
                AtsOptimizationCard(
                    feedback = item,
                    onApply = {
                        resumeContent = resumeContent.replace(item.originalLine, item.suggestedLine)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AtsOptimizationCard(
    feedback: ResumeAtsFeedback,
    onApply: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF131A2E)),
        border = BorderStroke(1.dp, Color(0xFF374151))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${feedback.scoreDelta} PTS ESTIMATED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onApply,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8))
                ) {
                    Text("Apply Replacement", fontSize = 10.sp, color = Color(0xFF38BDF8))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Original Line
            Text(
                text = "BEFORE (PASSIVE / UNMEASURED):",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
            Text(
                text = feedback.originalLine,
                fontSize = 12.sp,
                color = Color(0xFFFCA5A5),
                style = TextStyle(textDecoration = TextDecoration.LineThrough)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Suggested Line
            Text(
                text = "SUGGESTED (GOOGLE XYZ HIGH-IMPACT):",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            BionicText(
                text = feedback.suggestedLine,
                style = TextStyle(fontSize = 12.sp, color = Color(0xFFA7F3D0), lineHeight = 16.sp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "💡 Rationale: ${feedback.impactRationale}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
