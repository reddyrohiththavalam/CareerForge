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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CognitiveGameScore
import kotlinx.coroutines.delay
import java.util.UUID

data class GameSnippetChallenge(
    val id: String,
    val title: String,
    val code: String,
    val targetComplexity: String,
    val explanation: String
)

val CHALLENGES = listOf(
    GameSnippetChallenge(
        id = "c1",
        title = "Binary Search Bounds Split",
        code = """var l = 0, r = n - 1
while (l <= r) {
    val mid = l + (r - l) / 2
    if (arr[mid] == target) return mid
    if (arr[mid] < target) l = mid + 1 else r = mid - 1
}""",
        targetComplexity = "O(log N)",
        explanation = "The search interval is cut in half at every comparison iteration."
    ),
    GameSnippetChallenge(
        id = "c2",
        title = "Nested Square Matrix Traversal",
        code = """for (i in 0 until n) {
    for (j in 0 until n) {
        matrix[i][j] += offset
    }
}""",
        targetComplexity = "O(N^2)",
        explanation = "Double nested loop each iterating N times yielding quadratic time."
    ),
    GameSnippetChallenge(
        id = "c3",
        title = "Divide-and-Conquer Merge Sort",
        code = """fun sort(arr: IntArray) {
    if (arr.size <= 1) return
    val mid = arr.size / 2
    val left = sort(arr.copyOfRange(0, mid))
    val right = sort(arr.copyOfRange(mid, arr.size))
    merge(left, right)
}""",
        targetComplexity = "O(N log N)",
        explanation = "Log N recursive levels with linear O(N) merging work at each level."
    ),
    GameSnippetChallenge(
        id = "c4",
        title = "Direct Hash Table Index Lookup",
        code = """val user = userMap[userId]
return user?.isActive ?: false""",
        targetComplexity = "O(1)",
        explanation = "Constant time hashing bucket index resolution amortized."
    ),
    GameSnippetChallenge(
        id = "c5",
        title = "Naive Exponential Recursive Fibonacci",
        code = """fun fib(n: Int): Int {
    if (n <= 1) return n
    return fib(n - 1) + fib(n - 2)
}""",
        targetComplexity = "O(2^N)",
        explanation = "Binary tree recursion branches into 2 calls per step without memoization."
    ),
    GameSnippetChallenge(
        id = "c6",
        title = "Inward Two-Pointer Array Scan",
        code = """var l = 0, r = arr.size - 1
while (l < r) {
    val sum = arr[l] + arr[r]
    if (sum == target) return intArrayOf(l, r)
    if (sum < target) l++ else r--
}""",
        targetComplexity = "O(N)",
        explanation = "Each element is visited at most once by either the left or right pointer."
    )
)

val COMPLEXITY_CHOICES = listOf("O(1)", "O(log N)", "O(N)", "O(N log N)", "O(N^2)", "O(2^N)")

@Composable
fun BigOGameScreen(
    onRecordScore: (CognitiveGameScore) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var currentScore by remember { mutableIntStateOf(0) }
    var comboMultiplier by remember { mutableIntStateOf(1) }
    var streakCount by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(12) }
    var isTimerActive by remember { mutableStateOf(true) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val challenge = CHALLENGES[currentIndex % CHALLENGES.size]

    // Countdown Timer Loop
    LaunchedEffect(currentIndex, isTimerActive) {
        if (isTimerActive) {
            timeLeft = 12
            while (timeLeft > 0 && !isAnswered) {
                delay(1000L)
                timeLeft -= 1
            }
            if (timeLeft == 0 && !isAnswered) {
                // Time expired
                isAnswered = true
                comboMultiplier = 1
                streakCount = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Game Header Stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game_stats_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF7C3AED).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Big-O Agility Game",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Algorithmic Complexity Reflexes",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$currentScore pts",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF38BDF8)
                        )
                        if (comboMultiplier > 1) {
                            Text(
                                text = "${comboMultiplier}x COMBO 🔥",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }

        // Timer Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (timeLeft <= 4) Color(0xFFEF4444) else Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$timeLeft s remaining",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeft <= 4) Color(0xFFEF4444) else Color.White
                )
            }
            Text(
                text = "Challenge ${currentIndex + 1} of ${CHALLENGES.size}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        LinearProgressIndicator(
            progress = { (timeLeft / 12f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = if (timeLeft <= 4) Color(0xFFEF4444) else Color(0xFF38BDF8),
            trackColor = Color(0xFF1E293B)
        )

        // Snippet Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("code_snippet_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
            border = BorderStroke(1.dp, Color(0xFF1F2937))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = challenge.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFE5E7EB)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "Kotlin",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090D16), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = challenge.code,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Complexity Answer Buttons
        Text(
            text = "Select Worst-Case Time Complexity:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val rows = COMPLEXITY_CHOICES.chunked(3)
            rows.forEach { rowChoices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowChoices.forEach { choice ->
                        val isSelected = selectedAnswer == choice
                        val isCorrect = choice == challenge.targetComplexity

                        val buttonColor = when {
                            !isAnswered -> Color(0xFF1E293B)
                            isCorrect -> Color(0xFF059669)
                            isSelected && !isCorrect -> Color(0xFFDC2626)
                            else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                        }

                        Button(
                            onClick = {
                                if (!isAnswered) {
                                    selectedAnswer = choice
                                    isAnswered = true
                                    val correct = choice == challenge.targetComplexity
                                    if (correct) {
                                        val pointsAdded = 100 * comboMultiplier + timeLeft * 10
                                        currentScore += pointsAdded
                                        streakCount += 1
                                        comboMultiplier = (comboMultiplier + 1).coerceAtMost(5)
                                    } else {
                                        comboMultiplier = 1
                                        streakCount = 0
                                    }

                                    onRecordScore(
                                        CognitiveGameScore(
                                            id = "game_${UUID.randomUUID()}",
                                            gameType = "BIG_O_AGILITY",
                                            snippetPrompt = challenge.title,
                                            targetAnswer = challenge.targetComplexity,
                                            studentAnswer = choice,
                                            isCorrect = correct,
                                            score = currentScore,
                                            comboMultiplier = comboMultiplier
                                        )
                                    )
                                }
                            },
                            enabled = !isAnswered,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_complexity_$choice"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Text(
                                text = choice,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Post-Answer Feedback Banner
        AnimatedVisibility(visible = isAnswered) {
            val correct = selectedAnswer == challenge.targetComplexity
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("answer_feedback_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (correct) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (correct) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (correct) Color(0xFF34D399) else Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (correct) "Correct! +${100 * comboMultiplier + timeLeft * 10} pts" else "Incorrect! Target was ${challenge.targetComplexity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = challenge.explanation,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isAnswered = false
                            selectedAnswer = null
                            currentIndex += 1
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_challenge_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "Next Challenge →",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
