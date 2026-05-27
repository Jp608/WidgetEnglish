package com.jp.widgetenglish.ai.data

import com.jp.widgetenglish.ai.network.GeminiContent
import com.jp.widgetenglish.ai.network.GeminiPart
import com.jp.widgetenglish.ai.network.GeminiRequest
import com.jp.widgetenglish.ai.network.GeminiRetrofitClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

class AiChatRepository(
    private val apiKey: String
) {

    private val api = GeminiRetrofitClient.api

    suspend fun sendMessage(userMessage: String): Result<String> {
        return try {
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
                Result.success(answer)
            }

        } catch (e: UnknownHostException) {
            Result.failure(Exception("No se pudo conectar con el asistente. Revisa tu conexión a internet."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("La respuesta tardó demasiado. Intenta nuevamente."))
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servicio IA. Código: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo obtener respuesta del asistente IA."))
        }
    }
}