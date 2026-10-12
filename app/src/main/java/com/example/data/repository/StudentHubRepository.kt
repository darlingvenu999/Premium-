package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class StudentHubRepository {

    private val _roadmaps = MutableStateFlow(createInitialRoadmaps())
    val roadmaps: StateFlow<List<RoadmapTrack>> = _roadmaps.asStateFlow()

    private val _hackathons = MutableStateFlow(createInitialHackathons())
    val hackathons: StateFlow<List<Hackathon>> = _hackathons.asStateFlow()

    private val _internships = MutableStateFlow(createInitialInternships())
    val internships: StateFlow<List<Internship>> = _internships.asStateFlow()

    private val _projects = MutableStateFlow(createInitialProjects())
    val projects: StateFlow<List<ProjectIdea>> = _projects.asStateFlow()

    private val _chatMessages = MutableStateFlow(createInitialMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _profile = MutableStateFlow(createInitialProfile())
    val profile: StateFlow<StudentProfile> = _profile.asStateFlow()

    private val geminiClient = com.example.data.api.GeminiApiClient()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isApiKeyConfigured = MutableStateFlow(geminiClient.isApiKeyConfigured())
    val isApiKeyConfigured: StateFlow<Boolean> = _isApiKeyConfigured.asStateFlow()

    fun toggleMilestoneCompletion(trackId: String, milestoneId: String) {
        _roadmaps.update { tracks ->
            tracks.map { track ->
                if (track.id == trackId) {
                    val updatedMilestones = track.milestones.map { milestone ->
                        if (milestone.id == milestoneId) {
                            val nextState = !milestone.isCompleted
                            if (nextState) awardXp(50)
                            milestone.copy(isCompleted = nextState)
                        } else milestone
                    }
                    track.copy(milestones = updatedMilestones)
                } else track
            }
        }
    }

    fun toggleHackathonBookmark(hackathonId: String) {
        _hackathons.update { list ->
            list.map {
                if (it.id == hackathonId) it.copy(isBookmarked = !it.isBookmarked) else it
            }
        }
    }

    fun toggleHackathonRegistered(hackathonId: String) {
        _hackathons.update { list ->
            list.map {
                if (it.id == hackathonId) {
                    val next = !it.isRegistered
                    if (next) awardXp(100)
                    it.copy(isRegistered = next)
                } else it
            }
        }
    }

    fun updateInternshipStage(internshipId: String, stage: ApplicationStage) {
        _internships.update { list ->
            list.map {
                if (it.id == internshipId) {
                    if (stage == ApplicationStage.APPLIED && it.stage != ApplicationStage.APPLIED) {
                        awardXp(75)
                    }
                    it.copy(stage = stage)
                } else it
            }
        }
    }

    fun addCustomInternship(company: String, role: String, location: String, comp: String, workType: String) {
        val newJob = Internship(
            id = UUID.randomUUID().toString(),
            company = company,
            role = role,
            location = location,
            compensation = comp,
            workType = workType,
            tags = listOf("Custom", workType),
            deadline = "Flexible",
            stage = ApplicationStage.APPLIED,
            description = "Custom tracked application entered by student.",
            requirements = listOf("Resume Submitted", "Follow up scheduled")
        )
        _internships.update { listOf(newJob) + it }
        awardXp(75)
    }

    fun toggleProjectSave(projectId: String) {
        _projects.update { list ->
            list.map {
                if (it.id == projectId) it.copy(isSaved = !it.isSaved) else it
            }
        }
    }

    fun updateProjectStatus(projectId: String, status: ProjectStatus) {
        _projects.update { list ->
            list.map {
                if (it.id == projectId) {
                    if (status == ProjectStatus.SHIPPED && it.status != ProjectStatus.SHIPPED) {
                        awardXp(200)
                    }
                    it.copy(status = status)
                } else it
            }
        }
    }

    fun generateAIProjectIdea(topic: String, tech: String) {
        val id = UUID.randomUUID().toString()
        val generated = ProjectIdea(
            id = id,
            title = "Autonomous $topic Engine",
            subtitle = "AI-Driven $topic system engineered with modern $tech",
            difficulty = "Intermediate",
            domain = "AI & Systems",
            techStack = listOf(tech, "FastAPI", "Vector DB", "Docker"),
            description = "An enterprise-grade $topic workflow engine tailored for high concurrency and real-time inference.",
            keyFeatures = listOf(
                "Real-time event processing pipeline",
                "Retrieval Augmented Generation (RAG) context engine",
                "Interactive telemetry dashboard with latency metrics",
                "Zero-downtime deployment script with Docker Compose"
            ),
            whyRecruitersLoveIt = "Demonstrates asynchronous programming, microservices resilience, and modern AI integration using $tech.",
            isSaved = true,
            status = ProjectStatus.IDEATION
        )
        _projects.update { listOf(generated) + it }
        awardXp(50)
    }

    suspend fun sendChatMessage(query: String, model: String = com.example.data.api.GeminiApiClient.DEFAULT_MODEL) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = SenderType.USER,
            text = query
        )
        _chatMessages.update { it + userMsg }
        _isThinking.value = true

        val result = geminiClient.generateTutorResponse(_chatMessages.value, query, model)
        _isThinking.value = false

        result.fold(
            onSuccess = { rawReply ->
                val (parsedText, codeInfo) = extractCodeFromMarkdown(rawReply)
                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = parsedText,
                    codeSnippet = codeInfo?.second,
                    language = codeInfo?.first
                )
                _chatMessages.update { it + aiMsg }
                awardXp(35)
            },
            onFailure = { error ->
                val isMissingKey = error is IllegalStateException && error.message == "GEMINI_API_KEY_NOT_CONFIGURED"
                val fallbackResponse = generateTutorReply(query)

                val noticeHeader = if (isMissingKey) {
                    "✨ [Offline Demo Mode — Gemini 3.5 Flash]\n(To enable live server-side generation, configure GEMINI_API_KEY in the Secrets panel)\n\n"
                } else {
                    "⚡ [Offline Assistance fallback (${error.localizedMessage ?: "Network Timeout"})]\n\n"
                }

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "$noticeHeader${fallbackResponse.text}",
                    codeSnippet = fallbackResponse.codeSnippet,
                    language = fallbackResponse.language
                )
                _chatMessages.update { it + aiMsg }
                awardXp(25)
            }
        )
    }

    fun clearChat() {
        _chatMessages.value = createInitialMessages()
    }

    private fun extractCodeFromMarkdown(raw: String): Pair<String, Pair<String, String>?> {
        val regex = Regex("```([a-zA-Z0-9_+#-]*)\\n([\\s\\S]*?)```")
        val match = regex.find(raw)
        return if (match != null) {
            val lang = match.groupValues[1].ifBlank { "code" }
            val code = match.groupValues[2].trim()
            val textClean = raw.replace(match.value, "").trim()
            Pair(textClean.ifBlank { "Here is the solution:" }, Pair(lang, code))
        } else {
            Pair(raw, null)
        }
    }


    private fun awardXp(amount: Int) {
        _profile.update { p ->
            val newXp = p.currentXp + amount
            if (newXp >= p.targetXp) {
                p.copy(
                    level = p.level + 1,
                    currentXp = newXp - p.targetXp,
                    targetXp = (p.targetXp * 1.25).toInt()
                )
            } else {
                p.copy(currentXp = newXp)
            }
        }
    }

    private fun generateTutorReply(prompt: String): ChatMessage {
        val p = prompt.lowercase()
        return when {
            p.contains("raft") || p.contains("paxos") || p.contains("consensus") -> {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "Consensus algorithms ensure distributed nodes agree on a single data value or log sequence despite network partitions.\n\nKey Raft phases:\n1. Leader Election: Nodes start as Followers with randomized timers.\n2. Log Replication: Leader receives client writes and broadcasts AppendEntries RPCs.\n3. Safety: Logs are committed once written by a quorum (majority).",
                    codeSnippet = "// Raft Node State Representation\nenum class NodeRole { FOLLOWER, CANDIDATE, LEADER }\n\ndata class LogEntry(val term: Long, val index: Long, val command: String)\n\nclass RaftNode(val id: Int) {\n    var currentTerm: Long = 0\n    var role: NodeRole = NodeRole.FOLLOWER\n    val log = mutableListOf<LogEntry>()\n}",
                    language = "kotlin"
                )
            }
            p.contains("two pointer") || p.contains("sliding window") -> {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "Great algorithmic question! \n• Two Pointers: Typically used on sorted arrays/linked lists moving towards each other (left & right) or at differing speeds (fast & slow).\n• Sliding Window: Maintains a dynamic contiguous subsegment of elements, expanding the right pointer and shrinking the left pointer when a constraint is violated.",
                    codeSnippet = "fun lengthOfLongestSubstring(s: String): Int {\n    var maxLen = 0\n    var left = 0\n    val lastSeen = HashMap<Char, Int>()\n    for (right in s.indices) {\n        val char = s[right]\n        if (lastSeen.containsKey(char)) {\n            left = maxOf(left, lastSeen[char]!! + 1)\n        }\n        lastSeen[char] = right\n        maxLen = maxOf(maxLen, right - left + 1)\n    }\n    return maxLen\n}",
                    language = "kotlin"
                )
            }
            p.contains("coroutine") || p.contains("async") || p.contains("flow") -> {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "In modern Android development, Coroutines deliver lightweight structured concurrency. Here is how to safely collect cold flows in Compose using lifecycle awareness.",
                    codeSnippet = "// Clean Coroutine & StateFlow Pattern\nclass UserViewModel(private val repository: Repo) : ViewModel() {\n    val uiState: StateFlow<UiState> = repository.dataFlow\n        .map { UiState.Success(it) }\n        .stateIn(\n            scope = viewModelScope,\n            started = SharingStarted.WhileSubscribed(5000),\n            initialValue = UiState.Loading\n        )\n}",
                    language = "kotlin"
                )
            }
            p.contains("system design") || p.contains("tinyurl") || p.contains("url") -> {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "System Design of TinyURL:\n1. Scale: 100M new URLs/day -> ~1,160 writes/sec, ~11,600 reads/sec.\n2. Hash Strategy: Base62 encoding ([a-zA-Z0-9]) on an auto-incrementing 64-bit ID yields 62^7 ≈ 3.5 Trillion combinations.\n3. Caching: Redis cluster for 80/20 hot URL lookups with LRU eviction.",
                    codeSnippet = "GET /api/v1/{shortCode} -> HTTP 302 Found (Location: targetUrl)\nPOST /api/v1/shorten\n{\n   \"url\": \"https://engineering.org/distributed-systems\",\n   \"ttl\": 86400\n}",
                    language = "json"
                )
            }
            p.contains("mock interview") || p.contains("lru") || p.contains("cache") -> {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "Let's run a Mock Interview! \nQuestion: Design an LRU Cache with O(1) get and put operations.\n\nConcept: A HashMap gives O(1) key-node lookup, while a Doubly Linked List maintains recency with O(1) node detachment and head insertion.",
                    codeSnippet = "class LRUCache(private val capacity: Int) {\n    class Node(val key: Int, var value: Int,\n               var prev: Node? = null, var next: Node? = null)\n    private val map = HashMap<Int, Node>()\n    private val head = Node(0, 0)\n    private val tail = Node(0, 0)\n    init { head.next = tail; tail.prev = head }\n}",
                    language = "kotlin"
                )
            }
            else -> {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = SenderType.AI,
                    text = "Here's the technical breakdown on '$prompt':\n1. Analyze the core invariants and time/space constraints.\n2. Modularize into decoupled components with clear interfaces.\n3. Benchmark edge cases, error handling, and thread safety.\n\nWould you like a code implementation or a quick quiz challenge on this?",
                    codeSnippet = "// Solution outline\nfun executeConcept() {\n    println(\"Executed $prompt successfully\")\n}",
                    language = "kotlin"
                )
            }
        }
    }

    private fun createInitialRoadmaps(): List<RoadmapTrack> {
        return listOf(
            RoadmapTrack(
                id = "ai-ml",
                title = "AI & Machine Learning Engineer",
                tag = "Trending",
                iconName = "Psychology",
                description = "Master deep learning, neural networks, PyTorch, RAG pipelines, and LLM orchestration.",
                colorHex = 0xFF8B5CF6,
                estimatedWeeks = 14,
                milestones = listOf(
                    Milestone(
                        id = "ml-1",
                        title = "Python, NumPy & Linear Algebra",
                        description = "Matrix transformations, vector dot products, and vectorized computation.",
                        duration = "2 Weeks",
                        isCompleted = true,
                        keyTopics = listOf("Broadcasting", "Eigenvalues", "Gradient Vectors"),
                        quizQuestion = "What operation enables arithmetic between NumPy arrays of different shapes?",
                        quizOptions = listOf("Broadcasting", "Convolution", "Vector Flattening", "Quantization"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ml-2",
                        title = "PyTorch & Deep Neural Nets",
                        description = "Backpropagation, automatic differentiation, loss functions, and activation layers.",
                        duration = "3 Weeks",
                        isCompleted = true,
                        keyTopics = listOf("Autograd", "CrossEntropyLoss", "AdamW Optimizer"),
                        quizQuestion = "Why is torch.no_grad() used during model evaluation?",
                        quizOptions = listOf("Reduces memory by skipping gradient graph calculation", "Speed up CPU clock", "Applies dropout", "Freezes weights"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ml-3",
                        title = "Transformer Architecture & Attention",
                        description = "Multi-head self-attention, positional embeddings, and encoder-decoder mechanisms.",
                        duration = "3 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("Scaled Dot-Product", "Q, K, V Projections", "LayerNorm"),
                        quizQuestion = "What prevents tokens from attending to future tokens in decoder-only LLMs?",
                        quizOptions = listOf("Causal Masking", "Layer Normalization", "Weight Decay", "Beam Search"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ml-4",
                        title = "Vector DBs & RAG Architecture",
                        description = "Embedding generation, cosine similarity, Chroma/Pinecone, and chunking strategies.",
                        duration = "3 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("SentenceTransformers", "HNSW Indexing", "Hybrid Search"),
                        quizQuestion = "Which vector index algorithm balances fast search speed with recall accuracy?",
                        quizOptions = listOf("HNSW (Hierarchical Navigable Small World)", "B-Tree", "Binary Search", "R-Tree"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ml-5",
                        title = "Model Serving & Quantization",
                        description = "vLLM, Ollama, GGUF/AWQ quantization, and production latency optimization.",
                        duration = "3 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("PagedAttention", "Tensor Parallelism", "Quantization 4-bit"),
                        quizQuestion = "What innovation in vLLM optimizes KV-cache memory allocation?",
                        quizOptions = listOf("PagedAttention", "Zero Redundancy", "Model Sharding", "Speculative Decoding"),
                        quizCorrectIndex = 0
                    )
                )
            ),
            RoadmapTrack(
                id = "fullstack",
                title = "Fullstack Modern Web",
                tag = "High Demand",
                iconName = "Code",
                description = "Modern TypeScript, Next.js, reactive state, REST/GraphQL APIs, PostgreSQL, and scalable auth.",
                colorHex = 0xFF6366F1,
                estimatedWeeks = 12,
                milestones = listOf(
                    Milestone(
                        id = "fs-1",
                        title = "TypeScript & Modern Async JS",
                        description = "Generics, mapped types, utility types, event loops, and Promises.",
                        duration = "2 Weeks",
                        isCompleted = true,
                        keyTopics = listOf("Utility Types", "Event Loop Microtasks", "Async/Await"),
                        quizQuestion = "Which TypeScript utility type constructs a type with all properties of T set to optional?",
                        quizOptions = listOf("Partial<T>", "Required<T>", "Pick<T>", "Omit<T>"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "fs-2",
                        title = "Next.js App Router & Server Components",
                        description = "SSR, SSG, streaming suspense, server actions, and client hydration.",
                        duration = "3 Weeks",
                        isCompleted = true,
                        keyTopics = listOf("RSC", "Server Actions", "Streaming with Suspense"),
                        quizQuestion = "Where do React Server Components execute?",
                        quizOptions = listOf("Exclusively on the server", "In the client browser", "In a Web Worker", "In Service Worker"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "fs-3",
                        title = "PostgreSQL, Prisma & DB Design",
                        description = "Relational schema indexing, ACID transactions, migrations, and query profiling.",
                        duration = "3 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("B-Tree Indexes", "Foreign Keys", "N+1 Query Problem"),
                        quizQuestion = "How do you avoid the N+1 query issue in ORMs?",
                        quizOptions = listOf("Eager loading / JOIN queries", "Adding more indexes", "Caching everything in memory", "Disabling foreign keys"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "fs-4",
                        title = "Production Auth & Microservices",
                        description = "OAuth 2.0, JWT stateless verification, session management, and rate limiting.",
                        duration = "2 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("PKCE flow", "HttpOnly Cookies", "Redis Token Revocation"),
                        quizQuestion = "Why should JWT refresh tokens be kept in HttpOnly cookies?",
                        quizOptions = listOf("Prevents cross-site scripting (XSS) token theft", "Enables faster SSL handshake", "Reduces payload size", "Compresses headers"),
                        quizCorrectIndex = 0
                    )
                )
            ),
            RoadmapTrack(
                id = "android-dev",
                title = "Android & Mobile Systems",
                tag = "Core Focus",
                iconName = "Smartphone",
                description = "Modern Kotlin, Jetpack Compose, Coroutines & Flow, Room SQLite, Material 3, and architectural MVVM.",
                colorHex = 0xFF10B981,
                estimatedWeeks = 10,
                milestones = listOf(
                    Milestone(
                        id = "ad-1",
                        title = "Modern Kotlin Idioms & Coroutines",
                        description = "Extension functions, sealed interfaces, structured concurrency, and Flow operators.",
                        duration = "2 Weeks",
                        isCompleted = true,
                        keyTopics = listOf("Sealed Interfaces", "CoroutineScope", "StateFlow vs SharedFlow"),
                        quizQuestion = "What is the primary difference between StateFlow and SharedFlow?",
                        quizOptions = listOf("StateFlow always holds a current value and replays 1", "SharedFlow is cold", "StateFlow cannot be collected in Compose", "SharedFlow cannot emit"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ad-2",
                        title = "Jetpack Compose & Material 3",
                        description = "Recomposition optimization, remember/derivedStateOf, Custom Layouts, and M3 design tokens.",
                        duration = "3 Weeks",
                        isCompleted = true,
                        keyTopics = listOf("Modifier Chaining", "derivedStateOf", "SubcomposeLayout"),
                        quizQuestion = "When should derivedStateOf be used in Compose?",
                        quizOptions = listOf("When a state changes more frequently than the UI needs to recompose", "Only for network calls", "To store Room instances", "For loading fonts"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ad-3",
                        title = "Room Database & Offline Architecture",
                        description = "SQLite queries, Flow observation, Room TypeConverters, and migration strategies.",
                        duration = "2 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("Room DAO", "ForeignKey cascade", "Transaction annotations"),
                        quizQuestion = "What return type in a Room DAO automatically observes DB changes reactively?",
                        quizOptions = listOf("Flow<List<T>>", "List<T>", "Deferred<T>", "CompletableFuture<T>"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "ad-4",
                        title = "Performance Profiling & Release",
                        description = "Baseline profiles, R8 code shrinking, layout inspectors, and Play Store bundle generation.",
                        duration = "3 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("Baseline Profiles", "ProGuard Rules", "Memory LeakCanary"),
                        quizQuestion = "What do Android Baseline Profiles improve?",
                        quizOptions = listOf("App startup time and frame rate without full JIT warmup", "Network bandwidth", "APK size reduction", "Bluetooth connectivity"),
                        quizCorrectIndex = 0
                    )
                )
            ),
            RoadmapTrack(
                id = "dsa-interview",
                title = "Algorithms & Data Structures",
                tag = "FAANG Prep",
                iconName = "Terminal",
                description = "Master blind-75 patterns: Dynamic Programming, Graphs, Heaps, Backtracking, and Binary Search.",
                colorHex = 0xFFF59E0B,
                estimatedWeeks = 8,
                milestones = listOf(
                    Milestone(
                        id = "dsa-1",
                        title = "Two Pointers & Sliding Window",
                        description = "Linear scan optimizations, monotonic queues, and interval merging.",
                        duration = "1 Week",
                        isCompleted = true,
                        keyTopics = listOf("Fast & Slow Pointer", "Monotonic Deque", "Interval Overlaps"),
                        quizQuestion = "What is the time complexity of the two-pointer approach for 2-Sum on a sorted array?",
                        quizOptions = listOf("O(N)", "O(N log N)", "O(N^2)", "O(1)"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "dsa-2",
                        title = "Graphs: BFS, DFS & Dijkstra",
                        description = "Adjacency lists, topological sorting (Kahn's), connected components, and shortest paths.",
                        duration = "2 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("Topological Sort", "PriorityQueue Dijkstra", "Union Find"),
                        quizQuestion = "Which data structure helps detect cycles in an undirected graph in near O(1) amortized time?",
                        quizOptions = listOf("Disjoint Set Union (Union-Find)", "Stack", "Binary Heap", "Trie"),
                        quizCorrectIndex = 0
                    ),
                    Milestone(
                        id = "dsa-3",
                        title = "Dynamic Programming Patterns",
                        description = "Memoization, Tabulation, 0/1 Knapsack, Longest Common Subsequence, and Interval DP.",
                        duration = "3 Weeks",
                        isCompleted = false,
                        keyTopics = listOf("State Transitions", "Space Optimization O(1)", "Bitmask DP"),
                        quizQuestion = "What is the optimal space complexity for the Fibonacci DP problem?",
                        quizOptions = listOf("O(1) using two variables", "O(N) with an array", "O(2^N)", "O(log N)"),
                        quizCorrectIndex = 0
                    )
                )
            )
        )
    }

    private fun createInitialHackathons(): List<Hackathon> {
        return listOf(
            Hackathon(
                id = "hack-1",
                title = "Global AI Innovation Sprint 2026",
                organizer = "OpenAI & Anthropic Community",
                dates = "Nov 14 - 16, 2026",
                daysLeft = 3,
                prizePool = "$50,000",
                location = "Online / Global",
                tags = listOf("AI / LLM", "Agents", "Open Source"),
                isBookmarked = true,
                isRegistered = true,
                description = "Build cutting-edge multi-agent AI systems, developer tooling, or accessibility solutions using frontier models.",
                teamRequirement = "1 - 4 Members",
                bannerColor = 0xFF6366F1
            ),
            Hackathon(
                id = "hack-2",
                title = "MIT HackGlobal 2026",
                organizer = "MIT Tech Club",
                dates = "Dec 05 - 07, 2026",
                daysLeft = 24,
                prizePool = "$35,000",
                location = "Cambridge, MA & Virtual",
                tags = listOf("Student Only", "Hardware & IoT", "Healthcare"),
                isBookmarked = true,
                isRegistered = false,
                description = "One of the premier university hackathons in the nation bringing together 1,200+ student creators from 80+ countries.",
                teamRequirement = "2 - 4 Members",
                bannerColor = 0xFF8B5CF6
            ),
            Hackathon(
                id = "hack-3",
                title = "Google Gemini Mobile Sprint",
                organizer = "Google Cloud & Android",
                dates = "Nov 28 - 30, 2026",
                daysLeft = 17,
                prizePool = "$25,000",
                location = "Online",
                tags = listOf("Android", "Gemini AI", "Kotlin"),
                isBookmarked = false,
                isRegistered = false,
                description = "Create innovative on-device or cloud-powered Android apps leveraging the multimodal capabilities of Gemini.",
                teamRequirement = "1 - 3 Members",
                bannerColor = 0xFF10B981
            ),
            Hackathon(
                id = "hack-4",
                title = "FinTech Disrupt Hackathon",
                organizer = "Stripe & Plaid Ecosystem",
                dates = "Jan 12 - 14, 2027",
                daysLeft = 62,
                prizePool = "$40,000",
                location = "San Francisco, CA & Online",
                tags = listOf("FinTech", "Payments", "Security"),
                isBookmarked = false,
                isRegistered = false,
                description = "Reimagine student financial literacy, cross-border micropayments, and decentralized fraud prevention.",
                teamRequirement = "1 - 4 Members",
                bannerColor = 0xFFF59E0B
            )
        )
    }

    private fun createInitialInternships(): List<Internship> {
        return listOf(
            Internship(
                id = "job-1",
                company = "Google",
                role = "Software Engineering Intern (Summer 2027)",
                location = "Mountain View, CA / Hybrid",
                compensation = "$58 - $65 / hr",
                workType = "Hybrid",
                tags = listOf("Systems", "Distributed", "High Pay"),
                deadline = "Rolling (Apply Soon)",
                stage = ApplicationStage.INTERVIEWING,
                description = "Work alongside world-class engineers building distributed infrastructure, Android, Search, or Cloud services.",
                requirements = listOf("Currently enrolled in BS/MS CS", "Proficiency in C++, Java, Kotlin, or Go", "Data structures & systems understanding"),
                dateApplied = "Oct 02, 2026"
            ),
            Internship(
                id = "job-2",
                company = "Stripe",
                role = "Backend Infrastructure Intern",
                location = "Remote (US & Canada)",
                compensation = "$62 / hr + Housing Stipend",
                workType = "Remote",
                tags = listOf("Ruby", "Go", "Payments", "Top Tier"),
                deadline = "Nov 30, 2026",
                stage = ApplicationStage.APPLIED,
                description = "Design and build fault-tolerant payment APIs, ledger systems, and low-latency financial gateways.",
                requirements = listOf("Solid grasp of concurrency and databases", "Experience building web APIs", "Strong testing discipline"),
                dateApplied = "Oct 08, 2026"
            ),
            Internship(
                id = "job-3",
                company = "OpenAI",
                role = "Applied AI Engineering Intern",
                location = "San Francisco, CA",
                compensation = "$72 / hr",
                workType = "Onsite",
                tags = listOf("LLMs", "Python", "Cutting Edge"),
                deadline = "Dec 15, 2026",
                stage = ApplicationStage.OFFER,
                description = "Partner with researchers and product teams to integrate state-of-the-art foundation models into reliable developer tooling.",
                requirements = listOf("Deep learning fundamentals", "Experience with PyTorch/TensorFlow", "Fast prototyping mindset"),
                dateApplied = "Sep 15, 2026"
            ),
            Internship(
                id = "job-4",
                company = "Datadog",
                role = "Cloud & Observability Intern",
                location = "New York, NY / Remote",
                compensation = "$52 / hr",
                workType = "Remote",
                tags = listOf("Kubernetes", "DevOps", "Go"),
                deadline = "Dec 01, 2026",
                stage = ApplicationStage.SAVED,
                description = "Build high-throughput metric aggregation pipelines and container monitoring tools processing trillions of events daily.",
                requirements = listOf("Familiarity with Linux and Docker", "Solid algorithms background", "Curiosity about distributed traces"),
                dateApplied = null
            ),
            Internship(
                id = "job-5",
                company = "Figma",
                role = "Product Engineering Intern",
                location = "San Francisco, CA",
                compensation = "$56 / hr",
                workType = "Hybrid",
                tags = listOf("WebAssembly", "TypeScript", "Graphics"),
                deadline = "Nov 20, 2026",
                stage = ApplicationStage.SAVED,
                description = "Build real-time collaborative creative tools using WebAssembly, WebGL, and reactive design patterns.",
                requirements = listOf("Experience in modern TypeScript or C++", "High attention to UI detail", "Love for user experience"),
                dateApplied = null
            )
        )
    }

    private fun createInitialProjects(): List<ProjectIdea> {
        return listOf(
            ProjectIdea(
                id = "proj-1",
                title = "Local RAG Codebase Companion",
                subtitle = "On-device or local vector search over multi-repo git projects",
                difficulty = "Advanced",
                domain = "AI & Developer Tools",
                techStack = listOf("Python", "FastAPI", "ChromaDB", "Ollama", "React"),
                description = "Ingest complex code repositories into a hybrid semantic-and-AST vector index, enabling zero-leakage offline code Q&A, refactor suggestions, and automated pull request analysis.",
                keyFeatures = listOf(
                    "AST code parser chunking by functions and classes",
                    "Local embedding inference without sending code to cloud",
                    "Interactive diff viewer for suggested optimizations",
                    "Sub-100ms vector lookup via HNSW index"
                ),
                whyRecruitersLoveIt = "Proves you understand vector mathematics, AST parsing, and the practical challenges of LLM context window limits.",
                isSaved = true,
                status = ProjectStatus.BUILDING
            ),
            ProjectIdea(
                id = "proj-2",
                title = "Real-time Multi-tenant Whiteboard",
                subtitle = "CRDT-based collaborative canvas with WebSocket syncing",
                difficulty = "Intermediate",
                domain = "Systems & Web",
                techStack = listOf("TypeScript", "WebSockets", "Yjs / Automerge", "Canvas API"),
                description = "A buttery smooth collaborative canvas supporting 100+ concurrent peers drawing, inserting sticky notes, and grouping objects with zero conflicting overrides.",
                keyFeatures = listOf(
                    "Conflict-free Replicated Data Types (CRDTs)",
                    "Sub-16ms frame rendering with OffscreenCanvas",
                    "Ephemeral cursor broadcast and room discovery",
                    "Export to SVG, PNG, and JSON vector state"
                ),
                whyRecruitersLoveIt = "Recruiters look for candidates who understand distributed conflict resolution, WebSockets, and browser rendering bottlenecks.",
                isSaved = true,
                status = ProjectStatus.IDEATION
            ),
            ProjectIdea(
                id = "proj-3",
                title = "Distributed In-Memory Key-Value Store",
                subtitle = "LSM-Tree persistent storage engine with Raft clustering",
                difficulty = "Advanced",
                domain = "Systems & Databases",
                techStack = listOf("Kotlin / Go", "gRPC", "Protobuf", "LSM Tree"),
                description = "A high-performance key-value database featuring an append-only write-ahead log (WAL), immutable MemTable flushing, and tiered SSTable compaction.",
                keyFeatures = listOf(
                    "WAL crash recovery with CRC32 checksums",
                    "Bloom filter lookups for zero-disk read misses",
                    "Size-tiered background compaction thread",
                    "Benchmarked against Redis for raw throughput"
                ),
                whyRecruitersLoveIt = "Demonstrates true computer engineering depth: disk I/O, cache coherency, binary serialization, and memory management.",
                isSaved = false,
                status = ProjectStatus.IDEATION
            ),
            ProjectIdea(
                id = "proj-4",
                title = "FocusPulse - Smart Student Habit Tracker",
                subtitle = "Offline-first Android app with Pomodoro telemetry & Room sync",
                difficulty = "Beginner to Intermediate",
                domain = "Mobile & Productivity",
                techStack = listOf("Kotlin", "Jetpack Compose", "Room", "Coroutines", "WorkManager"),
                description = "A privacy-first focus tracker that correlates study sessions, subject milestones, and active streaks with beautiful Material 3 widgets.",
                keyFeatures = listOf(
                    "Custom animated countdown timer with ambient canvas visuals",
                    "Daily and weekly productivity charts",
                    "Background notifications via Android WorkManager",
                    "Exportable JSON & CSV backup"
                ),
                whyRecruitersLoveIt = "Highlights modern Android architecture (MVVM, StateFlow, Room) and meticulous design sense.",
                isSaved = false,
                status = ProjectStatus.SHIPPED
            )
        )
    }

    private fun createInitialMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "msg-0",
                sender = SenderType.AI,
                text = "Hey Alex! I'm your AI Study Buddy and CS mentor. 🚀\n\nI can help you review system design, debug Kotlin & Python code, prepare for technical interviews, or explain distributed systems.\n\nWhat are you working on today?"
            )
        )
    }

    private fun createInitialProfile(): StudentProfile {
        return StudentProfile(
            name = "Alex Rivera",
            title = "Junior CS @ Tech Institute",
            gpa = "3.92",
            level = 8,
            currentXp = 2450,
            targetXp = 3000,
            streakDays = 14,
            totalHoursStudied = 68.5,
            badges = listOf(
                AchievementBadge("b1", "14-Day Streak", "🔥", "Maintained an unbroken two-week study streak.", true),
                AchievementBadge("b2", "Hackathon Contender", "🏆", "Registered for global tech hackathons.", true),
                AchievementBadge("b3", "Algorithm Guru", "🧩", "Completed 50+ DSA patterns & quizzes.", true),
                AchievementBadge("b4", "Architect Builder", "💡", "Generated and saved 4 project specs.", true),
                AchievementBadge("b5", "Career Focused", "🎯", "Tracked active internship pipeline.", true),
                AchievementBadge("b6", "Open Source Pioneer", "⚡", "Shipped a working project prototype.", false, "80%")
            )
        )
    }
}
