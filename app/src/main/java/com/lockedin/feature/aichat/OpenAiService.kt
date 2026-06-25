package com.lockedin.feature.aichat

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiService {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

// Request models
data class ChatCompletionRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAiMessage>,
    val max_tokens: Int = 1000
)

data class OpenAiMessage(
    val role: String,
    val content: String
)

// Response models
data class ChatCompletionResponse(
    val id: String?,
    val choices: List<ChatChoice>?
)

data class ChatChoice(
    val index: Int?,
    val message: OpenAiMessage?,
    val finish_reason: String?
)
