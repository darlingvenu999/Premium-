package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StudentHubBottomNav
import com.example.ui.components.StudentHubTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950
import com.example.viewmodel.HubTab
import com.example.viewmodel.StudentHubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudentHubApp()
            }
        }
    }
}

@Composable
fun StudentHubApp(
    viewModel: StudentHubViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val roadmaps by viewModel.roadmaps.collectAsStateWithLifecycle()
    val hackathons by viewModel.hackathons.collectAsStateWithLifecycle()
    val internships by viewModel.internships.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedTrackId by viewModel.selectedTrackId.collectAsStateWithLifecycle()
    val hackathonFilter by viewModel.hackathonFilter.collectAsStateWithLifecycle()
    val internshipFilter by viewModel.internshipFilter.collectAsStateWithLifecycle()
    val projectFilter by viewModel.projectFilter.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

    var brainstormHackathonTopic by remember { mutableStateOf<String?>(null) }

    // Predictive back handler: return to dashboard if on sub-screens
    BackHandler(enabled = currentTab != HubTab.DASHBOARD) {
        viewModel.selectTab(HubTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950),
        topBar = {
            if (currentTab != HubTab.PROFILE) {
                StudentHubTopBar(
                    profile = profile,
                    onProfileClick = { viewModel.selectTab(HubTab.PROFILE) },
                    onAiTutorClick = { viewModel.selectTab(HubTab.AI_TUTOR) }
                )
            }
        },
        bottomBar = {
            if (currentTab != HubTab.PROFILE) {
                StudentHubBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        },
        containerColor = Slate950
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    HubTab.DASHBOARD -> DashboardScreen(
                        profile = profile,
                        roadmaps = roadmaps,
                        hackathons = hackathons,
                        internships = internships,
                        onNavigateTab = { viewModel.selectTab(it) },
                        onSelectTrack = { viewModel.selectTrack(it) }
                    )
                    HubTab.ROADMAPS -> RoadmapsScreen(
                        roadmaps = roadmaps,
                        selectedTrackId = selectedTrackId,
                        onSelectTrack = { viewModel.selectTrack(it) },
                        onToggleMilestone = { trackId, mId -> viewModel.toggleMilestone(trackId, mId) }
                    )
                    HubTab.HACKATHONS -> HackathonsScreen(
                        hackathons = hackathons,
                        filter = hackathonFilter,
                        onFilterChange = { viewModel.setHackathonFilter(it) },
                        onToggleBookmark = { viewModel.toggleHackathonBookmark(it) },
                        onToggleRegister = { viewModel.toggleHackathonRegistered(it) },
                        onGenerateIdeaForHackathon = { topic ->
                            brainstormHackathonTopic = topic
                            viewModel.selectTab(HubTab.INCUBATOR)
                        }
                    )
                    HubTab.INTERNSHIPS -> InternshipsScreen(
                        internships = internships,
                        filter = internshipFilter,
                        onFilterChange = { viewModel.setInternshipFilter(it) },
                        onUpdateStage = { id, stage -> viewModel.updateInternshipStage(id, stage) },
                        onAddInternship = { comp, role, loc, pay, type ->
                            viewModel.addInternship(comp, role, loc, pay, type)
                        }
                    )
                    HubTab.INCUBATOR -> IncubatorScreen(
                        projects = projects,
                        filter = projectFilter,
                        onFilterChange = { viewModel.setProjectFilter(it) },
                        onToggleSave = { viewModel.toggleProjectSave(it) },
                        onUpdateStatus = { id, status -> viewModel.updateProjectStatus(id, status) },
                        onGenerateIdea = { domain, tech -> viewModel.generateProjectIdea(domain, tech) },
                        initialHackathonTopic = brainstormHackathonTopic
                    )
                    HubTab.AI_TUTOR -> AITutorScreen(
                        messages = chatMessages,
                        isThinking = isAiThinking,
                        isApiKeyConfigured = isApiKeyConfigured,
                        selectedModel = selectedModel,
                        onSelectModel = { viewModel.selectModel(it) },
                        onSendMessage = { viewModel.sendChatPrompt(it) },
                        onClearChat = { viewModel.clearChat() }
                    )
                    HubTab.PROFILE -> ProfileScreen(
                        profile = profile,
                        onBack = { viewModel.selectTab(HubTab.DASHBOARD) }
                    )
                }
            }
        }
    }
}
