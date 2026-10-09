/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.data.repository

import com.example.data.firebase.FirestoreRepository
import com.example.data.local.AppDatabase
import com.example.data.model.AtsEvaluationResult
import com.example.data.model.DsaProblem
import com.example.data.model.GapPriority
import com.example.data.model.InterviewSession
import com.example.data.model.JobDescriptionAnalysisResult
import com.example.data.model.MockInterviewEvaluation
import com.example.data.model.PanicTriageTask
import com.example.data.model.ProblemDifficulty
import com.example.data.model.ProblemStatus
import com.example.data.model.ResumeVersion
import com.example.data.model.SkillRoadmapNode
import com.example.data.model.SkillStatus
import com.example.data.model.StudentProfile
import com.example.data.model.SyncState
import com.example.data.remote.GeminiService
import com.example.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class CareerRepository(
    private val db: AppDatabase,
    val syncManager: SyncManager,
    private val geminiService: GeminiService,
    private val firestoreRepository: FirestoreRepository? = null
) {
    val studentProfileFlow: Flow<StudentProfile?> = db.studentDao().getProfileFlow()
    val allRoadmapNodesFlow: Flow<List<SkillRoadmapNode>> = db.skillRoadmapDao().getAllNodesFlow()
    val allDsaProblemsFlow: Flow<List<DsaProblem>> = db.dsaDao().getAllProblemsFlow()
    val allInterviewSessionsFlow: Flow<List<InterviewSession>> = db.interviewDao().getAllSessionsFlow()
    val latestResumeFlow: Flow<ResumeVersion?> = db.resumeDao().getLatestResumeFlow()
    val allTimetableSlotsFlow: Flow<List<com.example.data.model.StudyTimetableSlot>> = db.timetableDao().getAllSlotsFlow()
    val allGameScoresFlow: Flow<List<com.example.data.model.CognitiveGameScore>> = db.cognitiveGameDao().getAllScoresFlow()
    val chatMessagesFlow: Flow<List<com.example.data.model.ChatbotMemoryMessage>> = db.chatbotMemoryDao().getMessagesFlow()

    suspend fun initializeSeedDataIfEmpty() {
        val existingProfile = db.studentDao().getProfile()
        if (existingProfile == null) {
            val defaultProfile = StudentProfile()
            db.studentDao().upsertProfile(defaultProfile)
        }

        val existingNodes = db.skillRoadmapDao().getAllNodesFlow().firstOrNull() ?: emptyList()
        if (existingNodes.isEmpty()) {
            val defaultCurriculum = generateCuratedBTechRoadmap()
            db.skillRoadmapDao().upsertNodes(defaultCurriculum)
        }

        val existingDsa = db.dsaDao().getAllProblemsFlow().firstOrNull() ?: emptyList()
        if (existingDsa.isEmpty()) {
            val defaultProblems = generateCuratedDsaQuestions()
            for (problem in defaultProblems) {
                db.dsaDao().upsertProblem(problem)
            }
        }

        val existingResume = db.resumeDao().getLatestResumeFlow().firstOrNull()
        if (existingResume == null) {
            val defaultResume = ResumeVersion(
                id = "master_resume_v1",
                title = "Senior SWE Placement Draft",
                targetCompany = "Google / Atlassian / Microsoft",
                rawText = """
                    Rohit Sharma | Computer Science Engineering (B.Tech)
                    GitHub: github.com/rohit-dev | LeetCode: leetcode.com/rohit_codes
                    
                    PROJECTS:
                    * Distributed High-Throughput Task Queue: Built an asynchronous distributed worker scheduler in Go, processing background jobs with Redis lock primitives and graceful worker shutdown.
                    * Real-Time Collaborative Canvas: Implemented canvas drawing room with WebSocket synchronization and operational transformation conflict resolution.
                    
                    TECHNICAL SKILLS:
                    Languages: Kotlin, Java, Go, TypeScript, C++
                    Frameworks: Spring Boot, Node.js/Express, Jetpack Compose, React
                    Databases & Infra: PostgreSQL, Redis, Docker, Git, CI/CD
                    
                    EXPERIENCE & ACHIEVEMENTS:
                    * Solved 180+ problems on LeetCode covering graph search and dynamic programming.
                    * 3rd Place at Inter-College Hackathon 2025 out of 120 teams.
                """.trimIndent(),
                atsScore = 78
            )
            db.resumeDao().upsertResume(defaultResume)
        }

        val existingSlots = db.timetableDao().getAllSlotsFlow().firstOrNull() ?: emptyList()
        if (existingSlots.isEmpty()) {
            val defaultSlots = listOf(
                com.example.data.model.StudyTimetableSlot(
                    id = "slot_dsa_daily",
                    dayOfWeek = "Mon-Fri",
                    startTime = "19:00",
                    endTime = "21:00",
                    topic = "Striver SDE Sheet Two Pointers & Dynamic Programming",
                    activityType = "DSA",
                    cronSchedule = "0 19 * * 1-5",
                    streakCount = 14,
                    isCompletedToday = false,
                    priority = GapPriority.HIGH,
                    rationale = "Anchors daily algorithmic pattern recognition for upcoming OA rounds."
                ),
                com.example.data.model.StudyTimetableSlot(
                    id = "slot_core_weekend",
                    dayOfWeek = "Saturday",
                    startTime = "10:00",
                    endTime = "13:00",
                    topic = "Operating Systems Virtual Memory & Paging Internals",
                    activityType = "CORE_CS",
                    cronSchedule = "0 10 * * 6",
                    streakCount = 8,
                    isCompletedToday = false,
                    priority = GapPriority.MEDIUM,
                    rationale = "Dedicated deep-work block for core CS concepts."
                ),
                com.example.data.model.StudyTimetableSlot(
                    id = "slot_contest_sunday",
                    dayOfWeek = "Sunday",
                    startTime = "11:00",
                    endTime = "12:30",
                    topic = "Weekly Online Assessment Speed Round Simulation",
                    activityType = "CONTEST",
                    cronSchedule = "0 11 * * 0",
                    streakCount = 4,
                    isCompletedToday = false,
                    priority = GapPriority.HIGH,
                    rationale = "Simulates real company online test time pressure."
                )
            )
            db.timetableDao().upsertSlots(defaultSlots)
        }
    }

    suspend fun updateStudentProfile(profile: StudentProfile) {
        val updated = profile.copy(
            updatedAt = System.currentTimeMillis(),
            version = profile.version + 1,
            syncState = SyncState.PENDING_SYNC
        )
        db.studentDao().upsertProfile(updated)
        syncManager.recordLocalChange(
            entityType = "studentProfile",
            recordId = updated.id,
            operation = "UPSERT",
            payloadJson = "{\"name\":\"${updated.name}\",\"targetRole\":\"${updated.targetRole}\"}"
        )
        try {
            firestoreRepository?.saveUserProfile(updated)
        } catch (_: Exception) {}
    }

    suspend fun updateSkillStatus(nodeId: String, newStatus: SkillStatus) {
        val all = db.skillRoadmapDao().getAllNodesFlow().firstOrNull() ?: emptyList()
        val node = all.find { it.id == nodeId } ?: return
        val updated = node.copy(
            status = newStatus,
            updatedAt = System.currentTimeMillis(),
            version = node.version + 1,
            syncState = SyncState.PENDING_SYNC
        )
        db.skillRoadmapDao().upsertNode(updated)
        syncManager.recordLocalChange(
            entityType = "skillRoadmaps",
            recordId = updated.id,
            operation = "UPSERT",
            payloadJson = "{\"id\":\"${updated.id}\",\"status\":\"${updated.status}\"}"
        )
        try {
            firestoreRepository?.saveRoadmapNode(updated)
        } catch (_: Exception) {}
    }

    suspend fun runJobDescriptionGapAnalysis(jobDescription: String): JobDescriptionAnalysisResult {
        val currentRoadmap = db.skillRoadmapDao().getAllNodesFlow().firstOrNull() ?: emptyList()
        val result = geminiService.analyzeJobDescriptionGaps(jobDescription, currentRoadmap)

        // Clear previous gaps
        db.skillRoadmapDao().clearAiGaps()

        // Apply newly identified gaps onto matching semester nodes
        for (gap in result.gapsFound) {
            // Check if matching node exists or find best node in semester
            val semNodes = currentRoadmap.filter { it.semester == gap.semester }
            val targetNode = semNodes.find { it.id == gap.nodeId }
                ?: semNodes.firstOrNull()

            if (targetNode != null) {
                db.skillRoadmapDao().flagAiGap(
                    id = targetNode.id,
                    reason = gap.whyRequired,
                    action = gap.recommendedAction,
                    priority = gap.priority.name
                )
            }
        }

        syncManager.recordLocalChange(
            entityType = "skillRoadmaps",
            recordId = "batch_jd_gap_analysis",
            operation = "UPSERT",
            payloadJson = "{\"gapsCount\":${result.gapsFound.size}}"
        )

        return result
    }

    suspend fun runResumeAtsOptimization(resumeText: String, targetRole: String): AtsEvaluationResult {
        val eval = geminiService.evaluateResumeAtsMetrics(resumeText, targetRole)
        val current = db.resumeDao().getLatestResumeFlow().firstOrNull() ?: ResumeVersion(
            id = UUID.randomUUID().toString(),
            rawText = resumeText
        )
        val updated = current.copy(
            rawText = resumeText,
            atsScore = eval.overallScore,
            targetCompany = targetRole,
            updatedAt = System.currentTimeMillis(),
            version = current.version + 1,
            syncState = SyncState.PENDING_SYNC
        )
        db.resumeDao().upsertResume(updated)
        syncManager.recordLocalChange(
            entityType = "resumeVersions",
            recordId = updated.id,
            operation = "UPSERT",
            payloadJson = "{\"atsScore\":${eval.overallScore}}"
        )
        try {
            firestoreRepository?.saveResumeVersion(updated)
        } catch (_: Exception) {}
        return eval
    }

    suspend fun conductMockInterview(
        problemPrompt: String,
        studentCode: String,
        role: String,
        topic: String,
        onChunkStream: (String) -> Unit
    ): MockInterviewEvaluation {
        val evaluation = geminiService.mockTechnicalInterviewRound(problemPrompt, studentCode, onChunkStream)
        val session = InterviewSession(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            role = role,
            topic = topic,
            questionText = problemPrompt,
            studentCode = studentCode,
            feedback = evaluation.critique,
            codeQualityScore = evaluation.codeQualityScore,
            logicScore = evaluation.logicScore,
            confidenceScore = evaluation.confidenceScore,
            syncState = SyncState.PENDING_SYNC
        )
        db.interviewDao().insertSession(session)
        syncManager.recordLocalChange(
            entityType = "interviewSessions",
            recordId = session.id,
            operation = "UPSERT",
            payloadJson = "{\"logicScore\":${session.logicScore}}"
        )
        try {
            firestoreRepository?.saveInterviewSession(session)
        } catch (_: Exception) {}
        return evaluation
    }

    suspend fun getPanicButtonTriageTasks(friction: String, semester: Int): List<PanicTriageTask> {
        return geminiService.panicButtonMicroTasks(friction, semester)
    }

    suspend fun addDsaProblem(problem: DsaProblem) {
        db.dsaDao().upsertProblem(problem)
        syncManager.recordLocalChange(
            entityType = "dsaTracker",
            recordId = problem.id,
            operation = "UPSERT",
            payloadJson = "{\"title\":\"${problem.title}\",\"status\":\"${problem.status}\"}"
        )
        try {
            firestoreRepository?.saveDsaProblem(problem)
        } catch (_: Exception) {}
    }

    suspend fun updateDsaStatus(id: String, newStatus: ProblemStatus) {
        val problems = db.dsaDao().getAllProblemsFlow().firstOrNull() ?: return
        val item = problems.find { it.id == id } ?: return
        val updated = item.copy(status = newStatus, version = item.version + 1)
        db.dsaDao().upsertProblem(updated)
        syncManager.recordLocalChange(
            entityType = "dsaTracker",
            recordId = id,
            operation = "UPSERT",
            payloadJson = "{\"id\":\"$id\",\"newStatus\":\"$newStatus\"}"
        )
    }

    suspend fun parseAndSaveTimetable(rawInput: String): com.example.data.model.ParsedTimetableResultDto {
        val result = geminiService.parseUnstructuredTimetable(rawInput)
        val entitySlots = result.slots.map { s ->
            com.example.data.model.StudyTimetableSlot(
                id = s.id,
                dayOfWeek = s.dayOfWeek,
                startTime = s.startTime,
                endTime = s.endTime,
                topic = s.topic,
                activityType = s.activityType,
                cronSchedule = s.cronSchedule,
                streakCount = 1,
                isCompletedToday = false,
                priority = s.priority,
                rationale = s.rationale
            )
        }
        db.timetableDao().upsertSlots(entitySlots)
        syncManager.recordLocalChange(
            entityType = "studyTimetable",
            recordId = "batch_timetable_${System.currentTimeMillis()}",
            operation = "UPSERT",
            payloadJson = "{\"slotsCount\":${entitySlots.size}}"
        )
        try {
            for (slot in entitySlots) {
                firestoreRepository?.saveTimetableSlot(slot)
            }
        } catch (_: Exception) {}
        return result
    }

    suspend fun markTimetableSlotCompleted(slotId: String) {
        db.timetableDao().markSlotCompleted(slotId)
        syncManager.recordLocalChange(
            entityType = "studyTimetable",
            recordId = slotId,
            operation = "UPSERT",
            payloadJson = "{\"completed\":true}"
        )
    }

    suspend fun toggleTimetableSlotCompleted(slotId: String) {
        db.timetableDao().toggleSlotCompleted(slotId)
        syncManager.recordLocalChange(
            entityType = "studyTimetable",
            recordId = slotId,
            operation = "UPSERT",
            payloadJson = "{\"toggled\":true}"
        )
    }

    suspend fun resetDailyGoals() {
        db.timetableDao().resetDailyGoals()
        syncManager.recordLocalChange(
            entityType = "studyTimetable",
            recordId = "reset_all",
            operation = "UPSERT",
            payloadJson = "{\"reset\":true}"
        )
    }

    suspend fun recordCognitiveGameScore(score: com.example.data.model.CognitiveGameScore) {
        db.cognitiveGameDao().insertScore(score)
        syncManager.recordLocalChange(
            entityType = "cognitiveGames",
            recordId = score.id,
            operation = "UPSERT",
            payloadJson = "{\"score\":${score.score},\"combo\":${score.comboMultiplier}}"
        )
        try {
            firestoreRepository?.saveCognitiveGameScore(score)
        } catch (_: Exception) {}
    }

    suspend fun sendCareerMentorMessage(
        userPrompt: String,
        memoryContext: String,
        onChunkStream: (String) -> Unit
    ): String {
        val userMsg = com.example.data.model.ChatbotMemoryMessage(
            id = "msg_usr_${System.currentTimeMillis()}",
            role = "user",
            messageText = userPrompt,
            timestamp = System.currentTimeMillis()
        )
        db.chatbotMemoryDao().insertMessage(userMsg)
        try {
            firestoreRepository?.saveChatbotMessage(userMsg)
        } catch (_: Exception) {}

        val responseText = geminiService.streamCareerMentorResponse(userPrompt, memoryContext, onChunkStream)

        val modelMsg = com.example.data.model.ChatbotMemoryMessage(
            id = "msg_mod_${System.currentTimeMillis()}",
            role = "model",
            messageText = responseText,
            timestamp = System.currentTimeMillis()
        )
        db.chatbotMemoryDao().insertMessage(modelMsg)
        try {
            firestoreRepository?.saveChatbotMessage(modelMsg)
        } catch (_: Exception) {}

        syncManager.recordLocalChange(
            entityType = "chatbotMemory",
            recordId = modelMsg.id,
            operation = "UPSERT",
            payloadJson = "{\"length\":${responseText.length}}"
        )

        return responseText
    }

    private fun generateCuratedBTechRoadmap(): List<SkillRoadmapNode> {
        return listOf(
            // Semester 1
            SkillRoadmapNode(
                id = "sem1_c_fundamentals",
                semester = 1,
                subject = "CS101",
                category = "Core CS",
                title = "C & Memory Pointers Mastery",
                description = "Master memory layout, pointers, stack vs heap, structs, and bitwise arithmetic.",
                status = SkillStatus.COMPLETED
            ),
            SkillRoadmapNode(
                id = "sem1_math_discrete",
                semester = 1,
                subject = "MA101",
                category = "Foundations",
                title = "Discrete Mathematics & Logic",
                description = "Propositional logic, set theory, graph foundations, and combinatorics.",
                status = SkillStatus.COMPLETED
            ),
            // Semester 2
            SkillRoadmapNode(
                id = "sem2_oop_cpp_java",
                semester = 2,
                subject = "CS201",
                category = "Core CS",
                title = "OOP Paradigms (C++ / Java)",
                description = "Encapsulation, polymorphism, inheritance, RAII, smart pointers, and SOLID design.",
                status = SkillStatus.COMPLETED
            ),
            SkillRoadmapNode(
                id = "sem2_basic_dsa",
                semester = 2,
                subject = "CS202",
                category = "DSA",
                title = "Linear Data Structures",
                description = "Dynamic Arrays, Singly/Doubly Linked Lists, Stacks, Queues, and Two-pointer fundamentals.",
                status = SkillStatus.COMPLETED
            ),
            // Semester 3
            SkillRoadmapNode(
                id = "sem3_adv_dsa",
                semester = 3,
                subject = "CS301",
                category = "DSA",
                title = "Non-Linear DSA (Trees & Heaps)",
                description = "Binary Search Trees, AVL Trees, Heaps/Priority Queues, Trie, and Recursion trees.",
                status = SkillStatus.IN_PROGRESS
            ),
            SkillRoadmapNode(
                id = "sem3_web_dev_fullstack",
                semester = 3,
                subject = "CS302",
                category = "Dev & Web",
                title = "Full-Stack Web Architectures",
                description = "REST APIs, asynchronous event loop, TypeScript, React 19, and database indexing.",
                status = SkillStatus.IN_PROGRESS
            ),
            // Semester 4
            SkillRoadmapNode(
                id = "sem4_os_threads",
                semester = 4,
                subject = "CS401",
                category = "Core CS",
                title = "Operating Systems & Concurrency",
                description = "Processes, POSIX threads, synchronization primitives (mutex, semaphore), and virtual memory paging.",
                status = SkillStatus.NOT_STARTED
            ),
            SkillRoadmapNode(
                id = "sem4_dbms_internals",
                semester = 4,
                subject = "CS402",
                category = "Core CS",
                title = "DBMS & B-Tree Indexing",
                description = "ACID properties, MVCC, B+ tree file structures, WAL logging, and query execution plans.",
                status = SkillStatus.NOT_STARTED
            ),
            // Semester 5
            SkillRoadmapNode(
                id = "sem5_dist_systems",
                semester = 5,
                subject = "CS501",
                category = "Systems",
                title = "Distributed Systems & Cloud",
                description = "CAP theorem, Paxos/Raft consensus, microservices, gRPC, Docker, and AWS basics.",
                status = SkillStatus.NOT_STARTED
            ),
            SkillRoadmapNode(
                id = "sem5_graphs_dp",
                semester = 5,
                subject = "CS502",
                category = "DSA",
                title = "Advanced Graphs & Dynamic Programming",
                description = "Dijkstra, Bellman-Ford, Tarjan's SCC, Bitmask DP, and Knapsack variations.",
                status = SkillStatus.NOT_STARTED
            ),
            // Semester 6
            SkillRoadmapNode(
                id = "sem6_system_design",
                semester = 6,
                subject = "CS601",
                category = "System Design",
                title = "High-Level System Design (HLD)",
                description = "Designing YouTube, Rate Limiters, Distributed Caching (Redis), Load Balancers, and Kafka pipelines.",
                status = SkillStatus.NOT_STARTED
            ),
            SkillRoadmapNode(
                id = "sem6_container_orchestration",
                semester = 6,
                subject = "CS602",
                category = "DevOps",
                title = "DevOps, CI/CD & Kubernetes",
                description = "Kubernetes pods/services, GitHub Actions CI/CD workflows, Terraform IaC, and Prometheus telemetry.",
                status = SkillStatus.NOT_STARTED
            ),
            // Semester 7
            SkillRoadmapNode(
                id = "sem7_placement_grind",
                semester = 7,
                subject = "CS701",
                category = "Placements",
                title = "Mock Interviews & SDE Sheet Sprint",
                description = "Striver SDE Sheet complete revision, speed coding rounds, behavior STAR framework.",
                status = SkillStatus.NOT_STARTED
            ),
            SkillRoadmapNode(
                id = "sem7_ats_resume_portfolio",
                semester = 7,
                subject = "CS702",
                category = "Placements",
                title = "ATS Resume Polish & Open Source",
                description = "Google XYZ bullet resume format, portfolio hosting, cold outreach to engineering managers.",
                status = SkillStatus.NOT_STARTED
            ),
            // Semester 8
            SkillRoadmapNode(
                id = "sem8_capstone_prod",
                semester = 8,
                subject = "CS801",
                category = "Production",
                title = "Production Capstone & Internship",
                description = "Large-scale industry production deployment, latency benchmarks, open-source maintainership.",
                status = SkillStatus.NOT_STARTED
            )
        )
    }

    private fun generateCuratedDsaQuestions(): List<DsaProblem> {
        return listOf(
            DsaProblem(
                id = "p1",
                title = "Two Sum (Hash Map Indexing)",
                platform = "LeetCode 1",
                difficulty = ProblemDifficulty.EASY,
                topic = "Arrays & Hashing",
                status = ProblemStatus.SOLVED,
                timeComplexity = "O(n)",
                spaceComplexity = "O(n)",
                notes = "One-pass hash map storing complement index."
            ),
            DsaProblem(
                id = "p2",
                title = "Trapping Rain Water",
                platform = "LeetCode 42",
                difficulty = ProblemDifficulty.HARD,
                topic = "Two Pointers",
                status = ProblemStatus.REVISING,
                timeComplexity = "O(n)",
                spaceComplexity = "O(1)",
                notes = "Maintains leftMax and rightMax pointers moving inwards based on height comparison."
            ),
            DsaProblem(
                id = "p3",
                title = "Course Schedule II (Topological Sort)",
                platform = "LeetCode 210",
                difficulty = ProblemDifficulty.MEDIUM,
                topic = "Graphs",
                status = ProblemStatus.SOLVED,
                timeComplexity = "O(V + E)",
                spaceComplexity = "O(V + E)",
                notes = "Kahn's Algorithm utilizing indegree array and BFS queue."
            ),
            DsaProblem(
                id = "p4",
                title = "Coin Change (Unbounded Knapsack)",
                platform = "LeetCode 322",
                difficulty = ProblemDifficulty.MEDIUM,
                topic = "Dynamic Programming",
                status = ProblemStatus.STUCK,
                timeComplexity = "O(amount * coins)",
                spaceComplexity = "O(amount)",
                notes = "Bottom-up 1D DP table initialized to amount + 1."
            ),
            DsaProblem(
                id = "p5",
                title = "Lowest Common Ancestor of Binary Tree",
                platform = "LeetCode 236",
                difficulty = ProblemDifficulty.MEDIUM,
                topic = "Binary Trees",
                status = ProblemStatus.SOLVED,
                timeComplexity = "O(n)",
                spaceComplexity = "O(h)",
                notes = "Post-order recursive traversal checking left and right subtree matches."
            )
        )
    }
}
