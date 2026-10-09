/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.GoogleAuthManager
import com.example.data.local.AppDatabase
import com.example.data.model.AtsEvaluationResult
import com.example.data.model.DsaProblem
import com.example.data.model.JobDescriptionAnalysisResult
import com.example.data.model.MockInterviewEvaluation
import com.example.data.model.PanicTriageTask
import com.example.data.model.ProblemStatus
import com.example.data.model.ResumeVersion
import com.example.data.model.SkillRoadmapNode
import com.example.data.model.SkillStatus
import com.example.data.model.StudentProfile
import com.example.data.remote.GeminiService
import com.example.data.repository.CareerRepository
import com.example.data.sync.SyncEngineStatus
import com.example.data.sync.SyncManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val geminiService = GeminiService()
    val authManager = GoogleAuthManager(application)
    val firestoreRepository = FirestoreRepository(application)
    val syncManager = SyncManager(application, db, viewModelScope)
    val repository = CareerRepository(db, syncManager, geminiService, firestoreRepository)

    // Auth state flow (Single source of truth)
    val currentUser: StateFlow<FirebaseUser?> = authManager.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.currentUser)

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val studentProfile: StateFlow<StudentProfile?> = repository.studentProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roadmapNodes: StateFlow<List<SkillRoadmapNode>> = repository.allRoadmapNodesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dsaProblems: StateFlow<List<DsaProblem>> = repository.allDsaProblemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestResume: StateFlow<ResumeVersion?> = repository.latestResumeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timetableSlots: StateFlow<List<com.example.data.model.StudyTimetableSlot>> = repository.allTimetableSlotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameScores: StateFlow<List<com.example.data.model.CognitiveGameScore>> = repository.allGameScoresFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<com.example.data.model.ChatbotMemoryMessage>> = repository.chatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncStatus: StateFlow<SyncEngineStatus> = syncManager.syncStatus
    val isOnline: StateFlow<Boolean> = syncManager.isOnline
    val simulatedOffline: StateFlow<Boolean> = syncManager.simulatedOffline

    // UI state filters
    private val _selectedSemester = MutableStateFlow<Int?>(null)
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

    // Timetable state
    private val _isParsingTimetable = MutableStateFlow(false)
    val isParsingTimetable: StateFlow<Boolean> = _isParsingTimetable.asStateFlow()

    // Chatbot state
    private val _isStreamingMentor = MutableStateFlow(false)
    val isStreamingMentor: StateFlow<Boolean> = _isStreamingMentor.asStateFlow()

    private val _streamingMentorText = MutableStateFlow("")
    val streamingMentorText: StateFlow<String> = _streamingMentorText.asStateFlow()

    // JD Analyzer state
    private val _isAnalyzingJd = MutableStateFlow(false)
    val isAnalyzingJd: StateFlow<Boolean> = _isAnalyzingJd.asStateFlow()

    private val _jdAnalysisResult = MutableStateFlow<JobDescriptionAnalysisResult?>(null)
    val jdAnalysisResult: StateFlow<JobDescriptionAnalysisResult?> = _jdAnalysisResult.asStateFlow()

    // ATS Resume state
    private val _isEvaluatingAts = MutableStateFlow(false)
    val isEvaluatingAts: StateFlow<Boolean> = _isEvaluatingAts.asStateFlow()

    private val _atsResult = MutableStateFlow<AtsEvaluationResult?>(null)
    val atsResult: StateFlow<AtsEvaluationResult?> = _atsResult.asStateFlow()

    // Mock Interview state
    private val _isEvaluatingInterview = MutableStateFlow(false)
    val isEvaluatingInterview: StateFlow<Boolean> = _isEvaluatingInterview.asStateFlow()

    private val _streamingInterviewLog = MutableStateFlow("")
    val streamingInterviewLog: StateFlow<String> = _streamingInterviewLog.asStateFlow()

    private val _interviewEvaluation = MutableStateFlow<MockInterviewEvaluation?>(null)
    val interviewEvaluation: StateFlow<MockInterviewEvaluation?> = _interviewEvaluation.asStateFlow()

    // Panic Button state
    private val _isPanicModalOpen = MutableStateFlow(false)
    val isPanicModalOpen: StateFlow<Boolean> = _isPanicModalOpen.asStateFlow()

    private val _isPanicLoading = MutableStateFlow(false)
    val isPanicLoading: StateFlow<Boolean> = _isPanicLoading.asStateFlow()

    private val _panicMicroTasks = MutableStateFlow<List<PanicTriageTask>>(emptyList())
    val panicMicroTasks: StateFlow<List<PanicTriageTask>> = _panicMicroTasks.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfEmpty()
        }
    }

    fun setSelectedSemester(semester: Int?) {
        _selectedSemester.value = semester
    }

    fun toggleSkillStatus(nodeId: String, newStatus: SkillStatus) {
        viewModelScope.launch {
            repository.updateSkillStatus(nodeId, newStatus)
        }
    }

    fun analyzeJobDescription(jd: String) {
        viewModelScope.launch {
            _isAnalyzingJd.value = true
            try {
                val result = repository.runJobDescriptionGapAnalysis(jd)
                _jdAnalysisResult.value = result
            } finally {
                _isAnalyzingJd.value = false
            }
        }
    }

    fun optimizeResume(resumeText: String, targetRole: String) {
        viewModelScope.launch {
            _isEvaluatingAts.value = true
            try {
                val result = repository.runResumeAtsOptimization(resumeText, targetRole)
                _atsResult.value = result
            } finally {
                _isEvaluatingAts.value = false
            }
        }
    }

    fun submitMockInterview(code: String, language: String, problemTitle: String, topic: String) {
        viewModelScope.launch {
            _isEvaluatingInterview.value = true
            _streamingInterviewLog.value = "Connecting to Gemini Staff Engineer Evaluator..."
            _interviewEvaluation.value = null

            try {
                val eval = repository.conductMockInterview(
                    problemPrompt = problemTitle,
                    studentCode = code,
                    role = "Backend Systems SDE",
                    topic = topic,
                    onChunkStream = { chunk ->
                        _streamingInterviewLog.value += "\n$chunk"
                    }
                )
                _interviewEvaluation.value = eval
            } finally {
                _isEvaluatingInterview.value = false
            }
        }
    }

    fun openPanicModal() {
        _isPanicModalOpen.value = true
    }

    fun closePanicModal() {
        _isPanicModalOpen.value = false
    }

    fun triggerPanicTriage(frictionSource: String) {
        viewModelScope.launch {
            _isPanicLoading.value = true
            try {
                val sem = studentProfile.value?.currentSemester ?: 5
                val tasks = repository.getPanicButtonTriageTasks(frictionSource, sem)
                _panicMicroTasks.value = tasks
            } finally {
                _isPanicLoading.value = false
            }
        }
    }

    fun saveStudentProfile(profile: StudentProfile) {
        viewModelScope.launch {
            repository.updateStudentProfile(profile)
        }
    }

    fun addDsaProblem(problem: DsaProblem) {
        viewModelScope.launch {
            repository.addDsaProblem(problem)
        }
    }

    fun toggleDsaStatus(id: String, newStatus: ProblemStatus) {
        viewModelScope.launch {
            repository.updateDsaStatus(id, newStatus)
        }
    }

    fun toggleSimulatedOffline(forceOffline: Boolean) {
        syncManager.toggleSimulatedOffline(forceOffline)
    }

    fun forceCloudSync() {
        syncManager.triggerDumpingPendingBuffer()
    }

    fun parseAndSaveTimetable(prompt: String) {
        viewModelScope.launch {
            _isParsingTimetable.value = true
            try {
                repository.parseAndSaveTimetable(prompt)
            } finally {
                _isParsingTimetable.value = false
            }
        }
    }

    fun markTimetableSlotCompleted(id: String) {
        viewModelScope.launch {
            repository.markTimetableSlotCompleted(id)
        }
    }

    fun toggleTimetableSlotCompleted(id: String) {
        viewModelScope.launch {
            repository.toggleTimetableSlotCompleted(id)
        }
    }

    fun resetDailyGoals() {
        viewModelScope.launch {
            repository.resetDailyGoals()
        }
    }

    fun recordGameScore(score: com.example.data.model.CognitiveGameScore) {
        viewModelScope.launch {
            repository.recordCognitiveGameScore(score)
        }
    }

    fun sendMentorMessage(prompt: String) {
        viewModelScope.launch {
            _isStreamingMentor.value = true
            _streamingMentorText.value = ""
            try {
                val profile = studentProfile.value
                val memory = "Student: ${profile?.name ?: "BTech Student"}, Sem: ${profile?.currentSemester ?: 5}, Target: ${profile?.targetRole ?: "SDE"}, Tier: ${profile?.targetTier ?: "Tier 1"}"
                repository.sendCareerMentorMessage(
                    userPrompt = prompt,
                    memoryContext = memory,
                    onChunkStream = { chunk ->
                        _streamingMentorText.value += chunk
                    }
                )
            } finally {
                _isStreamingMentor.value = false
                _streamingMentorText.value = ""
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authError.value = null
            val result = authManager.signInWithGoogle()
            result.onFailure { error ->
                _authError.value = error.localizedMessage ?: "Sign-in cancelled or failed"
            }
            _isSigningIn.value = false
        }
    }

    fun signOut() {
        authManager.signOut()
    }
}
