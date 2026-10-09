/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockInterviewEvaluation

@Composable
fun MockPanel(
    problemTitle: String,
    problemDescription: String,
    initialCode: String,
    isEvaluating: Boolean,
    streamingLog: String,
    evaluationResult: MockInterviewEvaluation?,
    onSubmitCode: (String, String) -> Unit, // code, language
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var codeContent by remember { mutableStateOf(initialCode) }

    val languages = listOf("Kotlin", "Java", "Python", "C++", "Go")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mock_interview_panel")
    ) {
        // Problem Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FAANG Technical Interview Round",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Surface(
                        color = Color(0xFF22C55E).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4ADE80),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = problemTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                BionicText(
                    text = problemDescription,
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF94A3B8), lineHeight = 16.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Language Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            languages.forEach { lang ->
                FilterChip(
                    selected = selectedLanguage == lang,
                    onClick = { selectedLanguage = lang },
                    label = { Text(lang, fontSize = 11.sp) },
                    modifier = Modifier.testTag("lang_$lang")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Split-Screen Editor Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090D16))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Solution Editor ($selectedLanguage)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Window indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Multi-line code input
                BasicTextField(
                    value = codeContent,
                    onValueChange = { codeContent = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF38BDF8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("code_editor_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = { onSubmitCode(codeContent, selectedLanguage) },
            enabled = !isEvaluating && codeContent.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_code_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isEvaluating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Interviewer Analyzing Logic...")
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Evaluate with Staff Engineer AI", fontWeight = FontWeight.Bold)
            }
        }

        // Live Streaming Feedback Section
        AnimatedVisibility(visible = streamingLog.isNotBlank() || isEvaluating) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isEvaluating) Color(0xFFF59E0B) else Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE INTERVIEW FEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = streamingLog.ifBlank { "Connecting to interviewer socket..." },
                    fontSize = 12.sp,
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Evaluation Results Panel
        AnimatedVisibility(visible = evaluationResult != null) {
            evaluationResult?.let { eval ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("interview_evaluation_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0D1424))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verdict: ${eval.seniorEngineerVerdict}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (eval.logicScore >= 75) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                            Text(
                                text = eval.algorithmicComplexity,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Score metrics
                        MetricProgressBar("Logic Correctness", eval.logicScore, Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(6.dp))
                        MetricProgressBar("Code Cleanliness", eval.codeQualityScore, Color(0xFFA855F7))
                        Spacer(modifier = Modifier.height(6.dp))
                        MetricProgressBar("Response Confidence", eval.confidenceScore, Color(0xFF10B981))

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Senior Interviewer Feedback:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BionicText(
                            text = eval.critique,
                            style = TextStyle(fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
                        )

                        if (eval.edgeCasesMissed.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Critical Edge Cases to Guard:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171)
                            )
                            eval.edgeCasesMissed.forEach { ec ->
                                Text(
                                    text = "• $ec",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricProgressBar(label: String, score: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(text = "$score / 100", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
    }
}
