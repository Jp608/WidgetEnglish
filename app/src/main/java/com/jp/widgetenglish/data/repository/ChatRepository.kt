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
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = contenido.trim()
        )
        chatDao.insertarMensaje(userMsg)

        val history = chatDao.obtenerMensajesRecientes(sessionId)
            .filter { it.role == "user" || it.role == "assistant" }
            .map { message ->
                GroqMessage(
                    role = message.role,
                    content = message.content
                )
            }

        val aiResponse = aiClient.enviarChat(history)

        val aiMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "assistant",
            content = aiResponse
        )
        chatDao.insertarMensaje(aiMsg)

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
