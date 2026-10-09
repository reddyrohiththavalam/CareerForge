/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example

import com.example.data.model.CognitiveGameScore
import com.example.data.model.GapPriority
import com.example.data.model.StudyTimetableSlot
import com.example.ui.components.formatBionicString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerForgeUnitTest {

    @Test
    fun testBionicStringPrefixFormatting() {
        val input = "Dynamic Programming"
        val formatted = formatBionicString(input, boldRatio = 0.5f)

        assertNotNull(formatted)
        assertEquals("Dynamic Programming", formatted.text)
        assertTrue("Formatted spans should not be empty", formatted.spanStyles.isNotEmpty())
    }

    @Test
    fun testBionicFormattingEmptyString() {
        val empty = formatBionicString("")
        assertEquals("", empty.text)
        assertTrue(empty.spanStyles.isEmpty())
    }

    @Test
    fun testStudyTimetableSlotModel() {
        val slot = StudyTimetableSlot(
            id = "slot_test",
            dayOfWeek = "Mon-Fri",
            startTime = "19:00",
            endTime = "21:00",
            topic = "Two Pointers & Binary Search",
            activityType = "DSA",
            cronSchedule = "0 19 * * 1-5",
            streakCount = 14,
            isCompletedToday = false,
            priority = GapPriority.HIGH,
            rationale = "Crucial placement pattern"
        )

        assertEquals("slot_test", slot.id)
        assertEquals("0 19 * * 1-5", slot.cronSchedule)
        assertEquals(14, slot.streakCount)
        assertFalse(slot.isCompletedToday)
        assertEquals(GapPriority.HIGH, slot.priority)
    }

    @Test
    fun testCognitiveGameScoreCalculation() {
        val score = CognitiveGameScore(
            id = "game_1",
            gameType = "BIG_O_AGILITY",
            snippetPrompt = "Binary Search",
            targetAnswer = "O(log N)",
            studentAnswer = "O(log N)",
            isCorrect = true,
            score = 250,
            comboMultiplier = 2
        )

        assertTrue(score.isCorrect)
        assertEquals("O(log N)", score.targetAnswer)
        assertEquals(2, score.comboMultiplier)
        assertEquals(250, score.score)
    }
}
