package com.example.data.model

data class Milestone(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val isCompleted: Boolean = false,
    val keyTopics: List<String> = emptyList(),
    val quizQuestion: String? = null,
    val quizOptions: List<String> = emptyList(),
    val quizCorrectIndex: Int = 0
)

data class RoadmapTrack(
    val id: String,
    val title: String,
    val tag: String,
    val iconName: String,
    val description: String,
    val colorHex: Long,
    val milestones: List<Milestone>,
    val estimatedWeeks: Int
) {
    val completedCount: Int get() = milestones.count { it.isCompleted }
    val progress: Float get() = if (milestones.isEmpty()) 0f else completedCount.toFloat() / milestones.size
}

data class Hackathon(
    val id: String,
    val title: String,
    val organizer: String,
    val dates: String,
    val daysLeft: Int,
    val prizePool: String,
    val location: String,
    val tags: List<String>,
    val isBookmarked: Boolean = false,
    val isRegistered: Boolean = false,
    val description: String,
    val teamRequirement: String,
    val bannerColor: Long = 0xFF6366F1
)

enum class ApplicationStage(val label: String, val colorHex: Long) {
    SAVED("Saved", 0xFF64748B),
    APPLIED("Applied", 0xFF3B82F6),
    INTERVIEWING("Interviewing", 0xFFF59E0B),
    OFFER("Offer Received", 0xFF10B981)
}

data class Internship(
    val id: String,
    val company: String,
    val role: String,
    val location: String,
    val compensation: String,
    val workType: String, // Remote, Hybrid, Onsite
    val tags: List<String>,
    val deadline: String,
    val stage: ApplicationStage = ApplicationStage.SAVED,
    val description: String,
    val requirements: List<String>,
    val dateApplied: String? = null
)

enum class ProjectStatus(val label: String) {
    IDEATION("Ideation"),
    BUILDING("Building"),
    SHIPPED("Shipped")
}

data class ProjectIdea(
    val id: String,
    val title: String,
    val subtitle: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val domain: String,     // AI, Mobile, SaaS, DevTools, Systems
    val techStack: List<String>,
    val description: String,
    val keyFeatures: List<String>,
    val whyRecruitersLoveIt: String,
    val isSaved: Boolean = false,
    val status: ProjectStatus = ProjectStatus.IDEATION
)

enum class SenderType {
    USER, AI
}

data class ChatMessage(
    val id: String,
    val sender: SenderType,
    val text: String,
    val codeSnippet: String? = null,
    val language: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val progress: String = "100%"
)

data class StudentProfile(
    val name: String = "Alex Rivera",
    val title: String = "Junior CS @ Tech Institute",
    val gpa: String = "3.92",
    val level: Int = 8,
    val currentXp: Int = 2450,
    val targetXp: Int = 3000,
    val streakDays: Int = 14,
    val totalHoursStudied: Double = 68.5,
    val badges: List<AchievementBadge> = emptyList(),
    val topSkills: List<String> = listOf("Kotlin", "Jetpack Compose", "Python", "PyTorch", "Docker", "System Design"),
    val githubUrl: String = "github.com/alexrivera-dev",
    val portfolioUrl: String = "alexrivera.tech"
)
