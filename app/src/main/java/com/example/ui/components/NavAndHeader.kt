package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.viewmodel.HubTab

@Composable
fun StudentHubTopBar(
    profile: StudentProfile,
    onProfileClick: () -> Unit,
    onAiTutorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Slate950,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand & Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(IndigoPrimary, CyanNeon))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Student",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate50
                        )
                        Text(
                            text = "Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyanNeon
                        )
                    }
                    Text(
                        text = "Level ${profile.level} • Master Builder",
                        fontSize = 11.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right side status pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, AmberStreak.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("streak_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔥", fontSize = 12.sp)
                        Text(
                            text = "${profile.streakDays}d",
                            color = AmberGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // AI Tutor Floating Shortcut
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = VioletAccent.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, VioletAccent),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onAiTutorClick)
                        .testTag("topbar_ai_tutor")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Tutor",
                            tint = VioletGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "AI Tutor",
                            color = VioletGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Profile Avatar Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .border(1.5.dp, IndigoPrimary, CircleShape)
                        .clickable(onClick = onProfileClick)
                        .testTag("topbar_profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AR",
                        color = Slate50,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StudentHubBottomNav(
    currentTab: HubTab,
    onTabSelected: (HubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavigationItem(HubTab.DASHBOARD, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "Home"),
        NavigationItem(HubTab.ROADMAPS, Icons.Filled.AltRoute, Icons.Outlined.AltRoute, "Tracks"),
        NavigationItem(HubTab.HACKATHONS, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "Hacks"),
        NavigationItem(HubTab.INTERNSHIPS, Icons.Filled.Work, Icons.Outlined.WorkOutline, "Jobs"),
        NavigationItem(HubTab.INCUBATOR, Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, "Ideas")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = Slate950,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, Slate850)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { item ->
                val isSelected = currentTab == item.tab
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) IndigoLight else Slate400,
                    label = "tab_color"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${item.tab.name}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .padding(horizontal = if (isSelected) 8.dp else 0.dp)
                            .background(
                                color = if (isSelected) IndigoPrimary.copy(alpha = 0.2f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = animatedColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = animatedColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val tab: HubTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)
