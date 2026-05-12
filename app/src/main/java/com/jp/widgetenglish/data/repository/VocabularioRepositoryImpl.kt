package com.jp.widgetenglish.data.repository


import com.jp.widgetenglish.data.local.dao.LoteDao
import com.jp.widgetenglish.data.local.dao.PalabraDao
import com.jp.widgetenglish.data.local.dao.ProgresoDao
import com.jp.widgetenglish.data.local.dao.VerboDao
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

    override fun observarPalabras(): Flow<List<PalabraEntity>> {
        return palabraDao.observarPalabras()
    }

    override fun observarVerbos(): Flow<List<VerboEntity>> {
        return verboDao.observarVerbos()
    }

    override fun observarLotes(): Flow<List<LoteEntity>> {
        return loteDao.observarLotes()
    }

    override fun observarContenidoDeLote(loteId: String): Flow<List<LoteContenidoEntity>> {
        return loteDao.observarContenidoDeLote(loteId)
    }

    override fun observarProgresoUsuario(usuarioId: String): Flow<List<ProgresoUsuarioEntity>> {
        return progresoDao.observarProgresoUsuario(usuarioId)
    }

    override fun observarProgresoLotes(usuarioId: String): Flow<List<ProgresoLoteEntity>> {
        return progresoDao.observarProgresoLotes(usuarioId)
    }

    override fun observarLoteActivo(usuarioId: String): Flow<ProgresoLoteEntity?> {
        return progresoDao.observarLoteActivo(usuarioId)
    }

    override suspend fun obtenerPalabraPorId(idPalabra: String): PalabraEntity? {
        return palabraDao.obtenerPalabraPorId(idPalabra)
    }

    override suspend fun obtenerVerboPorId(idVerbo: String): VerboEntity? {
        return verboDao.obtenerVerboPorId(idVerbo)
    }

    override suspend fun obtenerLotePorId(idLote: String): LoteEntity? {
        return loteDao.obtenerLotePorId(idLote)
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

    override suspend fun guardarProgresoLote(progreso: ProgresoLoteEntity) {
        progresoDao.insertarProgresoLote(progreso)
    }

    override suspend fun activarLote(usuarioId: String, loteId: String) {
        progresoDao.desactivarLotes(usuarioId)
        progresoDao.activarLote(usuarioId, loteId)
    }

    override suspend fun desactivarLotes(usuarioId: String) {
        progresoDao.desactivarLotes(usuarioId)
    }
}