package com.jp.widgetenglish.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.VerboEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerboDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVerbo(verbo: VerboEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVerbos(verbos: List<VerboEntity>)

    @Update
    suspend fun actualizarVerbo(verbo: VerboEntity)

    @Query("SELECT * FROM verbos WHERE activo = 1 ORDER BY formaBase ASC")
    fun observarVerbos(): Flow<List<VerboEntity>>

    @Query("SELECT * FROM verbos WHERE idVerbo = :idVerbo LIMIT 1")
    suspend fun obtenerVerboPorId(idVerbo: String): VerboEntity?

    @Query("""
        SELECT * FROM verbos 
        WHERE activo = 1 
        AND formaBase LIKE '%' || :texto || '%' 
        ORDER BY formaBase ASC
    """)
    fun buscarVerbos(texto: String): Flow<List<VerboEntity>>

    @Query("DELETE FROM verbos")
    suspend fun eliminarVerbos()
}