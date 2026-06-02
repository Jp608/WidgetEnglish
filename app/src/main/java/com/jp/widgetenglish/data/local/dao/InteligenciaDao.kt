package com.jp.widgetenglish.data.local.dao

import androidx.room.*
import com.jp.widgetenglish.data.local.entity.DificultadUsuarioEntity
import com.jp.widgetenglish.data.local.entity.InteresUsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InteligenciaDao {

    // Dificultades
    @Query("SELECT * FROM dificultades_usuario WHERE usuarioId = :usuarioId ORDER BY fallos DESC")
    fun observarDificultades(usuarioId: String): Flow<List<DificultadUsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDificultad(dificultad: DificultadUsuarioEntity)

    @Query("SELECT * FROM dificultades_usuario WHERE usuarioId = :usuarioId AND tema = :tema LIMIT 1")
    suspend fun obtenerDificultadPorTema(usuarioId: String, tema: String): DificultadUsuarioEntity?

    // Intereses
    @Query("SELECT * FROM intereses_usuario WHERE usuarioId = :usuarioId ORDER BY frecuencia DESC")
    fun observarIntereses(usuarioId: String): Flow<List<InteresUsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInteres(interes: InteresUsuarioEntity)

    @Query("SELECT * FROM intereses_usuario WHERE usuarioId = :usuarioId AND interes = :interes LIMIT 1")
    suspend fun obtenerInteresPorNombre(usuarioId: String, interes: String): InteresUsuarioEntity?
}
