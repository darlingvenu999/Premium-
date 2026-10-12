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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicationStage
import com.example.data.model.Internship
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun InternshipsScreen(
    internships: List<Internship>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onUpdateStage: (String, ApplicationStage) -> Unit,
    onAddInternship: (String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("Explore") } // "Explore" or "Pipeline"
    var showAddDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Remote", "Hybrid", "Onsite")
    val filteredList = internships.filter { item ->
        if (filter == "All") true else item.workType.equals(filter, ignoreCase = true)
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
                    text = "Tech Internships",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50
                )
                Text(
                    text = "Curated roles at high-growth startups & FAANG with built-in pipeline tracker",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        // SubTab Switcher (Explore vs My Pipeline)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Explore", "My Pipeline").forEach { subTab ->
                    val isSelected = activeSubTab == subTab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { activeSubTab = subTab }
                            .testTag("internship_subtab_$subTab"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IndigoPrimary else Color.Transparent
                    ) {
                        Text(
                            text = subTab,
                            color = if (isSelected) Color.White else Slate400,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        if (activeSubTab == "Explore") {
            // Work Type Filter Chips
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
                                .testTag("internship_filter_$opt"),
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

            // Results summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} Opportunities",
                        color = Slate400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Updated Daily",
                        color = EmeraldSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // List of Job Cards
            items(filteredList) { job ->
                InternshipCard(
                    job = job,
                    onUpdateStage = { stage -> onUpdateStage(job.id, stage) }
                )
            }
        } else {
            // My Pipeline Tab
            val savedCount = internships.count { it.stage == ApplicationStage.SAVED }
            val appliedCount = internships.count { it.stage == ApplicationStage.APPLIED }
            val interviewCount = internships.count { it.stage == ApplicationStage.INTERVIEWING }
            val offerCount = internships.count { it.stage == ApplicationStage.OFFER }

            // Pipeline Summary Metric Cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PipelineStatTile(label = "Saved", count = savedCount, color = Slate400, modifier = Modifier.weight(1f))
                        PipelineStatTile(label = "Applied", count = appliedCount, color = IndigoLight, modifier = Modifier.weight(1f))
                        PipelineStatTile(label = "Interviews", count = interviewCount, color = AmberGlow, modifier = Modifier.weight(1f))
                        PipelineStatTile(label = "Offers", count = offerCount, color = EmeraldSuccess, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_custom_application_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Tracked Application", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tracked Applications List
            val myApps = internships.filter { it.stage != ApplicationStage.SAVED }
            if (myApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No active applications yet", color = Slate400, fontSize = 14.sp)
                            Text("Explore listings and click 'Apply' or add a custom company above!", color = Slate600, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(myApps) { job ->
                    InternshipCard(
                        job = job,
                        onUpdateStage = { stage -> onUpdateStage(job.id, stage) }
                    )
                }
            }
        }
    }

    // Add Application Dialog
    if (showAddDialog) {
        var comp by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("Remote") }
        var pay by remember { mutableStateOf("$50 / hr") }
        var type by remember { mutableStateOf("Remote") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Track New Application", color = Slate50, fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        if (comp.isNotBlank() && role.isNotBlank()) {
                            onAddInternship(comp, role, location, pay, type)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Save to Pipeline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = comp,
                        onValueChange = { comp = it },
                        label = { Text("Company Name (e.g. Apple)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role Title (e.g. iOS Intern)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pay,
                        onValueChange = { pay = it },
                        label = { Text("Expected Compensation") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun PipelineStatTile(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Slate900,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Slate400,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InternshipCard(
    job: Internship,
    onUpdateStage: (ApplicationStage) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Slate900,
            borderColor = when (job.stage) {
                ApplicationStage.OFFER -> EmeraldSuccess.copy(alpha = 0.6f)
                ApplicationStage.INTERVIEWING -> AmberGlow.copy(alpha = 0.5f)
                else -> BorderGlass
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Company Monogram
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = job.company.take(2).uppercase(),
                        color = CyanNeon,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.company,
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Current Stage Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(job.stage.colorHex).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(job.stage.colorHex))
                        ) {
                            Text(
                                text = job.stage.label,
                                color = Color(job.stage.colorHex),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = job.role,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${job.location} • ${job.workType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💵 ${job.compensation}",
                    color = AmberGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deadline: ${job.deadline}",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate800, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Application Pipeline Stage Mover Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Move Stage:",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ApplicationStage.values().forEach { stage ->
                        val isCurrent = job.stage == stage
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrent) Color(stage.colorHex) else Slate800,
                            modifier = Modifier
                                .clickable { onUpdateStage(stage) }
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = stage.label.take(5),
                                color = if (isCurrent) Color.White else Slate400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
