package com.jp.widgetenglish.data.repository

import com.jp.widgetenglish.data.local.dao.ChatDao
import com.jp.widgetenglish.data.local.entity.ChatMessageEntity
import com.jp.widgetenglish.data.local.entity.ChatSessionEntity
import com.jp.widgetenglish.data.remote.ai.GroqAiClient
import com.jp.widgetenglish.data.remote.ai.GroqMessage
import kotlinx.coroutines.flow.Flow
import java.util.*

interface ChatRepository {
    fun observarSesiones(): Flow<List<ChatSessionEntity>>
    fun observarMensajes(sessionId: String): Flow<List<ChatMessageEntity>>
    suspend fun crearNuevaSesion(tituloInicial: String): String
    suspend fun enviarMensaje(sessionId: String, contenido: String): String
    suspend fun eliminarSesion(sessionId: String)
}

class ChatRepositoryImpl(
    private val chatDao: ChatDao,
    private val aiClient: GroqAiClient
) : ChatRepository {

    override fun observarSesiones(): Flow<List<ChatSessionEntity>> = chatDao.observarTodasLasSesiones()

    override fun observarMensajes(sessionId: String): Flow<List<ChatMessageEntity>> = chatDao.observarMensajesPorSesion(sessionId)

    override suspend fun crearNuevaSesion(tituloInicial: String): String {
        val id = UUID.randomUUID().toString()
        val sesion = ChatSessionEntity(
            id = id,
            titulo = tituloInicial
        )
        chatDao.insertarSesion(sesion)
        return id
    }

    override suspend fun enviarMensaje(sessionId: String, contenido: String): String {
        // 1. Guardar mensaje del usuario
        val userMsg = ChatMessageEntity(sessionId = sessionId, role = "user", content = contenido)
        chatDao.insertarMensaje(userMsg)

        // 2. Obtener historial para la IA
        // Nota: En un repo real usaríamos un flow convertido a lista o una query directa
        // Para simplificar esta versión, obtenemos los mensajes actuales
        val history = mutableListOf<GroqMessage>()
        // Aquí deberíamos obtener los mensajes previos de la DB, por ahora simulamos con el nuevo
        history.add(GroqMessage(role = "user", content = contenido))

        // 3. Consultar a la IA
        val aiResponse = aiClient.enviarChat(history)

        // 4. Guardar respuesta de la IA
        val aiMsg = ChatMessageEntity(sessionId = sessionId, role = "assistant", content = aiResponse)
        chatDao.insertarMensaje(aiMsg)

        // 5. Actualizar última interacción y generar resumen si es necesario
        val sesion = chatDao.obtenerSesionPorId(sessionId)
        sesion?.let {
            val updatedSesion = it.copy(
                ultimaInteraccion = System.currentTimeMillis(),
                resumen = if (it.resumen == null) aiClient.generarResumen(history + GroqMessage("assistant", aiResponse)) else it.resumen
            )
            chatDao.actualizarSesion(updatedSesion)
        }

        return aiResponse
    }

    override suspend fun eliminarSesion(sessionId: String) {
        chatDao.eliminarSesion(sessionId)
    }
}
