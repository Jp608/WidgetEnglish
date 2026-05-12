package com.jp.widgetenglish.data.repository


import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.VerboEntity
import kotlinx.coroutines.flow.Flow

interface VocabularioRepository {

    fun observarPalabras(): Flow<List<PalabraEntity>>

    fun observarVerbos(): Flow<List<VerboEntity>>

    fun observarLotes(): Flow<List<LoteEntity>>

    fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>>

    fun observarProgresoUsuario(usuarioId: String): Flow<List<ProgresoUsuarioEntity>>

    fun observarProgresoLotes(usuarioId: String): Flow<List<ProgresoLoteEntity>>

    fun observarLoteActivo(usuarioId: String): Flow<ProgresoLoteEntity?>

    suspend fun obtenerPalabraPorId(idPalabra: String): PalabraEntity?

    suspend fun obtenerVerboPorId(idVerbo: String): VerboEntity?

    suspend fun obtenerLotePorId(idLote: String): LoteEntity?

    suspend fun obtenerProgresoContenido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ): ProgresoUsuarioEntity?

    suspend fun guardarProgresoUsuario(progreso: ProgresoUsuarioEntity)

    suspend fun guardarProgresoLote(progreso: ProgresoLoteEntity)

    suspend fun activarLote(usuarioId: String, loteId: String)

    suspend fun desactivarLotes(usuarioId: String)
}