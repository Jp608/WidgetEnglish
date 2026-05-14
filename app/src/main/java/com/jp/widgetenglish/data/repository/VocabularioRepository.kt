package com.jp.widgetenglish.data.repository

import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.VerboEntity
import kotlinx.coroutines.flow.Flow

interface VocabularioRepository {

    // -------------------------
    // PALABRAS Y VERBOS
    // -------------------------

    fun observarPalabras(): Flow<List<PalabraEntity>>

    fun observarVerbos(): Flow<List<VerboEntity>>

    suspend fun obtenerPalabraPorId(idPalabra: String): PalabraEntity?

    suspend fun obtenerVerboPorId(idVerbo: String): VerboEntity?

    // -------------------------
    // LOTES
    // -------------------------

    fun observarLotes(): Flow<List<LoteEntity>>

    fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>>

    suspend fun obtenerLotePorId(idLote: String): LoteEntity?

    suspend fun activarLote(usuarioId: String, loteId: String)

    suspend fun desactivarLotes(usuarioId: String)

    // -------------------------
    // PROGRESO DE USUARIO
    // -------------------------

    fun observarProgresoUsuario(usuarioId: String): Flow<List<ProgresoUsuarioEntity>>

    fun observarContenidosAprendidos(usuarioId: String): Flow<List<ProgresoUsuarioEntity>>

    fun observarContenidosPorEstado(
        usuarioId: String,
        estado: EstadoAprendizaje
    ): Flow<List<ProgresoUsuarioEntity>>

    suspend fun obtenerProgresoContenido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ): ProgresoUsuarioEntity?

    suspend fun guardarProgresoUsuario(progreso: ProgresoUsuarioEntity)

    suspend fun marcarContenidoComoAprendido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    )

    suspend fun marcarContenidoEnProgreso(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    )

    suspend fun marcarContenidoComoDificil(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    )

    suspend fun revertirContenidoAprendido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    )

    suspend fun actualizarFavorito(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        favorito: Boolean
    )

    // -------------------------
    // PROGRESO DE LOTES
    // -------------------------

    fun observarProgresoLotes(usuarioId: String): Flow<List<ProgresoLoteEntity>>

    fun observarLoteActivo(usuarioId: String): Flow<ProgresoLoteEntity?>

    suspend fun obtenerProgresoLote(
        usuarioId: String,
        loteId: String
    ): ProgresoLoteEntity?

    suspend fun guardarProgresoLote(progreso: ProgresoLoteEntity)

    suspend fun actualizarProgresoLotePorcentaje(
        usuarioId: String,
        loteId: String,
        progresoPorcentaje: Float
    )
}