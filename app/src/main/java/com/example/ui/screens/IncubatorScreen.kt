package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import com.example.data.model.ProjectIdea
import com.example.data.model.ProjectStatus
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun IncubatorScreen(
    projects: List<ProjectIdea>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onUpdateStatus: (String, ProjectStatus) -> Unit,
    onGenerateIdea: (String, String) -> Unit,
    initialHackathonTopic: String? = null,
    modifier: Modifier = Modifier
) {
    var showGeneratorModal by remember { mutableStateOf(initialHackathonTopic != null) }
    var selectedDomain by remember { mutableStateOf(initialHackathonTopic ?: "AI & Agents") }
    var selectedTech by remember { mutableStateOf("Kotlin & Compose") }

    val filterOptions = listOf("All", "Saved", "Building", "Shipped")
    val filteredProjects = projects.filter { proj ->
        when (filter) {
            "All" -> true
            "Saved" -> proj.isSaved
            "Building" -> proj.status == ProjectStatus.BUILDING
            "Shipped" -> proj.status == ProjectStatus.SHIPPED
            else -> true
        }
    }

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
                    text = "Project Incubator",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50
                )
                Text(
                    text = "High-signal project specs with architectural blueprints recruiters love",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        // AI Generator Trigger Banner
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(IndigoLight, CyanNeon))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGeneratorModal = true }
                        .testTag("open_ai_idea_generator")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(IndigoPrimary, VioletAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Blueprint Architect",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Generate a tailored MVP architecture and user stories for your target role.",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Status Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { opt ->
                    val isSelected = opt == filter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFilterChange(opt) }
                            .testTag("project_filter_$opt"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) IndigoPrimary else Slate900,
                        border = if (isSelected) BorderStroke(1.dp, BorderGlass) else BorderStroke(1.dp, Slate800)
                    ) {
                        Text(
                            text = opt,
                            color = if (isSelected) Color.White else Slate400,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Project Cards
        items(filteredProjects) { project ->
            ProjectCard(
                project = project,
                onToggleSave = { onToggleSave(project.id) },
                onUpdateStatus = { status -> onUpdateStatus(project.id, status) }
            )
        }
    }

    // AI Blueprint Generator Modal
    if (showGeneratorModal) {
        val domainOptions = listOf("AI & Agents", "Cloud & Infra", "FinTech", "Developer Tooling", "Mobile Systems")
        val techOptions = listOf("Kotlin & Compose", "Python & PyTorch", "TypeScript & Next.js", "Go & Docker", "Rust")

        AlertDialog(
            onDismissRequest = { showGeneratorModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VioletGlow)
                    Text("Generate Project Blueprint", color = Slate50, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onGenerateIdea(selectedDomain, selectedTech)
                        showGeneratorModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Architect Project (+50 XP)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeneratorModal = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Domain:", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(domainOptions) { d ->
                            val isSel = d == selectedDomain
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) IndigoPrimary else Slate800,
                                modifier = Modifier.clickable { selectedDomain = d }
                            ) {
                                Text(d, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Target Tech Stack:", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(techOptions) { t ->
                            val isSel = t == selectedTech
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) CyanNeon.copy(alpha = 0.4f) else Slate800,
                                border = if (isSel) BorderStroke(1.dp, CyanNeon) else null,
                                modifier = Modifier.clickable { selectedTech = t }
                            ) {
                                Text(t, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ProjectCard(
    project: ProjectIdea,
    onToggleSave: () -> Unit,
    onUpdateStatus: (ProjectStatus) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Slate900,
            borderColor = if (project.status == ProjectStatus.SHIPPED) EmeraldSuccess.copy(alpha = 0.5f) else BorderGlass,
            onClick = { isExpanded = !isExpanded }
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (project.difficulty) {
                        "Advanced" -> RoseHighlight.copy(alpha = 0.2f)
                        "Intermediate" -> AmberStreak.copy(alpha = 0.2f)
                        else -> EmeraldSuccess.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = project.difficulty,
                        color = when (project.difficulty) {
                            "Advanced" -> RoseHighlight
                            "Intermediate" -> AmberGlow
                            else -> EmeraldSuccess
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800
                    ) {
                        Text(
                            text = project.status.label,
                            color = when (project.status) {
                                ProjectStatus.SHIPPED -> EmeraldSuccess
                                ProjectStatus.BUILDING -> CyanNeon
                                ProjectStatus.IDEATION -> Slate400
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (project.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (project.isSaved) AmberGlow else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = project.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(10.dp))
            // Tech stack pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                project.techStack.forEach { tech ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate850
                    ) {
                        Text(
                            text = tech,
                            color = CyanNeon,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Expanded Architectural Deep Dive
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Slate800)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = project.description, color = Slate200, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Key Architectural Features:", color = IndigoLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    project.keyFeatures.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text("⚡", fontSize = 10.sp)
                            Text(feature, color = Slate400, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate950,
                        border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Why Recruiters Love It:", color = VioletGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(project.whyRecruitersLoveIt, color = Slate200, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Status advancement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Update Progress:", color = Slate400, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProjectStatus.values().forEach { st ->
                                val isCur = project.status == st
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCur) IndigoPrimary else Slate800,
                                    modifier = Modifier.clickable { onUpdateStatus(st) }
                                ) {
                                    Text(
                                        text = st.label,
                                        color = if (isCur) Color.White else Slate400,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
