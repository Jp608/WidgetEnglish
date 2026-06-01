package com.jp.widgetenglish.data.local.dao


import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import kotlinx.coroutines.flow.Flow

data class LoteConProgreso(
    @Embedded val lote: LoteEntity,
    @Embedded(prefix = "prog_") val progreso: ProgresoLoteEntity?
)

@Dao
interface LoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLote(lote: LoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLotes(lotes: List<LoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarContenidosLote(contenidos: List<LoteContenidoEntity>)

    @Update
    suspend fun actualizarLote(lote: LoteEntity)

    @Query("UPDATE lotes SET nombre = :nombre, descripcion = :descripcion, nivel = :nivel, tipoLote = :tipo, cantidadContenido = :cantidad WHERE idLote = :idLote")
    suspend fun actualizarMetadatos(idLote: String, nombre: String, descripcion: String?, nivel: com.jp.widgetenglish.data.local.entity.NivelLote, tipo: com.jp.widgetenglish.data.local.entity.TipoLote, cantidad: Int)

    @Query("SELECT * FROM lotes ORDER BY orden ASC")
    fun observarLotes(): Flow<List<LoteEntity>>

    @Query("""
        SELECT lotes.*, 
               progreso_lote.id AS prog_id, 
               progreso_lote.usuarioId AS prog_usuarioId, 
               progreso_lote.loteId AS prog_loteId, 
               progreso_lote.activo AS prog_activo, 
               progreso_lote.completado AS prog_completado, 
               progreso_lote.progresoPorcentaje AS prog_progresoPorcentaje, 
               progreso_lote.contenidosAprendidos AS prog_contenidosAprendidos, 
               progreso_lote.totalContenidos AS prog_totalContenidos, 
               progreso_lote.fechaInicio AS prog_fechaInicio, 
               progreso_lote.fechaUltimoEstudio AS prog_fechaUltimoEstudio, 
               progreso_lote.fechaCompletado AS prog_fechaCompletado
        FROM lotes
        LEFT JOIN progreso_lote ON lotes.idLote = progreso_lote.loteId AND progreso_lote.usuarioId = :usuarioId
        ORDER BY lotes.orden ASC
    """)
    fun observarLotesConProgreso(usuarioId: String): Flow<List<LoteConProgreso>>

    @Query("SELECT * FROM lotes WHERE idLote = :idLote LIMIT 1")
    suspend fun obtenerLotePorId(idLote: String): LoteEntity?

    @Query("SELECT * FROM lote_contenido WHERE loteId = :loteId ORDER BY orden ASC")
    fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>>

    @Query("DELETE FROM lote_contenido WHERE loteId = :loteId")
    suspend fun eliminarContenidoDeLote(loteId: String)

    @Query("DELETE FROM lotes")
    suspend fun eliminarLotes()

    @Query("""
        SELECT COUNT(*) FROM lote_contenido 
        WHERE loteId = :loteId 
        AND (contenidoId, tipoContenido) NOT IN (
            SELECT contenidoId, tipoContenido FROM progreso_usuario 
            WHERE usuarioId = :usuarioId AND estadoAprendizaje = 'APRENDIDA'
        )
    """)
    suspend fun obtenerConteoPendientesLote(loteId: String, usuarioId: String): Int

    @Query("""
        SELECT * FROM lote_contenido 
        WHERE loteId = :loteId 
        AND (contenidoId, tipoContenido) NOT IN (
            SELECT contenidoId, tipoContenido FROM progreso_usuario 
            WHERE usuarioId = :usuarioId AND estadoAprendizaje = 'APRENDIDA'
        )
        ORDER BY orden ASC
    """)
    suspend fun obtenerPendientesLoteDirecto(loteId: String, usuarioId: String): List<LoteContenidoEntity>
}
