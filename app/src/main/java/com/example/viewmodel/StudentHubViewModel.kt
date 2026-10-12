package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.StudentHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HubTab(val label: String) {
    DASHBOARD("Home"),
    ROADMAPS("Roadmaps"),
    HACKATHONS("Hackathons"),
    INTERNSHIPS("Internships"),
    INCUBATOR("Incubator"),
    AI_TUTOR("AI Tutor"),
    PROFILE("Profile")
}

class StudentHubViewModel(
    private val repository: StudentHubRepository = StudentHubRepository()
) : ViewModel() {

    val roadmaps: StateFlow<List<RoadmapTrack>> = repository.roadmaps
    val hackathons: StateFlow<List<Hackathon>> = repository.hackathons
    val internships: StateFlow<List<Internship>> = repository.internships
    val projects: StateFlow<List<ProjectIdea>> = repository.projects
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val profile: StateFlow<StudentProfile> = repository.profile
    val isAiThinking: StateFlow<Boolean> = repository.isThinking
    val isApiKeyConfigured: StateFlow<Boolean> = repository.isApiKeyConfigured

    private val _selectedModel = MutableStateFlow(com.example.data.api.GeminiApiClient.DEFAULT_MODEL)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _currentTab = MutableStateFlow(HubTab.DASHBOARD)
    val currentTab: StateFlow<HubTab> = _currentTab.asStateFlow()

    private val _selectedTrackId = MutableStateFlow("ai-ml")
    val selectedTrackId: StateFlow<String> = _selectedTrackId.asStateFlow()

    private val _hackathonFilter = MutableStateFlow("All")
    val hackathonFilter: StateFlow<String> = _hackathonFilter.asStateFlow()

    private val _internshipFilter = MutableStateFlow("All")
    val internshipFilter: StateFlow<String> = _internshipFilter.asStateFlow()

    private val _projectFilter = MutableStateFlow("All")
    val projectFilter: StateFlow<String> = _projectFilter.asStateFlow()

    fun selectTab(tab: HubTab) {
        _currentTab.value = tab
    }

    fun selectTrack(trackId: String) {
        _selectedTrackId.value = trackId
    }

    fun setHackathonFilter(filter: String) {
        _hackathonFilter.value = filter
    }

    fun setInternshipFilter(filter: String) {
        _internshipFilter.value = filter
    }

    fun setProjectFilter(filter: String) {
        _projectFilter.value = filter
    }

    fun toggleMilestone(trackId: String, milestoneId: String) {
        viewModelScope.launch {
            repository.toggleMilestoneCompletion(trackId, milestoneId)
        }
    }

    fun toggleHackathonBookmark(id: String) {
        viewModelScope.launch {
            repository.toggleHackathonBookmark(id)
        }
    }

    fun toggleHackathonRegistered(id: String) {
        viewModelScope.launch {
            repository.toggleHackathonRegistered(id)
        }
    }

    fun updateInternshipStage(id: String, stage: ApplicationStage) {
        viewModelScope.launch {
            repository.updateInternshipStage(id, stage)
        }
    }

    fun addInternship(company: String, role: String, location: String, comp: String, workType: String) {
        viewModelScope.launch {
            repository.addCustomInternship(company, role, location, comp, workType)
        }
    }

    fun toggleProjectSave(id: String) {
        viewModelScope.launch {
            repository.toggleProjectSave(id)
        }
    }

    fun updateProjectStatus(id: String, status: ProjectStatus) {
        viewModelScope.launch {
            repository.updateProjectStatus(id, status)
        }
    }

    fun generateProjectIdea(topic: String, tech: String) {
        viewModelScope.launch {
            repository.generateAIProjectIdea(topic, tech)
        }
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun clearChat() {
        repository.clearChat()
    }

    fun sendChatPrompt(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(prompt, _selectedModel.value)
        }
    }
}
