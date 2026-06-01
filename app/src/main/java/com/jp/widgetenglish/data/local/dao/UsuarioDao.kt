package com.jp.widgetenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE idUsuario = :idUsuario LIMIT 1")
    suspend fun obtenerUsuarioPorId(idUsuario: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun obtenerUsuarioPorFirebaseUid(firebaseUid: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE idUsuario = :idUsuario LIMIT 1")
    fun observarUsuario(idUsuario: String): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE firebaseUid = :firebaseUid LIMIT 1")
    fun observarUsuarioPorFirebaseUid(firebaseUid: String): Flow<UsuarioEntity?>

    @Query("""
        UPDATE usuarios 
        SET quizzesRealizados = quizzesRealizados + 1,
            ultimoAcceso = :timestamp
        WHERE firebaseUid = :firebaseUid
    """)
    suspend fun incrementarQuizzesRealizados(
        firebaseUid: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE usuarios 
        SET quizzesRealizados = :cantidad,
            ultimoAcceso = :timestamp
        WHERE firebaseUid = :firebaseUid
    """)
    suspend fun actualizarQuizzesRealizados(
        firebaseUid: String,
        cantidad: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE usuarios 
        SET palabrasAprendidas = :cantidad,
            ultimoAcceso = :timestamp
        WHERE firebaseUid = :firebaseUid
    """)
    suspend fun actualizarPalabrasAprendidas(
        firebaseUid: String,
        cantidad: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE usuarios 
        SET porcentajeProgreso = :porcentaje,
            ultimoAcceso = :timestamp
        WHERE firebaseUid = :firebaseUid
    """)
    suspend fun actualizarPorcentajeProgreso(
        firebaseUid: String,
        porcentaje: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM usuarios")
    suspend fun eliminarUsuarios()

    @Query("DELETE FROM usuarios WHERE firebaseUid = :firebaseUid")
    suspend fun eliminarUsuarioPorFirebaseUid(firebaseUid: String)
}
