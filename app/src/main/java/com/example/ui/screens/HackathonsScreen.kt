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
import com.example.data.model.Hackathon
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HackathonsScreen(
    hackathons: List<Hackathon>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleRegister: (String) -> Unit,
    onGenerateIdeaForHackathon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedHackathonForDetails by remember { mutableStateOf<Hackathon?>(null) }

    val categories = listOf("All", "AI / LLM", "Student Only", "Android", "FinTech")
    val filtered = hackathons.filter { item ->
        val matchesCategory = when (filter) {
            "All" -> true
            else -> item.tags.any { it.contains(filter, ignoreCase = true) }
        }
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.organizer.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
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
                    text = "Hackathon Radar",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate50
                )
                Text(
                    text = "Compete in global developer challenges, win bounties & build your portfolio",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        // Search bar
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search hackathons, AI sprints, themes...", color = Slate400, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hackathon_search_input")
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == filter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFilterChange(cat) }
                            .testTag("hackathon_filter_$cat"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) IndigoPrimary else Slate900,
                        border = if (isSelected) BorderStroke(1.dp, BorderGlass) else BorderStroke(1.dp, Slate800)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Slate400,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // List Header Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filtered.size} Competitions Open",
                    color = Slate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sorted by Urgency",
                    color = CyanNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Hackathon Cards
        items(filtered) { hack ->
            HackathonCard(
                hackathon = hack,
                onCardClick = { selectedHackathonForDetails = hack },
                onToggleBookmark = { onToggleBookmark(hack.id) },
                onToggleRegister = { onToggleRegister(hack.id) }
            )
        }
    }

    // Details Modal / Dialog
    if (selectedHackathonForDetails != null) {
        val h = selectedHackathonForDetails!!
        AlertDialog(
            onDismissRequest = { selectedHackathonForDetails = null },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleRegister(h.id)
                        selectedHackathonForDetails = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (h.isRegistered) Slate800 else EmeraldSuccess
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (h.isRegistered) "Leave Team" else "Register / Join (+100 XP)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onGenerateIdeaForHackathon(h.title)
                        selectedHackathonForDetails = null
                    }
                ) {
                    Text("💡 Brainstorm Project", color = CyanNeon, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Slate900,
            title = {
                Text(text = h.title, color = Slate50, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prize Pool: ${h.prizePool}", color = AmberGlow, fontWeight = FontWeight.Bold)
                        Text(h.dates, color = Slate400, fontSize = 12.sp)
                    }
                    Text("Organizer: ${h.organizer}", color = Slate200, fontSize = 13.sp)
                    Text("Location: ${h.location}", color = Slate400, fontSize = 12.sp)
                    Text("Team Size: ${h.teamRequirement}", color = IndigoLight, fontSize = 12.sp)
                    HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 4.dp))
                    Text(h.description, color = Slate200, fontSize = 13.sp)
                }
            }
        )
    }
}

@Composable
private fun HackathonCard(
    hackathon: Hackathon,
    onCardClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Slate900,
            borderColor = if (hackathon.isRegistered) EmeraldSuccess.copy(alpha = 0.5f) else BorderGlass,
            onClick = onCardClick
        ) {
            // Top badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GradientPill(
                    text = "🏆 ${hackathon.prizePool}",
                    gradient = listOf(AmberStreak, RoseHighlight)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (hackathon.daysLeft <= 7) RoseHighlight.copy(alpha = 0.2f) else Slate800
                    ) {
                        Text(
                            text = "Starts in ${hackathon.daysLeft}d",
                            color = if (hackathon.daysLeft <= 7) RoseHighlight else CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (hackathon.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (hackathon.isBookmarked) AmberGlow else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hackathon.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate50
            )
            Text(
                text = "${hackathon.organizer} • ${hackathon.location}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(10.dp))
            // Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                hackathon.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate850
                    ) {
                        Text(
                            text = tag,
                            color = Slate200,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 ${hackathon.teamRequirement}",
                    color = Slate400,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onToggleRegister,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hackathon.isRegistered) Slate800 else IndigoPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (hackathon.isRegistered) "Registered ✓" else "Register Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (hackathon.isRegistered) EmeraldSuccess else Color.White
                    )
                }
            }
        }
    }
}
