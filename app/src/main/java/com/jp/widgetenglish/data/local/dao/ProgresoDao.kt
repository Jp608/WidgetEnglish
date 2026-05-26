package com.jp.widgetenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgresoDao {

    // -------------------------
    // PROGRESO POR CONTENIDO
    // -------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProgresoUsuario(progreso: ProgresoUsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProgreso(progreso: ProgresoUsuarioEntity)

    @Update
    suspend fun actualizarProgresoUsuario(progreso: ProgresoUsuarioEntity)

    @Query(
        """
        SELECT * FROM progreso_usuario 
        WHERE usuarioId = :usuarioId 
        AND contenidoId = :contenidoId 
        AND tipoContenido = :tipoContenido 
        LIMIT 1
        """
    )
    suspend fun obtenerProgresoContenido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ): ProgresoUsuarioEntity?

    @Query("SELECT * FROM progreso_usuario WHERE usuarioId = :usuarioId AND contenidoId = :contenidoId AND tipoContenido = :tipo LIMIT 1")
    suspend fun obtenerProgreso(usuarioId: String, contenidoId: String, tipo: TipoContenido): ProgresoUsuarioEntity?

    @Query("""
    SELECT * FROM progreso_lote
    WHERE usuarioId = :usuarioId
""")
    fun observarProgresosLotesUsuario(
        usuarioId: String
    ): Flow<List<ProgresoLoteEntity>>

    @Query(
        """
        SELECT * FROM progreso_usuario 
        WHERE usuarioId = :usuarioId
        """
    )
    fun observarProgresoUsuario(
        usuarioId: String
    ): Flow<List<ProgresoUsuarioEntity>>

    @Query(
        """
        SELECT * FROM progreso_usuario 
        WHERE usuarioId = :usuarioId 
        AND aprendido = 1
        """
    )
    fun observarContenidosAprendidos(
        usuarioId: String
    ): Flow<List<ProgresoUsuarioEntity>>

    @Query(
        """
        SELECT * FROM progreso_usuario 
        WHERE usuarioId = :usuarioId 
        AND estadoAprendizaje = :estado
        """
    )
    fun observarContenidosPorEstado(
        usuarioId: String,
        estado: EstadoAprendizaje
    ): Flow<List<ProgresoUsuarioEntity>>

    @Query(
        """
        UPDATE progreso_usuario
        SET 
            estadoAprendizaje = :estado,
            aprendido = :aprendido,
            nivelDominio = :nivelDominio,
            vecesRepasado = vecesRepasado + 1,
            ultimaRevision = :fechaRevision
        WHERE usuarioId = :usuarioId
        AND contenidoId = :contenidoId
        AND tipoContenido = :tipoContenido
        """
    )
    suspend fun actualizarEstadoContenido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        estado: EstadoAprendizaje,
        aprendido: Boolean,
        nivelDominio: Float,
        fechaRevision: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE progreso_usuario
        SET 
            estadoAprendizaje = 'APRENDIDA',
            aprendido = 1,
            nivelDominio = 1.0,
            vecesRepasado = vecesRepasado + 1,
            ultimaRevision = :fechaRevision
        WHERE usuarioId = :usuarioId
        AND contenidoId = :contenidoId
        AND tipoContenido = :tipoContenido
        """
    )
    suspend fun marcarContenidoComoAprendido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        fechaRevision: Long = System.currentTimeMillis()
    )

    @Query("UPDATE progreso_usuario SET estadoAprendizaje = 'APRENDIDA', aprendido = 1, nivelDominio = 1.0 WHERE usuarioId = :usuarioId AND contenidoId = :contenidoId AND tipoContenido = :tipo")
    suspend fun marcarComoAprendido(usuarioId: String, contenidoId: String, tipo: TipoContenido)

    @Query(
        """
        UPDATE progreso_usuario
        SET 
            estadoAprendizaje = 'EN_PROGRESO',
            aprendido = 0,
            nivelDominio = 0.5,
            vecesRepasado = vecesRepasado + 1,
            ultimaRevision = :fechaRevision
        WHERE usuarioId = :usuarioId
        AND contenidoId = :contenidoId
        AND tipoContenido = :tipoContenido
        """
    )
    suspend fun marcarContenidoEnProgreso(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        fechaRevision: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE progreso_usuario
        SET 
            estadoAprendizaje = 'DIFICIL',
            aprendido = 0,
            nivelDominio = 0.25,
            vecesRepasado = vecesRepasado + 1,
            respuestasIncorrectas = respuestasIncorrectas + 1,
            ultimaRevision = :fechaRevision
        WHERE usuarioId = :usuarioId
        AND contenidoId = :contenidoId
        AND tipoContenido = :tipoContenido
        """
    )
    suspend fun marcarContenidoComoDificil(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        fechaRevision: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE progreso_usuario
        SET 
            estadoAprendizaje = 'EN_PROGRESO',
            aprendido = 0,
            nivelDominio = 0.5,
            ultimaRevision = :fechaRevision
        WHERE usuarioId = :usuarioId
        AND contenidoId = :contenidoId
        AND tipoContenido = :tipoContenido
        """
    )
    suspend fun revertirContenidoAprendido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        fechaRevision: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE progreso_usuario
        SET favorito = :favorito
        WHERE usuarioId = :usuarioId
        AND contenidoId = :contenidoId
        AND tipoContenido = :tipoContenido
        """
    )
    suspend fun actualizarFavorito(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        favorito: Boolean
    )

    // -------------------------
    // PROGRESO POR LOTE
    // -------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProgresoLote(progreso: ProgresoLoteEntity)

    @Query("""
    UPDATE progreso_lote
    SET 
        activo = :activo,
        progresoPorcentaje = :progresoPorcentaje,
        contenidosAprendidos = :aprendidas,
        totalContenidos = :total,
        fechaUltimoEstudio = :fecha
    WHERE usuarioId = :usuarioId 
    AND loteId = :loteId
    """)
    suspend fun actualizarProgresoLoteFull(
        usuarioId: String, 
        loteId: String, 
        activo: Boolean,
        progresoPorcentaje: Float,
        aprendidas: Int,
        total: Int,
        fecha: Long = System.currentTimeMillis()
    )

    @Update
    suspend fun actualizarProgresoLote(progreso: ProgresoLoteEntity)

    @Query(
        """
        SELECT * FROM progreso_lote 
        WHERE usuarioId = :usuarioId
        """
    )
    fun observarProgresoLotes(
        usuarioId: String
    ): Flow<List<ProgresoLoteEntity>>

    @Query(
        """
        SELECT * FROM progreso_lote 
        WHERE usuarioId = :usuarioId 
        AND activo = 1 
        LIMIT 1
        """
    )
    fun observarLoteActivo(
        usuarioId: String
    ): Flow<ProgresoLoteEntity?>

    @Query(
        """
        SELECT * FROM progreso_lote 
        WHERE usuarioId = :usuarioId
        AND loteId = :loteId
        LIMIT 1
        """
    )
    suspend fun obtenerProgresoLote(
        usuarioId: String,
        loteId: String
    ): ProgresoLoteEntity?

    @Query(
        """
        UPDATE progreso_lote 
        SET activo = 0 
        WHERE usuarioId = :usuarioId
        """
    )
    suspend fun desactivarLotes(
        usuarioId: String
    )

    @Query(
        """
        UPDATE progreso_lote 
        SET activo = 1 
        WHERE usuarioId = :usuarioId 
        AND loteId = :loteId
        """
    )
    suspend fun activarLote(
        usuarioId: String,
        loteId: String
    )

    @Query(
        """
    UPDATE progreso_lote
    SET 
        progresoPorcentaje = :progresoPorcentaje,
        fechaUltimoEstudio = :fechaUltimoEstudio
    WHERE usuarioId = :usuarioId
    AND loteId = :loteId
    """
    )
    suspend fun actualizarProgresoLotePorcentaje(
        usuarioId: String,
        loteId: String,
        progresoPorcentaje: Float,
        fechaUltimoEstudio: Long = System.currentTimeMillis()
    )

    @Query(
        """
    UPDATE progreso_lote
    SET 
        progresoPorcentaje = 0,
        contenidosAprendidos = 0,
        completado = 0,
        fechaUltimoEstudio = :fecha
    WHERE usuarioId = :usuarioId
    AND loteId = :loteId
    """
    )
    suspend fun reiniciarProgresoLote(
        usuarioId: String,
        loteId: String,
        fecha: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE progreso_usuario
        SET 
            estadoAprendizaje = 'NO_VISTA',
            aprendido = 0,
            nivelDominio = 0.0,
            respuestasCorrectas = 0,
            respuestasIncorrectas = 0,
            vecesRepasado = 0,
            ultimaRevision = NULL
        WHERE usuarioId = :usuarioId
        AND (contenidoId, tipoContenido) IN (
            SELECT contenidoId, tipoContenido FROM lote_contenido WHERE loteId = :loteId
        )
        """
    )
    suspend fun reiniciarProgresoContenidosLote(usuarioId: String, loteId: String)
}