package com.jp.widgetenglish.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.ai.data.AiChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val repository: AiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState

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
            repository.sendMessage(message)
                .onSuccess { answer ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = it.messages + AiChatMessage(
                                role = AiRole.ASSISTANT,
                                content = answer
                            )
                        )
                    }
                }
                .onFailure { error ->
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