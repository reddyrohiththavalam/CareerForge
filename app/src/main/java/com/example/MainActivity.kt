/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.SyncEngineStatus
import com.example.ui.MainViewModel
import com.example.ui.components.PanicButtonModal
import com.example.ui.screens.BigOGameScreen
import com.example.ui.screens.CareerChatbotScreen
import com.example.ui.screens.DsaScreen
import com.example.ui.screens.InterviewScreen
import com.example.ui.screens.ProfileSyncScreen
import com.example.ui.screens.ResumeScreen
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                val isSigningIn by viewModel.isSigningIn.collectAsState()
                val authError by viewModel.authError.collectAsState()

                if (currentUser == null) {
                    SignInScreen(
                        isLoading = isSigningIn,
                        errorMessage = authError,
                        onSignInWithGoogle = { viewModel.signInWithGoogle() }
                    )
                } else {
                    MainAppContent(
                        viewModel = viewModel,
                        user = currentUser,
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    user: FirebaseUser?,
    onSignOut: () -> Unit
) {
    var currentNavIndex by remember { mutableIntStateOf(0) }

    val studentProfile by viewModel.studentProfile.collectAsState()
    val roadmapNodes by viewModel.roadmapNodes.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val dsaProblems by viewModel.dsaProblems.collectAsState()
    val latestResume by viewModel.latestResume.collectAsState()

    val syncStatus by viewModel.syncStatus.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val simulatedOffline by viewModel.simulatedOffline.collectAsState()

    val isAnalyzingJd by viewModel.isAnalyzingJd.collectAsState()
    val jdAnalysisResult by viewModel.jdAnalysisResult.collectAsState()

    val isEvaluatingAts by viewModel.isEvaluatingAts.collectAsState()
    val atsResult by viewModel.atsResult.collectAsState()

    val isEvaluatingInterview by viewModel.isEvaluatingInterview.collectAsState()
    val streamingLog by viewModel.streamingInterviewLog.collectAsState()
    val interviewEvaluation by viewModel.interviewEvaluation.collectAsState()

    val isPanicModalOpen by viewModel.isPanicModalOpen.collectAsState()
    val isPanicLoading by viewModel.isPanicLoading.collectAsState()
    val panicMicroTasks by viewModel.panicMicroTasks.collectAsState()

    val timetableSlots by viewModel.timetableSlots.collectAsState()
    val isParsingTimetable by viewModel.isParsingTimetable.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isStreamingMentor by viewModel.isStreamingMentor.collectAsState()
    val streamingMentorText by viewModel.streamingMentorText.collectAsState()

    var dsaSubTab by remember { mutableIntStateOf(0) }
    var aiSubTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        Triple("Roadmap", Icons.Default.Map, "nav_roadmap"),
        Triple("DSA & Big-O", Icons.Default.Code, "nav_dsa"),
        Triple("Schedule", Icons.Default.DateRange, "nav_timetable"),
        Triple("AI Mentor", Icons.Default.Psychology, "nav_interview"),
        Triple("ATS Studio", Icons.Default.Description, "nav_resume"),
        Triple("Profile", Icons.Default.Person, "nav_profile")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CareerForge",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "BTech Profile Accelerator",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    // Sync Status Badge
                    Surface(
                        onClick = { currentNavIndex = 5 },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(end = 4.dp).testTag("sync_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        when (syncStatus) {
                                            is SyncEngineStatus.FullySynced -> Color(0xFF10B981)
                                            is SyncEngineStatus.Syncing -> Color(0xFF38BDF8)
                                            is SyncEngineStatus.OfflineBuffered -> Color(0xFFF59E0B)
                                            is SyncEngineStatus.Error -> Color(0xFFEF4444)
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when (syncStatus) {
                                    is SyncEngineStatus.FullySynced -> "Synced"
                                    is SyncEngineStatus.Syncing -> "Syncing"
                                    is SyncEngineStatus.OfflineBuffered -> "Offline"
                                    is SyncEngineStatus.Error -> "Err"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Emergency Panic Button Quick Action
                    IconButton(
                        onClick = { viewModel.openPanicModal() },
                        modifier = Modifier.testTag("topbar_panic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = "Panic Triage",
                            tint = Color(0xFFFB7185)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F19))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentNavIndex == index,
                        onClick = { currentNavIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.first,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        label = { Text(item.first, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag(item.third)
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentNavIndex != 5) { // Don't block profile form
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openPanicModal() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    text = { Text("Panic / Reset", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) },
                    containerColor = Color(0xFFE11D48),
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier.testTag("floating_panic_button")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B0F19))
        ) {
            when (currentNavIndex) {
                0 -> RoadmapScreen(
                    roadmapNodes = roadmapNodes,
                    selectedSemester = selectedSemester,
                    isAnalyzingJd = isAnalyzingJd,
                    jdAnalysisResult = jdAnalysisResult,
                    onSelectSemester = { viewModel.setSelectedSemester(it) },
                    onToggleStatus = { id, status -> viewModel.toggleSkillStatus(id, status) },
                    onAnalyzeJd = { jd -> viewModel.analyzeJobDescription(jd) }
                )
                1 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = dsaSubTab,
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color(0xFF38BDF8),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[dsaSubTab]),
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        ) {
                            Tab(
                                selected = dsaSubTab == 0,
                                onClick = { dsaSubTab = 0 },
                                text = { Text("DSA Problems (${dsaProblems.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = dsaSubTab == 1,
                                onClick = { dsaSubTab = 1 },
                                text = { Text("Big-O Agility 🎮", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                        }
                        if (dsaSubTab == 0) {
                            DsaScreen(
                                problems = dsaProblems,
                                onAddProblem = { viewModel.addDsaProblem(it) },
                                onToggleStatus = { id, status -> viewModel.toggleDsaStatus(id, status) }
                            )
                        } else {
                            BigOGameScreen(
                                onRecordScore = { viewModel.recordGameScore(it) }
                            )
                        }
                    }
                }
                2 -> TimetableScreen(
                    slots = timetableSlots,
                    isParsing = isParsingTimetable,
                    onParseTimetable = { viewModel.parseAndSaveTimetable(it) },
                    onMarkCompleted = { viewModel.toggleTimetableSlotCompleted(it) },
                    onResetDailyGoals = { viewModel.resetDailyGoals() }
                )
                3 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = aiSubTab,
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color(0xFF38BDF8),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[aiSubTab]),
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        ) {
                            Tab(
                                selected = aiSubTab == 0,
                                onClick = { aiSubTab = 0 },
                                text = { Text("Mock Technical Interview", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = aiSubTab == 1,
                                onClick = { aiSubTab = 1 },
                                text = { Text("Staff Mentor Console 💬", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                        }
                        if (aiSubTab == 0) {
                            InterviewScreen(
                                isEvaluating = isEvaluatingInterview,
                                streamingLog = streamingLog,
                                evaluationResult = interviewEvaluation,
                                onSubmitCode = { code, lang, title, topic ->
                                    viewModel.submitMockInterview(code, lang, title, topic)
                                }
                            )
                        } else {
                            CareerChatbotScreen(
                                profile = studentProfile,
                                messages = chatMessages,
                                isStreaming = isStreamingMentor,
                                streamingText = streamingMentorText,
                                onSendMessage = { viewModel.sendMentorMessage(it) }
                            )
                        }
                    }
                }
                4 -> ResumeScreen(
                    currentResume = latestResume,
                    isEvaluatingAts = isEvaluatingAts,
                    atsResult = atsResult,
                    onOptimizeResume = { resumeText, role -> viewModel.optimizeResume(resumeText, role) }
                )
                5 -> ProfileSyncScreen(
                    profile = studentProfile,
                    syncStatus = syncStatus,
                    isOnline = isOnline,
                    simulatedOffline = simulatedOffline,
                    userEmail = user?.email,
                    onSignOut = onSignOut,
                    onToggleSimulatedOffline = { viewModel.toggleSimulatedOffline(it) },
                    onForceSync = { viewModel.forceCloudSync() },
                    onSaveProfile = { viewModel.saveStudentProfile(it) }
                )
            }

            // Panic Button Intervention Modal
            PanicButtonModal(
                isOpen = isPanicModalOpen,
                currentSemester = studentProfile?.currentSemester ?: 5,
                isLoading = isPanicLoading,
                microTasks = panicMicroTasks,
                onTriggerPanic = { friction -> viewModel.triggerPanicTriage(friction) },
                onDismiss = { viewModel.closePanicModal() }
            )
        }
    }
}
