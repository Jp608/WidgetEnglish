package com.jp.widgetenglish.data.repository

import com.jp.widgetenglish.data.local.dao.LoteConProgreso
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
import kotlinx.coroutines.flow.first

class VocabularioRepositoryImpl(
    private val palabraDao: PalabraDao,
    private val verboDao: VerboDao,
    private val loteDao: LoteDao,
    private val progresoDao: ProgresoDao,
    private val usuarioFirestoreDataSource: com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource? = null
) : VocabularioRepository {

    // -------------------------
    // PALABRAS Y VERBOS
    // -------------------------

    override fun observarPalabras(): Flow<List<PalabraEntity>> = palabraDao.observarPalabras()
    override fun observarVerbos(): Flow<List<VerboEntity>> = verboDao.observarVerbos()
    override suspend fun obtenerPalabraPorId(idPalabra: String): PalabraEntity? = palabraDao.obtenerPalabraPorId(idPalabra)
    override suspend fun obtenerVerboPorId(idVerbo: String): VerboEntity? = verboDao.obtenerVerboPorId(idVerbo)

    // -------------------------
    // LOTES
    // -------------------------

    override fun observarLotes(): Flow<List<LoteEntity>> = loteDao.observarLotes()
    override fun observarLotesConProgreso(usuarioId: String): Flow<List<LoteConProgreso>> = loteDao.observarLotesConProgreso(usuarioId)
    override fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>> = loteDao.observarContenidoDeLote(loteId)
    override suspend fun obtenerLotePorId(idLote: String): LoteEntity? = loteDao.obtenerLotePorId(idLote)

    override suspend fun activarLote(usuarioId: String, loteId: String) {
        progresoDao.desactivarLotes(usuarioId)
        
        // Sincronizar con Firestore (Cambio solicitado)
        try {
            usuarioFirestoreDataSource?.guardarLoteActivo(usuarioId, loteId)
        } catch (e: Exception) {}

        // Recalcular para sincronizar (Fix HU13-14)
        val contenidos = loteDao.observarContenidoDeLote(loteId).first()
        val total = contenidos.size
        val aprendidas = progresoDao.observarContenidosAprendidos(usuarioId).first().count { p ->
            contenidos.any { c -> c.contenidoId == p.contenidoId && c.tipoContenido == p.tipoContenido }
        }
        val porcentaje = if (total > 0) (aprendidas.toFloat() / total.toFloat()) * 100f else 0f

        val existente = progresoDao.obtenerProgresoLote(usuarioId, loteId)
        if (existente == null) {
            progresoDao.insertarProgresoLote(
                ProgresoLoteEntity(
                    id = "pl_${usuarioId}_$loteId",
                    usuarioId = usuarioId,
                    loteId = loteId,
                    activo = true,
                    progresoPorcentaje = porcentaje,
                    contenidosAprendidos = aprendidas,
                    totalContenidos = total,
                    fechaUltimoEstudio = System.currentTimeMillis()
                )
            )
        } else {
            progresoDao.actualizarProgresoLoteFull(usuarioId, loteId, true, porcentaje, aprendidas, total)
        }
    }

    override suspend fun desactivarLotes(usuarioId: String) = progresoDao.desactivarLotes(usuarioId)

    // -------------------------
    // PROGRESO DE USUARIO
    // -------------------------

    override fun observarProgresoUsuario(usuarioId: String): Flow<List<ProgresoUsuarioEntity>> = progresoDao.observarProgresoUsuario(usuarioId)
    override fun observarContenidosAprendidos(usuarioId: String): Flow<List<ProgresoUsuarioEntity>> = progresoDao.observarContenidosAprendidos(usuarioId)
    override fun observarContenidosPorEstado(usuarioId: String, estado: EstadoAprendizaje): Flow<List<ProgresoUsuarioEntity>> = 
        progresoDao.observarContenidosPorEstado(usuarioId, estado)

    override suspend fun obtenerProgresoContenido(usuarioId: String, contenidoId: String, tipoContenido: TipoContenido): ProgresoUsuarioEntity? =
        progresoDao.obtenerProgresoContenido(usuarioId, contenidoId, tipoContenido)

    override suspend fun sincronizarProgresos(usuarioId: String) {
        sincronizarTodosLosLotes(usuarioId)
    }

    override suspend fun guardarProgresoUsuario(progreso: ProgresoUsuarioEntity) {
        progresoDao.insertarProgresoUsuario(progreso)
        sincronizarTodosLosLotes(progreso.usuarioId)
    }

    override suspend fun marcarContenidoComoAprendido(usuarioId: String, contenidoId: String, tipoContenido: TipoContenido) {
        val existente = progresoDao.obtenerProgresoContenido(usuarioId, contenidoId, tipoContenido)
        if (existente == null) {
            progresoDao.insertarProgresoUsuario(
                ProgresoUsuarioEntity(
                    id = "pu_${usuarioId}_$contenidoId",
                    usuarioId = usuarioId,
                    contenidoId = contenidoId,
                    tipoContenido = tipoContenido,
                    estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                    aprendido = true
                )
            )
        } else {
            progresoDao.marcarContenidoComoAprendido(usuarioId, contenidoId, tipoContenido)
        }
        sincronizarTodosLosLotes(usuarioId)
    }

    override suspend fun marcarContenidoEnProgreso(usuarioId: String, contenidoId: String, tipoContenido: TipoContenido) {
        progresoDao.marcarContenidoEnProgreso(usuarioId, contenidoId, tipoContenido)
        sincronizarTodosLosLotes(usuarioId)
    }

    override suspend fun marcarContenidoComoDificil(usuarioId: String, contenidoId: String, tipoContenido: TipoContenido) {
        progresoDao.marcarContenidoComoDificil(usuarioId, contenidoId, tipoContenido)
        sincronizarTodosLosLotes(usuarioId)
    }

    override suspend fun revertirContenidoAprendido(usuarioId: String, contenidoId: String, tipoContenido: TipoContenido) {
        progresoDao.revertirContenidoAprendido(usuarioId, contenidoId, tipoContenido)
        sincronizarTodosLosLotes(usuarioId)
    }

    override suspend fun actualizarFavorito(usuarioId: String, contenidoId: String, tipoContenido: TipoContenido, favorito: Boolean) =
        progresoDao.actualizarFavorito(usuarioId, contenidoId, tipoContenido, favorito)

    private suspend fun sincronizarTodosLosLotes(usuarioId: String) {
        val lotes = loteDao.observarLotes().first()
        val aprendidos = progresoDao.observarContenidosAprendidos(usuarioId).first()

        lotes.forEach { lote ->
            val contenidos = loteDao.observarContenidoDeLote(lote.idLote).first()
            val total = contenidos.size
            if (total > 0) {
                val aprendidas = aprendidos.count { p ->
                    contenidos.any { c -> c.contenidoId == p.contenidoId && c.tipoContenido == p.tipoContenido }
                }
                val porcentaje = (aprendidas.toFloat() / total.toFloat()) * 100f
                
                val pLote = progresoDao.obtenerProgresoLote(usuarioId, lote.idLote)
                val isActivoActual = pLote?.activo ?: false

                if (pLote == null) {
                    progresoDao.insertarProgresoLote(
                        ProgresoLoteEntity(
                            id = "pl_${usuarioId}_${lote.idLote}",
                            usuarioId = usuarioId,
                            loteId = lote.idLote,
                            activo = false,
                            progresoPorcentaje = porcentaje,
                            contenidosAprendidos = aprendidas,
                            totalContenidos = total,
                            fechaUltimoEstudio = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Mantenemos el estado 'activo' actual del registro de progreso
                    progresoDao.actualizarProgresoLoteFull(usuarioId, lote.idLote, isActivoActual, porcentaje, aprendidas, total)
                }
            }
        }
    }

    // -------------------------
    // PROGRESO DE LOTES
    // -------------------------

    override fun observarProgresoLotes(usuarioId: String): Flow<List<ProgresoLoteEntity>> = progresoDao.observarProgresoLotes(usuarioId)
    override fun observarLoteActivo(usuarioId: String): Flow<ProgresoLoteEntity?> = progresoDao.observarLoteActivo(usuarioId)
    override suspend fun obtenerProgresoLote(usuarioId: String, loteId: String): ProgresoLoteEntity? = progresoDao.obtenerProgresoLote(usuarioId, loteId)
    override suspend fun guardarProgresoLote(progreso: ProgresoLoteEntity) = progresoDao.insertarProgresoLote(progreso)
    override suspend fun actualizarProgresoLotePorcentaje(usuarioId: String, loteId: String, progresoPorcentaje: Float) =
        progresoDao.actualizarProgresoLotePorcentaje(usuarioId, loteId, progresoPorcentaje)

    override suspend fun reiniciarProgresoLote(usuarioId: String, loteId: String) {
        progresoDao.reiniciarProgresoLote(usuarioId, loteId)
        progresoDao.reiniciarProgresoContenidosLote(usuarioId, loteId)

        val pLote = progresoDao.obtenerProgresoLote(usuarioId, loteId)
        if (pLote != null) {
            progresoDao.actualizarProgresoLoteFull(usuarioId, loteId, pLote.activo, 0f, 0, pLote.totalContenidos)
        }
    }

    override suspend fun sincronizarLoteActivoConFirestore(usuarioId: String) {
        try {
            val loteIdRemoto = usuarioFirestoreDataSource?.obtenerLoteActivoId(usuarioId)
            if (!loteIdRemoto.isNullOrBlank()) {
                activarLote(usuarioId, loteIdRemoto)
            }
        } catch (e: Exception) {}
    }
}
