package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.HubTab

@Composable
fun DashboardScreen(
    profile: StudentProfile,
    roadmaps: List<RoadmapTrack>,
    hackathons: List<Hackathon>,
    internships: List<Internship>,
    onNavigateTab: (HubTab) -> Unit,
    onSelectTrack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTrack = roadmaps.firstOrNull { it.id == "ai-ml" } ?: roadmaps.first()
    val nextMilestone = activeTrack.milestones.firstOrNull { !it.isCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Visual Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_student_hub),
                    contentDescription = "Student Hub Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Slate950.copy(alpha = 0.6f),
                                    Slate950
                                )
                            )
                        )
                )

                // Overlay Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    GradientPill(
                        text = "STUDENT DEVELOPER ACCELERATOR",
                        gradient = listOf(IndigoPrimary, CyanNeon)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Build. Learn. Compete.",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your personalized cockpit for high-impact tech careers",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200
                    )
                }
            }
        }

        // Student Level & XP Progress Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Slate900
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Level ${profile.level}",
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGlow,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "• Senior Architect Tier",
                                    color = Slate400,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${profile.currentXp} / ${profile.targetXp} XP to Level ${profile.level + 1}",
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Circular XP badge
                        Surface(
                            shape = CircleShape,
                            color = IndigoPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, IndigoLight)
                        ) {
                            Text(
                                text = "+50 XP/Task",
                                color = IndigoLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val xpFraction = (profile.currentXp.toFloat() / profile.targetXp.toFloat()).coerceIn(0f, 1f)
                    GlowingProgressBar(
                        progress = xpFraction,
                        gradient = listOf(AmberStreak, IndigoLight, CyanNeon)
                    )
                }
            }
        }

        // Quick Action Shortcuts Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Quick Launch",
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Learning Track",
                        subtitle = "${(activeTrack.progress * 100).toInt()}% Done",
                        icon = Icons.Default.AltRoute,
                        color = IndigoLight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSelectTrack(activeTrack.id)
                            onNavigateTab(HubTab.ROADMAPS)
                        }
                    )
                    QuickActionTile(
                        title = "AI CS Tutor",
                        subtitle = "Instant Answers",
                        icon = Icons.Default.AutoAwesome,
                        color = VioletGlow,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(HubTab.AI_TUTOR) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Hack Radar",
                        subtitle = "${hackathons.size} Live Sprints",
                        icon = Icons.Default.EmojiEvents,
                        color = AmberGlow,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(HubTab.HACKATHONS) }
                    )
                    QuickActionTile(
                        title = "Job Pipeline",
                        subtitle = "1 Offer Active",
                        icon = Icons.Default.Work,
                        color = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(HubTab.INTERNSHIPS) }
                    )
                }
            }
        }

        // Weekly Study Activity Pulse
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Study Cadence",
                                style = MaterialTheme.typography.titleMedium,
                                color = Slate50,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile.totalHoursStudied}h logged this semester",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }

                        GradientPill(
                            text = "🔥 ${profile.streakDays} Days",
                            gradient = listOf(AmberStreak, RoseHighlight)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimal sleek bar chart
                    val days = listOf("M" to 3.5f, "T" to 5.0f, "W" to 2.8f, "T" to 6.2f, "F" to 4.0f, "S" to 7.5f, "S" to 5.2f)
                    val maxVal = 8f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEach { (day, hours) ->
                            val heightFraction = (hours / maxVal).coerceIn(0.15f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${hours}h",
                                    fontSize = 9.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(CyanNeon, IndigoPrimary)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = Slate200,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Roadmap Up Next Card
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SectionHeader(
                    title = "Active Career Track",
                    subtitle = activeTrack.title,
                    actionText = "All Tracks",
                    onActionClick = { onNavigateTab(HubTab.ROADMAPS) }
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = IndigoLight.copy(alpha = 0.3f),
                    onClick = {
                        onSelectTrack(activeTrack.id)
                        onNavigateTab(HubTab.ROADMAPS)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Milestone",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeTrack.completedCount}/${activeTrack.milestones.size} Completed",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextMilestone?.title ?: "All Milestones Completed! 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = nextMilestone?.description ?: "You've finished this track. Check out other roadmaps!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onSelectTrack(activeTrack.id)
                            onNavigateTab(HubTab.ROADMAPS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_learning_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Continue Lesson & Quiz", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Hot Upcoming Hackathon Spotlight
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SectionHeader(
                    title = "Upcoming Hackathons",
                    subtitle = "High-prize developer challenges",
                    actionText = "Explore All",
                    onActionClick = { onNavigateTab(HubTab.HACKATHONS) }
                )

                val hotHack = hackathons.firstOrNull()
                if (hotHack != null) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateTab(HubTab.HACKATHONS) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GradientPill(
                                text = "🏆 ${hotHack.prizePool} PRIZE",
                                gradient = listOf(AmberStreak, RoseHighlight)
                            )
                            Text(
                                text = "Starts in ${hotHack.daysLeft} days",
                                color = CyanNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = hotHack.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate50
                        )
                        Text(
                            text = hotHack.organizer + " • " + hotHack.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("quick_action_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        color = Slate900,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate50
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }
    }
}
