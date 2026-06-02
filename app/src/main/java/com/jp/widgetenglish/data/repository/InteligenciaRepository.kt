package com.jp.widgetenglish.data.repository

import com.jp.widgetenglish.data.local.dao.InteligenciaDao
import com.jp.widgetenglish.data.local.dao.ProgresoDao
import com.jp.widgetenglish.data.local.entity.DificultadUsuarioEntity
import com.jp.widgetenglish.data.local.entity.InteresUsuarioEntity
import com.jp.widgetenglish.data.remote.ai.GroqAiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface InteligenciaRepository {
    fun observarIntereses(usuarioId: String): Flow<List<InteresUsuarioEntity>>
    fun observarDificultades(usuarioId: String): Flow<List<DificultadUsuarioEntity>>
    suspend fun registrarFallo(usuarioId: String, tema: String)
    suspend fun analizarInteresesDeTexto(usuarioId: String, texto: String)
    suspend fun obtenerPrediccionNivel(usuarioId: String, racha: Int): String
}

class InteligenciaRepositoryImpl(
    private val inteligenciaDao: InteligenciaDao,
    private val progresoDao: ProgresoDao,
    private val aiClient: GroqAiClient
) : InteligenciaRepository {

    override fun observarIntereses(usuarioId: String) = inteligenciaDao.observarIntereses(usuarioId)

    override fun observarDificultades(usuarioId: String) = inteligenciaDao.observarDificultades(usuarioId)

    override suspend fun registrarFallo(usuarioId: String, tema: String) {
        val existente = inteligenciaDao.obtenerDificultadPorTema(usuarioId, tema)
        if (existente != null) {
            inteligenciaDao.insertarDificultad(existente.copy(fallos = existente.fallos + 1, ultimaVezFallado = System.currentTimeMillis()))
        } else {
            inteligenciaDao.insertarDificultad(DificultadUsuarioEntity(usuarioId = usuarioId, tema = tema, fallos = 1))
        }
    }

    override suspend fun analizarInteresesDeTexto(usuarioId: String, texto: String) {
        val intereses = aiClient.extraerIntereses(texto)
        intereses.forEach { nombre ->
            val existente = inteligenciaDao.obtenerInteresPorNombre(usuarioId, nombre)
            if (existente != null) {
                inteligenciaDao.insertarInteres(existente.copy(frecuencia = existente.frecuencia + 1))
            } else {
                inteligenciaDao.insertarInteres(InteresUsuarioEntity(usuarioId = usuarioId, interes = nombre))
            }
        }
    }

    override suspend fun obtenerPrediccionNivel(usuarioId: String, racha: Int): String {
        val progresos = progresoDao.observarProgresoUsuario(usuarioId).first()
        val aprendidas = progresos.count { it.aprendido }
        val errores = progresos.sumOf { it.respuestasIncorrectas }
        
        return aiClient.predecirNivel(aprendidas, errores, racha)
    }
}
