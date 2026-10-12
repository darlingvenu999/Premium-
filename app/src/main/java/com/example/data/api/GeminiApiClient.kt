package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.SenderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val DEFAULT_MODEL = "gemini-3.5-flash"
        const val PRO_MODEL = "gemini-3.1-pro-preview"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val SYSTEM_PROMPT = """
You are StudentHub's elite AI Study Tutor and Computer Science mentor. 
Your mission is to help tech students master software engineering, data structures & algorithms, system design, modern mobile/web frameworks, and AI.

When explaining technical concepts:
1. Provide an intuitive mental model or clear real-world analogy.
2. Structure your response with clear headers and bullet points.
3. Include clean, idiomatic code snippets (in Kotlin, Python, TypeScript, Go, or SQL) whenever helpful.
4. Highlight time and space complexity (Big-O analysis) where applicable.
5. Point out common student pitfalls, edge cases, and technical interview expectations.
Keep answers engaging, technically precise, and formatted in clean Markdown.
"""
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateTutorResponse(
        history: List<ChatMessage>,
        userMessage: String,
        selectedModel: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY_NOT_CONFIGURED")
            )
        }

        try {
            val endpointUrl = "$BASE_URL/$selectedModel:generateContent?key=$apiKey"

            // Construct JSON request body
            val requestJson = JSONObject()

            // System Instruction
            val systemObj = JSONObject()
            val systemParts = JSONArray()
            systemParts.put(JSONObject().put("text", SYSTEM_PROMPT))
            systemObj.put("parts", systemParts)
            requestJson.put("systemInstruction", systemObj)

            // Conversation history (limit to last 10 messages for context efficiency)
            val contentsArray = JSONArray()
            val recentHistory = history.takeLast(10)

            for (msg in recentHistory) {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.sender == SenderType.USER) "user" else "model")
                val partsArray = JSONArray()
                val textPayload = if (msg.codeSnippet != null) {
                    "${msg.text}\n\n```${msg.language ?: ""}\n${msg.codeSnippet}\n```"
                } else {
                    msg.text
                }
                partsArray.put(JSONObject().put("text", textPayload))
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            // Append current user message
            val currentUserObj = JSONObject()
            currentUserObj.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentUserObj.put("parts", currentParts)
            contentsArray.put(currentUserObj)

            requestJson.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            generationConfig.put("maxOutputTokens", 2500)
            requestJson.put("generationConfig", generationConfig)

            val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(endpointUrl)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBodyString")
                val errorMsg = try {
                    val errorJson = JSONObject(responseBodyString)
                    errorJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception("Gemini API error: $errorMsg"))
            }

            // Parse response candidates
            val rootJson = JSONObject(responseBodyString)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val textBuilder = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val partObj = parts.getJSONObject(i)
                        val partText = partObj.optString("text", "")
                        textBuilder.append(partText)
                    }
                    val reply = textBuilder.toString()
                    return@withContext Result.success(reply)
                }
            }

            Result.failure(Exception("No content returned from Gemini model."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API request", e)
            Result.failure(e)
        }
    }
}
