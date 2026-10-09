/*
 * Copyright (C) 2026 CareerForge Open-Source Project
 * Licensed under the GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)
 */

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.StudentProfile
import com.example.data.sync.SyncEngineStatus

@Composable
fun ProfileSyncScreen(
    profile: StudentProfile?,
    syncStatus: SyncEngineStatus,
    isOnline: Boolean,
    simulatedOffline: Boolean,
    userEmail: String? = null,
    onSignOut: () -> Unit = {},
    onToggleSimulatedOffline: (Boolean) -> Unit,
    onForceSync: () -> Unit,
    onSaveProfile: (StudentProfile) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "Rohit Sharma") }
    var college by remember(profile) { mutableStateOf(profile?.college ?: "NIT Trichy") }
    var branch by remember(profile) { mutableStateOf(profile?.branch ?: "CSE") }
    var semester by remember(profile) { mutableIntStateOf(profile?.currentSemester ?: 5) }
    var targetRole by remember(profile) { mutableStateOf(profile?.targetRole ?: "Backend Systems SDE") }
    var targetTier by remember(profile) { mutableStateOf(profile?.targetTier ?: "Tier 1 Product / FinTech") }
    var leetCodeUsername by remember(profile) { mutableStateOf(profile?.leetCodeUsername ?: "rohit_codes") }
    var githubUsername by remember(profile) { mutableStateOf(profile?.githubUsername ?: "rohit-dev") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("profile_sync_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Sync Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline && !simulatedOffline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline && !simulatedOffline) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Offline-First CRDT Sync Pipeline",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = when (syncStatus) {
                                        is SyncEngineStatus.FullySynced -> "🟢 100% Up to Date (Local + Cloud)"
                                        is SyncEngineStatus.Syncing -> "🔄 Streaming CDC buffer (${syncStatus.pendingCount} items)..."
                                        is SyncEngineStatus.OfflineBuffered -> "🟡 Offline Queue Active (${syncStatus.pendingCount} mutations)"
                                        is SyncEngineStatus.Error -> "❌ Sync Error: ${syncStatus.message}"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Button(
                            onClick = onForceSync,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.testTag("force_sync_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Drain", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Offline Mode Switch
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Simulate Offline Disconnection",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tests local mutation buffer and deferred merge.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = simulatedOffline,
                                onCheckedChange = onToggleSimulatedOffline,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFF59E0B),
                                    checkedTrackColor = Color(0xFF78350F)
                                ),
                                modifier = Modifier.testTag("simulated_offline_switch")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student Profile Form
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF131A2A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BTech Student Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = college,
                            onValueChange = { college = it },
                            label = { Text("Engineering College / NIT / IIT") },
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = branch,
                            onValueChange = { branch = it },
                            label = { Text("Branch") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = semester.toString(),
                            onValueChange = { semester = it.toIntOrNull()?.coerceIn(1, 8) ?: semester },
                            label = { Text("Current Sem (1-8)") },
                            modifier = Modifier.weight(1f).testTag("profile_sem_input")
                        )
                        OutlinedTextField(
                            value = targetTier,
                            onValueChange = { targetTier = it },
                            label = { Text("Target Tier") },
                            modifier = Modifier.weight(2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = targetRole,
                        onValueChange = { targetRole = it },
                        label = { Text("Target Placement Role (e.g. Distributed Systems SDE)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = leetCodeUsername,
                            onValueChange = { leetCodeUsername = it },
                            label = { Text("LeetCode Handle") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = githubUsername,
                            onValueChange = { githubUsername = it },
                            label = { Text("GitHub Handle") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val updated = (profile ?: StudentProfile()).copy(
                                name = name,
                                college = college,
                                branch = branch,
                                currentSemester = semester,
                                targetRole = targetRole,
                                targetTier = targetTier,
                                leetCodeUsername = leetCodeUsername,
                                githubUsername = githubUsername
                            )
                            onSaveProfile(updated)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Persist Profile to Local Database", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!userEmail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF131A2A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Authenticated with Google",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = userEmail,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Text("Sign Out", fontSize = 11.sp, color = Color(0xFFFCA5A5))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AGPL-3.0 License & Open Source Attestation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AGPL-3.0-or-later Licensed Core",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CareerForge is strictly free and open-source software built for BTech engineering students. Designed with local-first offline resilience, Lamport logical clocks, and Google Gemini SDK intelligence.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
