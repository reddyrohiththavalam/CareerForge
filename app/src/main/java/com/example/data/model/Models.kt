/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncState {
    SYNCED,
    PENDING_SYNC,
    LOCAL_ONLY
}

enum class SkillStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

enum class GapPriority {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

enum class ProblemDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class ProblemStatus {
    SOLVED,
    REVISING,
    STUCK
}

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: String = "default_student",
    val name: String = "Rohit Sharma",
    val college: String = "National Institute of Technology",
    val branch: String = "Computer Science & Engineering",
    val currentSemester: Int = 5,
    val targetRole: String = "Backend Systems & Distributed SDE",
    val targetTier: String = "Tier 1 Product / High-Frequency FinTech",
    val leetCodeUsername: String = "rohit_codes",
    val githubUsername: String = "rohit-dev",
    val solvedDsaCount: Int = 184,
    val currentCgpa: Double = 8.72,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "skill_roadmaps")
data class SkillRoadmapNode(
    @PrimaryKey val id: String,
    val semester: Int, // 1 through 8
    val subject: String,
    val category: String, // "Core CS", "DSA", "Dev & Web3", "Systems", "Placements"
    val title: String,
    val description: String,
    val status: SkillStatus = SkillStatus.NOT_STARTED,
    val isAiGap: Boolean = false,
    val gapPriority: GapPriority = GapPriority.NONE,
    val aiGapReason: String = "",
    val actionPlan: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "dsa_tracker")
data class DsaProblem(
    @PrimaryKey val id: String,
    val title: String,
    val platform: String = "LeetCode", // LeetCode, Codeforces, Striver SDE Sheet
    val difficulty: ProblemDifficulty = ProblemDifficulty.MEDIUM,
    val topic: String, // Arrays, DP, Graphs, Binary Trees, Sliding Window
    val status: ProblemStatus = ProblemStatus.SOLVED,
    val timeComplexity: String = "O(n log n)",
    val spaceComplexity: String = "O(n)",
    val notes: String = "",
    val solvedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "interview_sessions")
data class InterviewSession(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val role: String,
    val topic: String,
    val questionText: String,
    val studentCode: String,
    val feedback: String,
    val codeQualityScore: Int, // 0 - 100
    val logicScore: Int, // 0 - 100
    val confidenceScore: Int, // 0 - 100
    val version: Long = 1L,
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "resume_versions")
data class ResumeVersion(
    @PrimaryKey val id: String,
    val title: String = "SWE Placement Master",
    val targetCompany: String = "Google / Atlassian",
    val rawText: String,
    val atsScore: Int = 78,
    val suggestionsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "sync_mutations")
data class SyncMutation(
    @PrimaryKey(autoGenerate = true) val mutationId: Long = 0,
    val entityType: String, // "studentProfile", "skillRoadmaps", "dsaTracker", etc.
    val recordId: String,
    val operation: String, // "UPSERT", "DELETE"
    val payloadJson: String,
    val clientLamportClock: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isDispatched: Boolean = false
)

// Data transfer models for Gemini parsing
data class JobDescriptionAnalysisResult(
    val matchPercentage: Int,
    val summary: String,
    val targetRoleIdentified: String,
    val gapsFound: List<SemesterGapNode>
)

data class SemesterGapNode(
    val nodeId: String,
    val semester: Int,
    val missingSkill: String,
    val priority: GapPriority,
    val whyRequired: String,
    val recommendedAction: String
)

data class ResumeAtsFeedback(
    val originalLine: String,
    val suggestedLine: String,
    val impactRationale: String,
    val scoreDelta: Int
)

data class AtsEvaluationResult(
    val overallScore: Int,
    val keywordMatchRate: Int,
    val summary: String,
    val optimizations: List<ResumeAtsFeedback>
)

data class MockInterviewEvaluation(
    val logicScore: Int,
    val codeQualityScore: Int,
    val confidenceScore: Int,
    val critique: String,
    val algorithmicComplexity: String,
    val edgeCasesMissed: List<String>,
    val seniorEngineerVerdict: String
)

data class PanicTriageTask(
    val microTask: String,
    val timeToCompleteMinutes: Int,
    val actionableStep: String
)

@Entity(tableName = "study_timetable")
data class StudyTimetableSlot(
    @PrimaryKey val id: String,
    val dayOfWeek: String = "Mon-Fri",
    val startTime: String = "19:00",
    val endTime: String = "21:00",
    val topic: String,
    val activityType: String = "DSA", // DSA, CORE_CS, PROJECT, CONTEST, REVISION
    val cronSchedule: String = "0 19 * * 1-5",
    val streakCount: Int = 12,
    val isCompletedToday: Boolean = false,
    val priority: GapPriority = GapPriority.HIGH,
    val rationale: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "cognitive_games")
data class CognitiveGameScore(
    @PrimaryKey val id: String,
    val gameType: String = "BIG_O_AGILITY",
    val snippetPrompt: String,
    val targetAnswer: String,
    val studentAnswer: String,
    val isCorrect: Boolean,
    val score: Int,
    val comboMultiplier: Int,
    val playedAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.SYNCED
)

@Entity(tableName = "chatbot_memory")
data class ChatbotMemoryMessage(
    @PrimaryKey val id: String,
    val conversationId: String = "default_mentor",
    val role: String, // "user", "model"
    val messageText: String,
    val memoryKey: String = "",
    val importanceScore: Int = 5,
    val timestamp: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.SYNCED
)

data class ParsedTimetableSlotDto(
    val id: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val topic: String,
    val activityType: String,
    val cronSchedule: String,
    val priority: GapPriority,
    val rationale: String
)

data class ParsedTimetableResultDto(
    val weeklyCommitmentHours: Int,
    val executiveSummary: String,
    val slots: List<ParsedTimetableSlotDto>
)
