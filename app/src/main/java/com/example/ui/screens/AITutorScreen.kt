package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.api.GeminiApiClient
import com.example.data.model.ChatMessage
import com.example.data.model.SenderType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AITutorScreen(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    isApiKeyConfigured: Boolean,
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val quickPrompts = listOf(
        "⚡ Explain Raft Consensus vs Paxos",
        "🧩 Two Pointers vs Sliding Window",
        "🎤 Mock Interview: LRU Cache in Kotlin",
        "📐 System Design: TinyURL at Scale",
        "🔍 How does PyTorch Autograd work?",
        "💡 Explain Transformers Self-Attention"
    )

    val studyModes = listOf(
        "🧠 Deep Explanation" to "Explain in-depth with intuition and diagrams: ",
        "🔍 Code Review" to "Review this code for bugs, edge cases, and performance: ",
        "🎤 Mock Interview" to "Act as a Senior FAANG Interviewer and test me on: ",
        "📐 System Architecture" to "Provide a complete system design blueprint for: "
    )

    // Auto-scroll on new messages or thinking state
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // AI Mentor Header Card with Model Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Slate900,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, Slate850)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mascot & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, CyanNeon, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ai_tutor_mascot),
                                contentDescription = "AI Tutor Mascot",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Gemini Study Tutor",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isApiKeyConfigured) EmeraldSuccess else AmberStreak)
                                )
                            }
                            Text(
                                text = if (isApiKeyConfigured) "Gemini API Connected" else "CS Mentor Assistant",
                                color = if (isApiKeyConfigured) CyanNeon else Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Model Badge and Clear Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Model Switcher Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Slate800,
                            border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showModelMenu = true }
                                .testTag("model_selector_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = VioletGlow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (selectedModel.contains("pro")) "Pro 3.1" else "Flash 3.5",
                                    color = VioletGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Gemini 3.5 Flash (Recommended)", fontWeight = FontWeight.Bold)
                                        Text("Ultra-fast responses & code generation", fontSize = 11.sp, color = Slate400)
                                    }
                                },
                                onClick = {
                                    onSelectModel(GeminiApiClient.DEFAULT_MODEL)
                                    showModelMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Gemini 3.1 Pro Preview", fontWeight = FontWeight.Bold)
                                        Text("Deep reasoning & advanced STEM proof", fontSize = 11.sp, color = Slate400)
                                    }
                                },
                                onClick = {
                                    onSelectModel(GeminiApiClient.PRO_MODEL)
                                    showModelMenu = false
                                }
                            )
                        }

                        // Clear Chat Button
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Clear Chat",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Study Mode Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(studyModes) { (label, prefix) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { inputText = prefix }
                ) {
                    Text(
                        text = label,
                        color = Slate200,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Quick Suggestion Prompt Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSendMessage(prompt) }
                        .testTag("quick_prompt_${prompt.take(12)}")
                ) {
                    Text(
                        text = prompt,
                        color = IndigoLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 14.dp)
        ) {
            items(messages) { msg ->
                TutorMessageBubble(
                    message = msg,
                    onCopyCode = { code ->
                        clipboardManager.setText(AnnotatedString(code))
                        Toast.makeText(context, "Code copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    },
                    onQuizRequest = {
                        onSendMessage("Generate a 1-question multiple-choice technical quiz based on your explanation above with 4 options and the answer explained at the end.")
                    },
                    onExampleRequest = {
                        onSendMessage("Can you provide an industry production example with full code for this concept?")
                    }
                )
            }

            // Animated Thinking Indicator
            if (isThinking) {
                item {
                    ThinkingIndicatorBubble(modelName = selectedModel)
                }
            }
        }

        // Bottom Input Row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 78.dp),
            color = Slate950,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, Slate900)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask anything: algorithms, coroutines, system design...",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_tutor_input")
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isThinking) {
                            val textToSend = inputText.trim()
                            inputText = ""
                            onSendMessage(textToSend)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isThinking)
                                Brush.linearGradient(listOf(IndigoPrimary, CyanNeon))
                            else
                                Brush.linearGradient(listOf(Slate800, Slate800))
                        )
                        .testTag("ai_tutor_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isThinking) Color.White else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Confirmation dialog for clearing chat
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Reset Chat Session?", color = Slate50, fontWeight = FontWeight.Bold) },
            text = { Text("This will clear the current conversation history with Gemini.", color = Slate400) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
private fun TutorMessageBubble(
    message: ChatMessage,
    onCopyCode: (String) -> Unit,
    onQuizRequest: () -> Unit,
    onExampleRequest: () -> Unit
) {
    val isUser = message.sender == SenderType.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(IndigoPrimary, VioletAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Gemini",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) IndigoPrimary else Slate900,
                border = if (!isUser) BorderStroke(1.dp, Slate800) else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else Slate50,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    // Code snippet container with Copy button
                    if (message.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(CyanNeon)
                                        )
                                        Text(
                                            text = message.language?.uppercase() ?: "CODE",
                                            color = CyanNeon,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Slate800,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onCopyCode(message.codeSnippet) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = Slate200,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Copy",
                                                color = Slate200,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = message.codeSnippet,
                                    fontFamily = JetBrainsMono,
                                    color = EmeraldSuccess,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Follow-Up action buttons on AI responses
            if (!isUser) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onQuizRequest)
                    ) {
                        Text(
                            text = "🎯 Quiz Me",
                            fontSize = 10.sp,
                            color = AmberGlow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onExampleRequest)
                    ) {
                        Text(
                            text = "💡 Production Example",
                            fontSize = 10.sp,
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicatorBubble(modelName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(IndigoPrimary, VioletAccent))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Thinking",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Gemini is analyzing concepts...",
                    color = VioletGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                // 3 pulsating dots
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = dotAlpha))
                    )
                }
            }
        }
    }
}
