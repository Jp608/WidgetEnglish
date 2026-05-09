package com.jp.widgetenglish.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PalabraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPalabra(palabra: PalabraEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPalabras(palabras: List<PalabraEntity>)

    @Update
    suspend fun actualizarPalabra(palabra: PalabraEntity)

    @Query("SELECT * FROM palabras WHERE activo = 1 ORDER BY termino ASC")
    fun observarPalabras(): Flow<List<PalabraEntity>>

    @Query("SELECT * FROM palabras WHERE idPalabra = :idPalabra LIMIT 1")
    suspend fun obtenerPalabraPorId(idPalabra: String): PalabraEntity?

    @Query("""
        SELECT * FROM palabras 
        WHERE activo = 1 
        AND termino LIKE '%' || :texto || '%' 
        ORDER BY termino ASC
    """)
    fun buscarPalabras(texto: String): Flow<List<PalabraEntity>>

    @Query("DELETE FROM palabras")
    suspend fun eliminarPalabras()
}