package com.jp.widgetenglish.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.ai.data.AiChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val repository: AiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState

    private var currentConversationId: String? = null
    private var messagesJob: Job? = null

    fun initConversation(conversationId: String?) {
        if (currentConversationId == conversationId && messagesJob != null) return

        currentConversationId = conversationId
        messagesJob?.cancel()

        if (conversationId == null) {
            _uiState.value = AiChatUiState()
            return
        }

        observeConversation(conversationId)
    }

    private fun observeConversation(conversationId: String) {
        messagesJob?.cancel()

        messagesJob = viewModelScope.launch {
            repository.getMessagesByConversation(conversationId)
                .collect { savedMessages ->

                    val uiMessages = savedMessages.map { savedMessage ->
                        AiChatMessage(
                            role = if (savedMessage.role == "USER") {
                                AiRole.USER
                            } else {
                                AiRole.ASSISTANT
                            },
                            content = savedMessage.content
                        )
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            messages = if (uiMessages.isEmpty()) {
                                AiChatUiState().messages
                            } else {
                                uiMessages
                            },
                            error = null
                        )
                    }
                }
        }
    }

    fun onInputChange(value: String) {
        _uiState.update {
            it.copy(input = value)
        }
    }

    fun sendMessage() {
        val message = _uiState.value.input.trim()

        if (message.isBlank() || _uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                input = "",
                isLoading = true,
                error = null,
                messages = it.messages + AiChatMessage(
                    role = AiRole.USER,
                    content = message
                )
            )
        }

        viewModelScope.launch {
            repository.sendMessage(
                conversationId = currentConversationId,
                userMessage = message
            ).onSuccess { result ->

                val isNewConversation = currentConversationId == null

                currentConversationId = result.conversationId

                if (isNewConversation) {
                    observeConversation(result.conversationId)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }

            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "No se pudo obtener respuesta de Gemini."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }
}