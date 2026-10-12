package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    profile: StudentProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var focusModeEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Back bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Slate50)
                }
                Text(
                    text = "Developer Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50
                )
            }
        }

        // Profile Identity Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Slate900
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Monogram Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(IndigoPrimary, CyanNeon))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AR",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = profile.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                            Text(
                                text = "GPA ${profile.gpa} • Top 3% of builders",
                                color = EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate800)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Verified Technical Skills:", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        profile.topSkills.take(4).forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate800
                            ) {
                                Text(
                                    text = skill,
                                    color = CyanNeon,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats Summary Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Career & Study Telemetry",
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox("🔥 Streak", "${profile.streakDays} Days", AmberGlow, Modifier.weight(1f))
                    MetricBox("⏱️ Study Time", "${profile.totalHoursStudied}h", IndigoLight, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox("⭐ Level", "Level ${profile.level}", CyanNeon, Modifier.weight(1f))
                    MetricBox("🎯 Total XP", "${profile.currentXp} XP", VioletGlow, Modifier.weight(1f))
                }
            }
        }

        // Achievements Section
        item {
            Text(
                text = "Earned Trophies & Badges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate50,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
            )
        }

        items(profile.badges) { badge ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Slate900,
                    borderColor = if (badge.isUnlocked) AmberStreak.copy(alpha = 0.3f) else Slate800
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = badge.emoji, fontSize = 26.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.title,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) Color.White else Slate400,
                                fontSize = 14.sp
                            )
                            Text(
                                text = badge.description,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (badge.isUnlocked) EmeraldSuccess.copy(alpha = 0.2f) else Slate800
                        ) {
                            Text(
                                text = if (badge.isUnlocked) "UNLOCKED" else badge.progress,
                                color = if (badge.isUnlocked) EmeraldSuccess else Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Preferences & Controls
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Study Streak Notifications", color = Slate50, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Daily reminders before streak expires", color = Slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Slate800)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Deep Work Focus Mode", color = Slate50, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Mutes distracting non-critical alerts", color = Slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = focusModeEnabled,
                            onCheckedChange = { focusModeEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanNeon)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Slate900,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
