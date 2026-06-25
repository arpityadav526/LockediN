package com.lockedin.feature.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.BuildConfig
import com.lockedin.data.db.entity.ChatMessageEntity
import com.lockedin.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val openAiService: OpenAiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private val systemPrompt = """
        You are a focused study assistant. Help the student understand concepts, 
        solve homework problems, explain topics clearly. Do not engage in casual 
        conversation unrelated to studying. Keep answers concise and educational.
    """.trimIndent()

    init {
        viewModelScope.launch {
            chatRepository.getRecentMessages(50).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            // Save user message
            val userMessage = ChatMessageEntity(
                role = "user",
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
            chatRepository.insertMessage(userMessage)

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Build message history for API
                val currentMessages = _uiState.value.messages
                val apiMessages = mutableListOf(
                    OpenAiMessage(role = "system", content = systemPrompt)
                )

                // Add recent conversation context (last 10 messages)
                val recentMessages = currentMessages.takeLast(10)
                recentMessages.forEach { msg ->
                    apiMessages.add(OpenAiMessage(role = msg.role, content = msg.content))
                }

                // Add the new user message
                apiMessages.add(OpenAiMessage(role = "user", content = content.trim()))

                val apiKey = BuildConfig.OPENAI_API_KEY
                val response = openAiService.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = ChatCompletionRequest(
                        messages = apiMessages
                    )
                )

                val assistantContent = response.choices?.firstOrNull()?.message?.content
                    ?: "I couldn't generate a response. Please try again."

                // Save assistant message
                val assistantMessage = ChatMessageEntity(
                    role = "assistant",
                    content = assistantContent,
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.insertMessage(assistantMessage)

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Unable to resolve host") == true -> "No internet connection"
                    e.message?.contains("401") == true -> "Invalid API key"
                    else -> "Something went wrong. Please try again."
                }
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
        }
    }
}
