package com.jp.widgetenglish.features.ai.presentation.viewmodel

import com.jp.widgetenglish.data.local.entity.ChatMessageEntity
import com.jp.widgetenglish.data.local.entity.ChatSessionEntity

data class ChatHistoryUiState(
    val sesiones: List<ChatSessionEntity> = emptyList(),
    val cargando: Boolean = false
)

data class ChatRoomUiState(
    val sessionId: String? = null,
    val mensajes: List<ChatMessageEntity> = emptyList(),
    val cargandoRespuesta: Boolean = false,
    val error: String? = null
)
