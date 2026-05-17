package com.jp.widgetenglish.data.repository

import com.jp.widgetenglish.data.local.dao.LoteDao
import com.jp.widgetenglish.data.local.dao.PalabraDao
import com.jp.widgetenglish.data.local.dao.ProgresoDao
import com.jp.widgetenglish.data.local.dao.VerboDao
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.VerboEntity
import kotlinx.coroutines.flow.Flow

class VocabularioRepositoryImpl(
    private val palabraDao: PalabraDao,
    private val verboDao: VerboDao,
    private val loteDao: LoteDao,
    private val progresoDao: ProgresoDao
) : VocabularioRepository {

    // -------------------------
    // PALABRAS Y VERBOS
    // -------------------------

    override fun observarPalabras(): Flow<List<PalabraEntity>> {
        return palabraDao.observarPalabras()
    }

    override fun observarVerbos(): Flow<List<VerboEntity>> {
        return verboDao.observarVerbos()
    }

    override suspend fun obtenerPalabraPorId(idPalabra: String): PalabraEntity? {
        return palabraDao.obtenerPalabraPorId(idPalabra)
    }

    override suspend fun obtenerVerboPorId(idVerbo: String): VerboEntity? {
        return verboDao.obtenerVerboPorId(idVerbo)
    }

    // -------------------------
    // LOTES
    // -------------------------

    override fun observarLotes(): Flow<List<LoteEntity>> {
        return loteDao.observarLotes()
    }

    override fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>> {
        return loteDao.observarContenidoDeLote(loteId)
    }

    override suspend fun obtenerLotePorId(idLote: String): LoteEntity? {
        return loteDao.obtenerLotePorId(idLote)
    }

    override suspend fun activarLote(usuarioId: String, loteId: String) {
        progresoDao.desactivarLotes(usuarioId)
        progresoDao.activarLote(usuarioId, loteId)
    }

    override suspend fun desactivarLotes(usuarioId: String) {
        progresoDao.desactivarLotes(usuarioId)
    }

    // -------------------------
    // PROGRESO DE USUARIO
    // -------------------------

    override fun observarProgresoUsuario(usuarioId: String): Flow<List<ProgresoUsuarioEntity>> {
        return progresoDao.observarProgresoUsuario(usuarioId)
    }

    override fun observarContenidosAprendidos(usuarioId: String): Flow<List<ProgresoUsuarioEntity>> {
        return progresoDao.observarContenidosAprendidos(usuarioId)
    }

    override fun observarContenidosPorEstado(
        usuarioId: String,
        estado: EstadoAprendizaje
    ): Flow<List<ProgresoUsuarioEntity>> {
        return progresoDao.observarContenidosPorEstado(
            usuarioId = usuarioId,
            estado = estado
        )
    }

    override suspend fun obtenerProgresoContenido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ): ProgresoUsuarioEntity? {
        return progresoDao.obtenerProgresoContenido(
            usuarioId = usuarioId,
            contenidoId = contenidoId,
            tipoContenido = tipoContenido
        )
    }

    override suspend fun guardarProgresoUsuario(progreso: ProgresoUsuarioEntity) {
        progresoDao.insertarProgresoUsuario(progreso)
    }

    override suspend fun marcarContenidoComoAprendido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ) {
        android.util.Log.d("VocabularioRepo", "Marking learned: $contenidoId for user $usuarioId")
        val progresoExistente = progresoDao.obtenerProgresoContenido(usuarioId, contenidoId, tipoContenido)
        if (progresoExistente == null) {
            android.util.Log.d("VocabularioRepo", "Creating new progress entry")
            progresoDao.insertarProgresoUsuario(
                ProgresoUsuarioEntity(
                    id = "pu_${usuarioId}_${contenidoId}",
                    usuarioId = usuarioId,
                    contenidoId = contenidoId,
                    tipoContenido = tipoContenido,
                    estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                    aprendido = true
                )
            )
        } else {
            android.util.Log.d("VocabularioRepo", "Updating existing progress entry")
            progresoDao.marcarContenidoComoAprendido(
                usuarioId = usuarioId,
                contenidoId = contenidoId,
                tipoContenido = tipoContenido
            )
        }
    }

    override suspend fun marcarContenidoEnProgreso(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ) {
        progresoDao.marcarContenidoEnProgreso(
            usuarioId = usuarioId,
            contenidoId = contenidoId,
            tipoContenido = tipoContenido
        )
    }

    override suspend fun marcarContenidoComoDificil(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ) {
        progresoDao.marcarContenidoComoDificil(
            usuarioId = usuarioId,
            contenidoId = contenidoId,
            tipoContenido = tipoContenido
        )
    }

    override suspend fun revertirContenidoAprendido(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido
    ) {
        val progresoExistente = progresoDao.obtenerProgresoContenido(usuarioId, contenidoId, tipoContenido)
        if (progresoExistente == null) {
            progresoDao.insertarProgresoUsuario(
                ProgresoUsuarioEntity(
                    id = "pu_${usuarioId}_${contenidoId}",
                    usuarioId = usuarioId,
                    contenidoId = contenidoId,
                    tipoContenido = tipoContenido,
                    estadoAprendizaje = EstadoAprendizaje.EN_PROGRESO,
                    aprendido = false
                )
            )
        } else {
            progresoDao.revertirContenidoAprendido(
                usuarioId = usuarioId,
                contenidoId = contenidoId,
                tipoContenido = tipoContenido
            )
        }
    }

    override suspend fun actualizarFavorito(
        usuarioId: String,
        contenidoId: String,
        tipoContenido: TipoContenido,
        favorito: Boolean
    ) {
        progresoDao.actualizarFavorito(
            usuarioId = usuarioId,
            contenidoId = contenidoId,
            tipoContenido = tipoContenido,
            favorito = favorito
        )
    }

    // -------------------------
    // PROGRESO DE LOTES
    // -------------------------

    override fun observarProgresoLotes(usuarioId: String): Flow<List<ProgresoLoteEntity>> {
        return progresoDao.observarProgresoLotes(usuarioId)
    }

    override fun observarLoteActivo(usuarioId: String): Flow<ProgresoLoteEntity?> {
        return progresoDao.observarLoteActivo(usuarioId)
    }

    override suspend fun obtenerProgresoLote(
        usuarioId: String,
        loteId: String
    ): ProgresoLoteEntity? {
        return progresoDao.obtenerProgresoLote(
            usuarioId = usuarioId,
            loteId = loteId
        )
    }

    override suspend fun guardarProgresoLote(progreso: ProgresoLoteEntity) {
        progresoDao.insertarProgresoLote(progreso)
    }

    override suspend fun actualizarProgresoLotePorcentaje(
        usuarioId: String,
        loteId: String,
        progresoPorcentaje: Float
    ) {
        progresoDao.actualizarProgresoLotePorcentaje(
            usuarioId = usuarioId,
            loteId = loteId,
            progresoPorcentaje = progresoPorcentaje
        )
    }
}