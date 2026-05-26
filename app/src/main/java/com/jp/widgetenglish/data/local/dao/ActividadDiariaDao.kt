package com.jp.widgetenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDiariaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarActividad(
        actividad: ActividadDiariaEntity
    )

    @Query("""
        SELECT * FROM actividad_diaria
        WHERE usuarioId = :usuarioId
        AND fecha = :fecha
        LIMIT 1
    """)
    suspend fun obtenerActividadPorFecha(
        usuarioId: String,
        fecha: String
    ): ActividadDiariaEntity?

    @Query("""
        SELECT * FROM actividad_diaria
        WHERE usuarioId = :usuarioId
        AND fecha = :fecha
        LIMIT 1
    """)
    fun observarActividadPorFecha(
        usuarioId: String,
        fecha: String
    ): Flow<ActividadDiariaEntity?>

    @Query("""
        SELECT * FROM actividad_diaria
        WHERE usuarioId = :usuarioId
        ORDER BY fecha DESC
    """)
    fun observarActividadesUsuario(
        usuarioId: String
    ): Flow<List<ActividadDiariaEntity>>

    @Query("""
        SELECT * FROM actividad_diaria
        WHERE usuarioId = :usuarioId
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha ASC
    """)
    fun observarActividadesPorRango(
        usuarioId: String,
        fechaInicio: String,
        fechaFin: String
    ): Flow<List<ActividadDiariaEntity>>

    @Query("""
        SELECT * FROM actividad_diaria
        WHERE usuarioId = :usuarioId
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha ASC
    """)
    suspend fun obtenerActividadesPorRango(
        usuarioId: String,
        fechaInicio: String,
        fechaFin: String
    ): List<ActividadDiariaEntity>

    @Query("""
        SELECT * FROM actividad_diaria
        WHERE usuarioId = :usuarioId
        AND objetivoCumplido = 1
        ORDER BY fecha DESC
        LIMIT 1
    """)
    suspend fun obtenerUltimaActividadCumplida(
        usuarioId: String
    ): ActividadDiariaEntity?

    @Query("""
        DELETE FROM actividad_diaria
        WHERE usuarioId = :usuarioId
    """)
    suspend fun eliminarActividadesUsuario(
        usuarioId: String
    )

    @Query("DELETE FROM actividad_diaria")
    suspend fun eliminarTodasLasActividades()
}