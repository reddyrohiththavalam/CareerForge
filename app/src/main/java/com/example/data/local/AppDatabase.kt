/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.DsaProblem
import com.example.data.model.InterviewSession
import com.example.data.model.ResumeVersion
import com.example.data.model.SkillRoadmapNode
import com.example.data.model.StudentProfile
import com.example.data.model.SyncMutation
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_profile WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: String = "default_student"): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profile WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String = "default_student"): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: StudentProfile)
}

@Dao
interface SkillRoadmapDao {
    @Query("SELECT * FROM skill_roadmaps ORDER BY semester ASC, id ASC")
    fun getAllNodesFlow(): Flow<List<SkillRoadmapNode>>

    @Query("SELECT * FROM skill_roadmaps WHERE semester = :semester ORDER BY id ASC")
    fun getNodesBySemesterFlow(semester: Int): Flow<List<SkillRoadmapNode>>

    @Query("SELECT * FROM skill_roadmaps WHERE isAiGap = 1")
    fun getAiGapNodesFlow(): Flow<List<SkillRoadmapNode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNode(node: SkillRoadmapNode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNodes(nodes: List<SkillRoadmapNode>)

    @Query("UPDATE skill_roadmaps SET isAiGap = 0, aiGapReason = '', actionPlan = ''")
    suspend fun clearAiGaps()

    @Query("UPDATE skill_roadmaps SET isAiGap = 1, aiGapReason = :reason, actionPlan = :action, gapPriority = :priority WHERE id = :id")
    suspend fun flagAiGap(id: String, reason: String, action: String, priority: String)
}

@Dao
interface DsaDao {
    @Query("SELECT * FROM dsa_tracker ORDER BY solvedAt DESC")
    fun getAllProblemsFlow(): Flow<List<DsaProblem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProblem(problem: DsaProblem)

    @Query("DELETE FROM dsa_tracker WHERE id = :id")
    suspend fun deleteProblem(id: String)
}

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interview_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<InterviewSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InterviewSession)
}

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resume_versions ORDER BY updatedAt DESC")
    fun getAllResumesFlow(): Flow<List<ResumeVersion>>

    @Query("SELECT * FROM resume_versions ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestResumeFlow(): Flow<ResumeVersion?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResume(resume: ResumeVersion)
}

@Dao
interface SyncMutationDao {
    @Query("SELECT * FROM sync_mutations WHERE isDispatched = 0 ORDER BY clientLamportClock ASC")
    fun getPendingMutationsFlow(): Flow<List<SyncMutation>>

    @Query("SELECT * FROM sync_mutations WHERE isDispatched = 0 ORDER BY clientLamportClock ASC")
    suspend fun getPendingMutationsList(): List<SyncMutation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordMutation(mutation: SyncMutation)

    @Query("UPDATE sync_mutations SET isDispatched = 1 WHERE mutationId = :mutationId")
    suspend fun markDispatched(mutationId: Long)

    @Query("DELETE FROM sync_mutations WHERE isDispatched = 1")
    suspend fun purgeDispatched()
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM study_timetable ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSlotsFlow(): Flow<List<com.example.data.model.StudyTimetableSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: com.example.data.model.StudyTimetableSlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlots(slots: List<com.example.data.model.StudyTimetableSlot>)

    @Query("UPDATE study_timetable SET isCompletedToday = 1, streakCount = streakCount + 1 WHERE id = :id")
    suspend fun markSlotCompleted(id: String)

    @Query("UPDATE study_timetable SET isCompletedToday = CASE WHEN isCompletedToday = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleSlotCompleted(id: String)

    @Query("UPDATE study_timetable SET isCompletedToday = 0")
    suspend fun resetDailyGoals()
}

@Dao
interface CognitiveGameDao {
    @Query("SELECT * FROM cognitive_games ORDER BY playedAt DESC")
    fun getAllScoresFlow(): Flow<List<com.example.data.model.CognitiveGameScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: com.example.data.model.CognitiveGameScore)
}

@Dao
interface ChatbotMemoryDao {
    @Query("SELECT * FROM chatbot_memory WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesFlow(convId: String = "default_mentor"): Flow<List<com.example.data.model.ChatbotMemoryMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: com.example.data.model.ChatbotMemoryMessage)
}

@Database(
    entities = [
        StudentProfile::class,
        SkillRoadmapNode::class,
        DsaProblem::class,
        InterviewSession::class,
        ResumeVersion::class,
        SyncMutation::class,
        com.example.data.model.StudyTimetableSlot::class,
        com.example.data.model.CognitiveGameScore::class,
        com.example.data.model.ChatbotMemoryMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun skillRoadmapDao(): SkillRoadmapDao
    abstract fun dsaDao(): DsaDao
    abstract fun interviewDao(): InterviewDao
    abstract fun resumeDao(): ResumeDao
    abstract fun syncMutationDao(): SyncMutationDao
    abstract fun timetableDao(): TimetableDao
    abstract fun cognitiveGameDao(): CognitiveGameDao
    abstract fun chatbotMemoryDao(): ChatbotMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "careerforge_offline.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
