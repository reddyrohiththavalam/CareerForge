/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockInterviewEvaluation
import com.example.ui.components.MockPanel

data class InterviewPreset(
    val title: String,
    val description: String,
    val boilerplate: String
)

@Composable
fun InterviewScreen(
    isEvaluating: Boolean,
    streamingLog: String,
    evaluationResult: MockInterviewEvaluation?,
    onSubmitCode: (String, String, String, String) -> Unit // code, language, problemTitle, topic
) {
    val presets = remember {
        listOf(
            InterviewPreset(
                title = "Two Sum (O(N) One-Pass)",
                description = "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. Optimize strictly for linear time and handle duplicate integers cleanly.",
                boilerplate = """
class Solution {
    fun twoSum(nums: IntArray, target: Int): IntArray {
        val map = HashMap<Int, Int>()
        for (i in nums.indices) {
            val complement = target - nums[i]
            if (map.containsKey(complement)) {
                return intArrayOf(map[complement]!!, i)
            }
            map[nums[i]] = i
        }
        return intArrayOf()
    }
}
                """.trimIndent()
            ),
            InterviewPreset(
                title = "Design LRU Cache in O(1)",
                description = "Design a data structure that follows the constraints of a Least Recently Used (LRU) cache. Implement get(key) and put(key, value) in strictly O(1) average time complexity.",
                boilerplate = """
class LRUCache(private val capacity: Int) {
    class Node(var key: Int = 0, var value: Int = 0, var prev: Node? = null, var next: Node? = null)
    private val map = HashMap<Int, Node>()
    private val head = Node()
    private val tail = Node()
    init {
        head.next = tail
        tail.prev = head
    }
    fun get(key: Int): Int {
        val node = map[key] ?: return -1
        moveToHead(node)
        return node.value
    }
    private fun moveToHead(node: Node) { /* TODO */ }
}
                """.trimIndent()
            ),
            InterviewPreset(
                title = "Course Schedule (Cycle Detection)",
                description = "There are numCourses courses labeled from 0 to numCourses - 1. You are given an array prerequisites. Return true if you can finish all courses, or false if cyclic dependencies exist.",
                boilerplate = """
class Solution {
    fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
        val inDegree = IntArray(numCourses)
        val adj = Array(numCourses) { ArrayList<Int>() }
        for (p in prerequisites) {
            adj[p[1]].add(p[0])
            inDegree[p[0]]++
        }
        // Run Kahn's algorithm
        return true
    }
}
                """.trimIndent()
            )
        )
    }

    var selectedPreset by remember { mutableStateOf(presets[0]) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("interview_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Problem Question Switcher
            Text(
                text = "Select Interview Problem:",
                fontSize = 11.sp,
                color = androidx.compose.ui.graphics.Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { preset ->
                    FilterChip(
                        selected = selectedPreset.title == preset.title,
                        onClick = { selectedPreset = preset },
                        label = { Text(preset.title.take(22) + "...", fontSize = 11.sp) },
                        modifier = Modifier.testTag("preset_${preset.title.take(6)}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            MockPanel(
                problemTitle = selectedPreset.title,
                problemDescription = selectedPreset.description,
                initialCode = selectedPreset.boilerplate,
                isEvaluating = isEvaluating,
                streamingLog = streamingLog,
                evaluationResult = evaluationResult,
                onSubmitCode = { code, language ->
                    onSubmitCode(code, language, selectedPreset.title, "Algorithms")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
