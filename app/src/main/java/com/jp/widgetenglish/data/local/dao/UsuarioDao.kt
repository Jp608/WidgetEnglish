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

    @Query("DELETE FROM usuarios")
    suspend fun eliminarUsuarios()
}