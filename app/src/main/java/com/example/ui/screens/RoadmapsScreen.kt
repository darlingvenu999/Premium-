package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.Milestone
import com.example.data.model.RoadmapTrack
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun RoadmapsScreen(
    roadmaps: List<RoadmapTrack>,
    selectedTrackId: String,
    onSelectTrack: (String) -> Unit,
    onToggleMilestone: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = roadmaps.find { it.id == selectedTrackId } ?: roadmaps.first()
    var expandedMilestoneId by remember { mutableStateOf<String?>(track.milestones.firstOrNull { !it.isCompleted }?.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                Text(
                    text = "Career Roadmaps",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50
                )
                Text(
                    text = "Structured milestones designed to get you hired at top tech firms",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        // Horizontal Track Selector Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roadmaps) { item ->
                    val isSelected = item.id == track.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectTrack(item.id)
                                expandedMilestoneId = item.milestones.firstOrNull { !it.isCompleted }?.id
                            }
                            .testTag("track_pill_${item.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) IndigoPrimary else Slate900,
                        border = if (isSelected) BorderStroke(1.dp, BorderGlass) else BorderStroke(1.dp, Slate800)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = if (isSelected) Color.White else Slate400,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Track Banner Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Slate900,
                    borderColor = Color(track.colorHex).copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GradientPill(
                            text = track.tag.uppercase(),
                            gradient = listOf(Color(track.colorHex), IndigoPrimary)
                        )
                        Text(
                            text = "${track.estimatedWeeks} Weeks • ${track.milestones.size} Modules",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = track.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Track Completion",
                            color = Slate400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(track.progress * 100).toInt()}% (${track.completedCount}/${track.milestones.size})",
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    GlowingProgressBar(
                        progress = track.progress,
                        gradient = listOf(Color(track.colorHex), CyanNeon)
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Track Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate50,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        // Milestone Items
        items(track.milestones) { milestone ->
            val isExpanded = expandedMilestoneId == milestone.id
            MilestoneItemCard(
                milestone = milestone,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedMilestoneId = if (isExpanded) null else milestone.id
                },
                onToggleComplete = {
                    onToggleMilestone(track.id, milestone.id)
                }
            )
        }
    }
}

@Composable
private fun MilestoneItemCard(
    milestone: Milestone,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleComplete: () -> Unit
) {
    var selectedQuizOption by remember { mutableStateOf<Int?>(null) }
    var quizSubmitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (milestone.isCompleted) Slate900.copy(alpha = 0.6f) else Slate900,
            borderColor = if (milestone.isCompleted) EmeraldSuccess.copy(alpha = 0.4f) else BorderGlass,
            onClick = onToggleExpand
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Checkbox / Completion Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (milestone.isCompleted) EmeraldSuccess.copy(alpha = 0.2f) else Slate800
                        )
                        .clickable(onClick = onToggleComplete)
                        .testTag("toggle_milestone_${milestone.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (milestone.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "In progress",
                            tint = IndigoLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (milestone.isCompleted) Slate400 else Color.White
                        )
                        Text(
                            text = milestone.duration,
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = milestone.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded details: Key topics & mini-quiz
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = Slate800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Key Concepts to Master:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanNeon
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        milestone.keyTopics.forEach { topic ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate800
                            ) {
                                Text(
                                    text = topic,
                                    color = Slate200,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Mini Quiz Section
                    if (milestone.quizQuestion != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = AmberGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Knowledge Checkpoint (+50 XP)",
                                        color = AmberGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = milestone.quizQuestion,
                                    color = Slate50,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                milestone.quizOptions.forEachIndexed { index, option ->
                                    val isSelected = selectedQuizOption == index
                                    val isCorrect = index == milestone.quizCorrectIndex

                                    val optionColor = when {
                                        quizSubmitted && isCorrect -> EmeraldSuccess
                                        quizSubmitted && isSelected && !isCorrect -> RoseHighlight
                                        isSelected -> IndigoLight
                                        else -> Slate800
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = optionColor.copy(alpha = if (isSelected || (quizSubmitted && isCorrect)) 0.25f else 0.5f),
                                        border = BorderStroke(1.dp, optionColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable(enabled = !quizSubmitted) {
                                                selectedQuizOption = index
                                            }
                                    ) {
                                        Text(
                                            text = option,
                                            color = Slate50,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                if (!quizSubmitted) {
                                    Button(
                                        onClick = {
                                            quizSubmitted = true
                                            if (selectedQuizOption == milestone.quizCorrectIndex && !milestone.isCompleted) {
                                                onToggleComplete()
                                            }
                                        },
                                        enabled = selectedQuizOption != null,
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Submit Answer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    val wasCorrect = selectedQuizOption == milestone.quizCorrectIndex
                                    Text(
                                        text = if (wasCorrect) "🎉 Correct! +50 XP Awarded." else "💡 Incorrect. Review the concept above!",
                                        color = if (wasCorrect) EmeraldSuccess else AmberGlow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onToggleComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (milestone.isCompleted) Slate800 else EmeraldSuccess
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (milestone.isCompleted) "Mark as Incomplete" else "Mark as Completed ✓",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
