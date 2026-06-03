package com.jp.widgetenglish.data.remote.ai

import com.jp.widgetenglish.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException

// Data models for Groq API (OpenAI compatible)
data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage
)

class AiClientException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

interface GroqService {
    @POST("v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") auth: String,
        @Body request: GroqRequest
    ): GroqResponse
}

class GroqAiClient(private val apiKey: String) {
    private val service: GroqService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(GroqService::class.java)
    }

    private fun authHeader(): String {
        if (apiKey.isBlank()) {
            throw AiClientException("La IA no esta configurada. Falta GROQ_API_KEY.")
        }

        return "Bearer $apiKey"
    }

    private suspend fun completarChat(mensajes: List<GroqMessage>): String {
        val response = service.getCompletion(
            auth = authHeader(),
            request = GroqRequest(messages = mensajes)
        )

        return response.choices.firstOrNull()?.message?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw AiClientException("La IA no devolvio una respuesta valida.")
    }

    private fun toAiClientException(error: Exception): AiClientException {
        if (error is AiClientException) {
            return error
        }

        val message = when (error) {
            is HttpException -> when (error.code()) {
                401, 403 -> "No se pudo autenticar con el servicio de IA."
                429 -> "Leo recibio demasiadas solicitudes. Intenta de nuevo en un momento."
                in 500..599 -> "El servicio de IA no esta disponible temporalmente."
                else -> "El servicio de IA respondio con error ${error.code()}."
            }
            is IOException -> "No se pudo conectar con Leo. Revisa tu internet."
            else -> "No se pudo obtener respuesta de Leo."
        }

        return AiClientException(message, error)
    }

    suspend fun obtenerExplicacion(termino: String): String {
        val prompt = """
            Eres un tutor de inglés práctico para hispanohablantes principiantes (nivel A1 o cero). Tu misión es enseñar la palabra "$termino" de forma clara, natural y fácil de memorizar para usar en conversaciones reales.

            Responde SIEMPRE en español y en máximo 3 líneas.

            Incluye:
            1. Significado simple y fácil de entender.
            2. Qué tan común es en inglés cotidiano (muy común, común o poco común).
            3. Un tip útil de gramática o pronunciación.
            4. Un ejemplo corto en inglés y debajo su traducción al español.

            Usa un tono amigable, motivador y directo. Evita explicaciones largas, técnicas o complejas. El objetivo es que el usuario aprenda rápido y pueda recordar la palabra fácilmente.
        """.trimIndent()

        return try {
            completarChat(listOf(GroqMessage(role = "user", content = prompt)))
        } catch (e: Exception) {
            "Error al conectar con la IA Open Source: ${e.message}"
        }
    }

    suspend fun enviarChat(mensajes: List<GroqMessage>): String {
        val systemPrompt = GroqMessage(
            role = "system",
            content = """
                Eres Leo, un tutor de inglés amigable y experto para hispanohablantes principiantes.
                Tu objetivo es ayudar al usuario a practicar inglés de forma natural.
                
                REGLAS:
                1. Responde siempre en español, pero usa frases en inglés cuando sea necesario.
                2. Si el usuario comete un error en inglés, corrígelo suavemente entre paréntesis ().
                3. Mantén tus respuestas cortas y motivadoras.
                4. Si el usuario no sabe de qué hablar, propón un juego de rol sencillo (ej: en un café, saludando a un amigo).
            """.trimIndent()
        )

        val fullHistory = listOf(systemPrompt) + mensajes

        return try {
            completarChat(fullHistory)
        } catch (e: Exception) {
            throw toAiClientException(e)
        }
    }

    suspend fun generarResumen(mensajes: List<GroqMessage>): String {
        val prompt = GroqMessage(
            role = "user",
            content = "Resume esta conversación de aprendizaje de inglés en UNA SOLA frase corta (máximo 10 palabras) que sirva como título o descripción. Responde solo con el resumen."
        )
        
        return try {
            completarChat(mensajes + prompt).trim()
        } catch (e: Exception) {
            "Conversación nueva"
        }
    }

    suspend fun obtenerAsistenciaContextual(pantalla: String, contexto: String?): String {
        val prompt = """
            Eres Leo, un copiloto inteligente de aprendizaje de inglés para hispanohablantes principiantes.
            El usuario está viendo la pantalla '$pantalla'. 
            Datos actuales del contexto: ${contexto ?: "Navegando por la app"}.

            Tu misión: Ofrecer orientación, motivación o un tip corto relacionado con lo que el usuario ve.
            
            REGLAS:
            1. Máximo 20 palabras.
            2. Tono amigable y motivador.
            3. Idioma: Español, con frases cortas en inglés si es relevante.
        """.trimIndent()

        return try {
            completarChat(listOf(GroqMessage(role = "user", content = prompt)))
        } catch (e: Exception) {
            "¡Hola! Soy Leo, tu copiloto. ¿En qué puedo ayudarte hoy?"
        }
    }

    suspend fun extraerIntereses(texto: String): List<String> {
        val prompt = GroqMessage(
            role = "user",
            content = "De este texto del usuario, extrae máximo 3 temas de interés (ej: Fútbol, Música, Videojuegos) en español, separados por comas. Si no hay intereses claros, responde 'NADA'. Texto: $texto"
        )
        return try {
            val content = completarChat(listOf(prompt))
            if (content.contains("NADA", ignoreCase = true)) emptyList()
            else content.split(",").map { it.trim() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun predecirNivel(vocabularioContado: Int, errores: Int, racha: Int): String {
        val prompt = GroqMessage(
            role = "user",
            content = """
                Basado en estos datos de un estudiante de inglés:
                - Palabras aprendidas: $vocabularioContado
                - Errores cometidos: $errores
                - Racha actual: $racha días
                
                Estima su nivel según el marco común europeo (A1, A2, B1, B2) y da una frase corta de ánimo.
                Ejemplo: "Nivel A1: ¡Vas por buen camino, pronto serás un experto!". 
                Responde en máximo 15 palabras.
            """.trimIndent()
        )
        return try {
            completarChat(listOf(prompt))
        } catch (e: Exception) {
            "Nivel inicial A1"
        }
    }

    suspend fun procesarConversacionVoz(textoUsuario: String): String {
        val prompt = """
            Eres Leo, un tutor de inglés experto en conversación y fonética.
            El usuario te acaba de hablar (vía voz).
            
            REGLAS:
            1. Responde de forma muy concisa (máximo 15 palabras).
            2. Si el usuario dijo algo en inglés con errores, corrígelo brevemente.
            3. Propón una frase corta para que el usuario repita (Modo Shadowing).
            4. Tono: Muy motivador y claro.
            5. Idioma: Mezcla inglés y español naturalmente.
        """.trimIndent()

        return try {
            completarChat(
                listOf(
                    GroqMessage(role = "system", content = prompt),
                    GroqMessage(role = "user", content = textoUsuario)
                )
            )
        } catch (e: Exception) {
            "Te escucho un poco entrecortado. Can you repeat that?"
        }
    }
}
