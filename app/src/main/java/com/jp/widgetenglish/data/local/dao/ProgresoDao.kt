package com.jp.widgetenglish.data.local.dao

import com.jp.widgetenglish.data.local.entity.TipoContenido
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgresoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProgresoUsuario(progreso: ProgresoUsuarioEntity)

    @Update
    suspend fun actualizarProgresoUsuario(progreso: ProgresoUsuarioEntity)

    @Query("""
    SELECT * FROM progreso_usuario 
    WHERE usuarioId = :usuarioId 
    AND contenidoId = :contenidoId 
    AND tipoContenido = :tipoContenido 
    LIMIT 1
""")
    suspend fun obtenerProgresoContenido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ): ProgresoUsuarioEntity?

    @Query("SELECT * FROM progreso_usuario WHERE usuarioId = :usuarioId")
    fun observarProgresoUsuario(usuarioId: String): Flow<List<ProgresoUsuarioEntity>>

    @Query("""
        SELECT * FROM progreso_usuario 
        WHERE usuarioId = :usuarioId 
        AND aprendido = 1
    """)
    fun observarContenidosAprendidos(usuarioId: String): Flow<List<ProgresoUsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProgresoLote(progreso: ProgresoLoteEntity)

    @Update
    suspend fun actualizarProgresoLote(progreso: ProgresoLoteEntity)

    @Query("SELECT * FROM progreso_lote WHERE usuarioId = :usuarioId")
    fun observarProgresoLotes(usuarioId: String): Flow<List<ProgresoLoteEntity>>

    @Query("""
        SELECT * FROM progreso_lote 
        WHERE usuarioId = :usuarioId 
        AND activo = 1 
        LIMIT 1
    """)
    fun observarLoteActivo(usuarioId: String): Flow<ProgresoLoteEntity?>

    @Query("""
        UPDATE progreso_lote 
        SET activo = 0 
        WHERE usuarioId = :usuarioId
    """)
    suspend fun desactivarLotes(usuarioId: String)

    @Query("""
        UPDATE progreso_lote 
        SET activo = 1 
        WHERE usuarioId = :usuarioId 
        AND loteId = :loteId
    """)
    suspend fun activarLote(usuarioId: String, loteId: String)
}