package com.jp.widgetenglish.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM lotes ORDER BY orden ASC")
    fun observarLotes(): Flow<List<LoteEntity>>

    @Query("SELECT * FROM lotes WHERE idLote = :idLote LIMIT 1")
    suspend fun obtenerLotePorId(idLote: String): LoteEntity?

    @Query("SELECT * FROM lote_contenido WHERE loteId = :loteId ORDER BY orden ASC")
    fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>>

    @Query("DELETE FROM lote_contenido WHERE loteId = :loteId")
    suspend fun eliminarContenidoDeLote(loteId: String)

    @Query("DELETE FROM lotes")
    suspend fun eliminarLotes()
}
