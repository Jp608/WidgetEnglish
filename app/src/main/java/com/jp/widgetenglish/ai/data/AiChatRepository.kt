package com.jp.widgetenglish.ai.data

import com.jp.widgetenglish.ai.data.local.AiChatDao
import com.jp.widgetenglish.ai.data.local.AiConversationEntity
import com.jp.widgetenglish.ai.data.local.AiMessageEntity
import com.jp.widgetenglish.ai.network.GeminiContent
import com.jp.widgetenglish.ai.network.GeminiPart
import com.jp.widgetenglish.ai.network.GeminiRequest
import com.jp.widgetenglish.ai.network.GeminiRetrofitClient
import kotlinx.coroutines.flow.Flow
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import retrofit2.HttpException

data class AiSendResult(
    val conversationId: String,
    val answer: String
)

class AiChatRepository(
    private val apiKey: String,
    private val aiChatDao: AiChatDao
) {

    private val api = GeminiRetrofitClient.api

    fun getConversations(): Flow<List<AiConversationEntity>> {
        return aiChatDao.getConversations()
    }

    fun getMessagesByConversation(conversationId: String): Flow<List<AiMessageEntity>> {
        return aiChatDao.getMessagesByConversation(conversationId)
    }

    suspend fun deleteConversation(conversationId: String) {
        aiChatDao.deleteMessagesByConversation(conversationId)
        aiChatDao.deleteConversation(conversationId)
    }

    suspend fun sendMessage(
        conversationId: String?,
        userMessage: String
    ): Result<AiSendResult> {
        val now = System.currentTimeMillis()
        val finalConversationId = conversationId ?: UUID.randomUUID().toString()

        return try {
            if (conversationId == null) {
                aiChatDao.insertConversation(
                    AiConversationEntity(
                        id = finalConversationId,
                        title = buildTitle(userMessage),
                        summary = userMessage.take(90),
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            aiChatDao.insertMessage(
                AiMessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = finalConversationId,
                    role = "USER",
                    content = userMessage,
                    createdAt = now
                )
            )

            val prompt = """
                Eres un tutor de inglés dentro de una app llamada WidgetEnglish.

                Tu función es ayudar únicamente con aprendizaje de inglés:
                - Traducciones entre español e inglés.
                - Corrección de frases.
                - Explicación de vocabulario.
                - Explicación de verbos.
                - Ejemplos cortos.
                - Consejos para estudiar inglés.

                Reglas:
                - Responde en español.
                - Sé claro, amable y breve.
                - No modifiques datos, progreso, almacenamiento ni configuración de la app.
                - Si el usuario pregunta algo que no sea sobre inglés, responde:
                  "Solo puedo ayudarte con temas relacionados con el aprendizaje de inglés."

                Mensaje del usuario:
                $userMessage
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            val response = api.generateContent(
                apiKey = apiKey,
                request = request
            )

            val answer = response
                .candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            if (answer.isNullOrBlank()) {
                Result.failure(Exception("Gemini no devolvió una respuesta válida."))
            } else {
                val answerTime = System.currentTimeMillis()

                aiChatDao.insertMessage(
                    AiMessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = finalConversationId,
                        role = "ASSISTANT",
                        content = answer,
                        createdAt = answerTime
                    )
                )

                aiChatDao.updateConversationInfo(
                    conversationId = finalConversationId,
                    title = buildTitle(userMessage),
                    summary = answer.take(120),
                    updatedAt = answerTime
                )

                Result.success(
                    AiSendResult(
                        conversationId = finalConversationId,
                        answer = answer
                    )
                )
            }

        } catch (e: UnknownHostException) {
            Result.failure(Exception("No se pudo conectar con el asistente. Revisa tu conexión a internet."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("La respuesta tardó demasiado. Intenta nuevamente."))
        } catch (e: HttpException) {
            val mensaje = when (e.code()) {
                429 -> "Has alcanzado temporalmente el límite de consultas del asistente IA. Intenta nuevamente en unos minutos."
                401 -> "La clave de Gemini no es válida. Revisa la API key configurada."
                403 -> "No tienes permisos para usar el servicio de Gemini con esta clave."
                500, 503 -> "El servicio de IA no está disponible en este momento. Intenta más tarde."
                else -> "Error del servicio IA. Código: ${e.code()}"
            }

            Result.failure(Exception(mensaje))

        } catch (e: Exception) {
            Result.failure(Exception("No se pudo obtener respuesta del asistente IA."))
        }
    }

    private fun buildTitle(message: String): String {
        val clean = message.trim()
        return if (clean.length <= 38) {
            clean.ifBlank { "Nueva conversación" }
        } else {
            clean.take(38) + "..."
        }
    }
}