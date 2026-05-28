package com.jp.widgetenglish.ai.ui

data class AiChatUiState(
    val messages: List<AiChatMessage> = listOf(
        AiChatMessage(
            role = AiRole.ASSISTANT,
            content = "¡Hola! Soy tu tutor de inglés. Puedes pedirme traducciones, correcciones, ejemplos o consejos de estudio."
        )
    ),
    val input: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AiChatMessage(
    val role: AiRole,
    val content: String
)

enum class AiRole {
    USER,
    ASSISTANT
}