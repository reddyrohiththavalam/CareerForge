/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.DsaProblem
import com.example.data.model.InterviewSession
import com.example.data.model.ResumeVersion
import com.example.data.model.SkillRoadmapNode
import com.example.data.model.StudentProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    // Secondary constructor resolving custom database ID from string resource
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: error("Operation requires authenticated user")
    }

    private fun userDoc(userId: String) = db.collection("users").document(userId)

    suspend fun saveUserProfile(profile: StudentProfile) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to profile.id,
            "name" to profile.name,
            "college" to profile.college,
            "branch" to profile.branch,
            "currentSemester" to profile.currentSemester,
            "targetRole" to profile.targetRole,
            "targetTier" to profile.targetTier,
            "leetCodeUsername" to profile.leetCodeUsername,
            "githubUsername" to profile.githubUsername,
            "updatedAt" to System.currentTimeMillis()
        )
        userDoc(uid).set(data, SetOptions.merge()).await()
    }

    suspend fun saveRoadmapNode(node: SkillRoadmapNode) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to node.id,
            "semester" to node.semester,
            "subject" to node.subject,
            "category" to node.category,
            "title" to node.title,
            "description" to node.description,
            "status" to node.status.name,
            "isAiGap" to node.isAiGap,
            "gapPriority" to node.gapPriority.name,
            "aiGapReason" to node.aiGapReason,
            "actionPlan" to node.actionPlan,
            "updatedAt" to System.currentTimeMillis()
        )
        userDoc(uid).collection("roadmaps").document(node.id).set(data, SetOptions.merge()).await()
    }

    suspend fun saveDsaProblem(problem: DsaProblem) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to problem.id,
            "title" to problem.title,
            "platform" to problem.platform,
            "difficulty" to problem.difficulty.name,
            "topic" to problem.topic,
            "status" to problem.status.name,
            "timeComplexity" to problem.timeComplexity,
            "spaceComplexity" to problem.spaceComplexity,
            "notes" to problem.notes,
            "solvedAt" to problem.solvedAt
        )
        userDoc(uid).collection("dsa_problems").document(problem.id).set(data, SetOptions.merge()).await()
    }

    suspend fun saveInterviewSession(session: InterviewSession) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to session.id,
            "timestamp" to session.timestamp,
            "role" to session.role,
            "topic" to session.topic,
            "questionText" to session.questionText,
            "studentCode" to session.studentCode,
            "feedback" to session.feedback,
            "codeQualityScore" to session.codeQualityScore,
            "logicScore" to session.logicScore,
            "confidenceScore" to session.confidenceScore
        )
        userDoc(uid).collection("interview_sessions").document(session.id).set(data, SetOptions.merge()).await()
    }

    suspend fun saveResumeVersion(resume: ResumeVersion) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to resume.id,
            "title" to resume.title,
            "targetCompany" to resume.targetCompany,
            "rawText" to resume.rawText,
            "atsScore" to resume.atsScore,
            "updatedAt" to System.currentTimeMillis()
        )
        userDoc(uid).collection("resume_versions").document(resume.id).set(data, SetOptions.merge()).await()
    }

    suspend fun saveTimetableSlot(slot: com.example.data.model.StudyTimetableSlot) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to slot.id,
            "dayOfWeek" to slot.dayOfWeek,
            "startTime" to slot.startTime,
            "endTime" to slot.endTime,
            "topic" to slot.topic,
            "activityType" to slot.activityType,
            "cronSchedule" to slot.cronSchedule,
            "streakCount" to slot.streakCount,
            "isCompletedToday" to slot.isCompletedToday,
            "priority" to slot.priority.name,
            "rationale" to slot.rationale,
            "updatedAt" to slot.updatedAt
        )
        userDoc(uid).collection("study_timetable").document(slot.id).set(data, SetOptions.merge()).await()
    }

    suspend fun saveCognitiveGameScore(score: com.example.data.model.CognitiveGameScore) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to score.id,
            "gameType" to score.gameType,
            "snippetPrompt" to score.snippetPrompt,
            "targetAnswer" to score.targetAnswer,
            "studentAnswer" to score.studentAnswer,
            "isCorrect" to score.isCorrect,
            "score" to score.score,
            "comboMultiplier" to score.comboMultiplier,
            "playedAt" to score.playedAt
        )
        userDoc(uid).collection("cognitive_games").document(score.id).set(data, SetOptions.merge()).await()
    }

    suspend fun saveChatbotMessage(msg: com.example.data.model.ChatbotMemoryMessage) {
        val uid = requireUserId()
        val data = hashMapOf(
            "id" to msg.id,
            "conversationId" to msg.conversationId,
            "role" to msg.role,
            "messageText" to msg.messageText,
            "memoryKey" to msg.memoryKey,
            "importanceScore" to msg.importanceScore,
            "timestamp" to msg.timestamp
        )
        userDoc(uid).collection("chatbot_memory").document(msg.id).set(data, SetOptions.merge()).await()
    }

    fun observeRemoteProfile(userId: String): Flow<StudentProfile?> {
        return userDoc(userId).snapshots().map { snap ->
            if (!snap.exists()) return@map null
            StudentProfile(
                id = snap.getString("id") ?: userId,
                name = snap.getString("name") ?: "Student",
                college = snap.getString("college") ?: "",
                branch = snap.getString("branch") ?: "",
                currentSemester = snap.getLong("currentSemester")?.toInt() ?: 5,
                targetRole = snap.getString("targetRole") ?: "",
                targetTier = snap.getString("targetTier") ?: "",
                leetCodeUsername = snap.getString("leetCodeUsername") ?: "",
                githubUsername = snap.getString("githubUsername") ?: ""
            )
        }
    }
}
