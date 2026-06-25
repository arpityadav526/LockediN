package com.lockedin.feature.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.BuildConfig
import com.lockedin.data.db.entity.ChatMessageEntity
import com.lockedin.data.preferences.AppPreferences
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
    private val geminiService: GeminiService,
    private val appPreferences: AppPreferences
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
                val systemInst = GeminiSystemInstruction(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )

                // Gemini uses "user" and "model" as roles
                val apiMessages = mutableListOf<GeminiContent>()

                // Add recent conversation context (last 10 messages)
                val recentMessages = currentMessages.takeLast(10)
                recentMessages.forEach { msg ->
                    // Convert our role to Gemini's role ("user" stays "user", "assistant" becomes "model")
                    val geminiRole = if (msg.role == "assistant") "model" else "user"
                    
                    if (apiMessages.isEmpty() && geminiRole == "model") {
                        // Gemini conversations must start with a 'user' message. Skip if first is 'model'
                        return@forEach
                    }

                    if (apiMessages.isNotEmpty() && apiMessages.last().role == geminiRole) {
                        // Consecutive messages with the same role. Append text.
                        val lastMsg = apiMessages.last()
                        val newText = lastMsg.parts.first().text + "\n\n" + msg.content
                        apiMessages[apiMessages.lastIndex] = lastMsg.copy(parts = listOf(GeminiPart(text = newText)))
                    } else {
                        apiMessages.add(
                            GeminiContent(
                                role = geminiRole,
                                parts = listOf(GeminiPart(text = msg.content))
                            )
                        )
                    }
                }

                // Add the new user message
                if (apiMessages.isNotEmpty() && apiMessages.last().role == "user") {
                    val lastMsg = apiMessages.last()
                    val newText = lastMsg.parts.first().text + "\n\n" + content.trim()
                    apiMessages[apiMessages.lastIndex] = lastMsg.copy(parts = listOf(GeminiPart(text = newText)))
                } else {
                    apiMessages.add(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = content.trim()))
                        )
                    )
                }

                val savedKey = appPreferences.aiApiKey.firstOrNull()
                val apiKey = if (!savedKey.isNullOrBlank()) savedKey else BuildConfig.OPENAI_API_KEY
                
                if (apiKey.isBlank() || apiKey == "your_openai_key_here") {
                    throw IllegalArgumentException("API Key is missing. Please set it in Settings.")
                }

                val response = geminiService.generateContent(
                    apiKey = apiKey,
                    request = GeminiRequest(
                        contents = apiMessages,
                        systemInstruction = systemInst
                    )
                )

                val assistantContent = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
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
                    e.message?.contains("API Key is missing") == true -> e.message!!
                    e.message?.contains("Unable to resolve host") == true -> "No internet connection"
                    e.message?.contains("401") == true -> "Invalid API key. Check Settings."
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
