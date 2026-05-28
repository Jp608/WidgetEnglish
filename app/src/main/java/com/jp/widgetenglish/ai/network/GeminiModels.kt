package com.jp.widgetenglish.ai.network

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.4,
    val maxOutputTokens: Int = 700
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiResponseContent? = null,
    val finishReason: String? = null
)

data class GeminiResponseContent(
    val parts: List<GeminiPart>? = null
)