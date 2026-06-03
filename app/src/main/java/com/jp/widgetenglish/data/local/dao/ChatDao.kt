package com.jp.widgetenglish.data.local.dao

import androidx.room.*
import com.jp.widgetenglish.data.local.entity.ChatMessageEntity
import com.jp.widgetenglish.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_sessions ORDER BY ultimaInteraccion DESC")
    fun observarTodasLasSesiones(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun observarMensajesPorSesion(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM (
            SELECT * FROM chat_messages
            WHERE sessionId = :sessionId
            ORDER BY timestamp DESC, id DESC
            LIMIT :limit
        )
        ORDER BY timestamp ASC, id ASC
    """)
    suspend fun obtenerMensajesRecientes(sessionId: String, limit: Int = 16): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSesion(sesion: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMensaje(mensaje: ChatMessageEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun eliminarSesion(sessionId: String)

    @Update
    suspend fun actualizarSesion(sesion: ChatSessionEntity)
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun obtenerSesionPorId(sessionId: String): ChatSessionEntity?
}
