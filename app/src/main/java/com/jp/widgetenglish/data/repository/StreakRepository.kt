package com.jp.widgetenglish.data.repository

import android.util.Log
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.ProgresoDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.remote.firestore.EstadisticasFirestoreDataSource
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StreakRepository(
    private val actividadDiariaDao: ActividadDiariaDao,
    private val usuarioDao: UsuarioDao,
    private val progresoDao: ProgresoDao,
    private val estadisticasFirestoreDataSource: EstadisticasFirestoreDataSource? = null
) {

    suspend fun registrarActividadDiaria(
        usuarioId: String,
        elementosEstudiados: Int = 0,
        tarjetasEstudiadas: Int = 0,
        preguntasQuizRespondidas: Int = 0,
        quizzesCompletados: Int = 0,
        objetivoDiario: Int = OBJETIVO_DIARIO_DEFAULT
    ) {
        if (usuarioId.isBlank()) return

        val hoy = obtenerFechaActual()
        val ahora = System.currentTimeMillis()

        val actividadActual = actividadDiariaDao.obtenerActividadPorFecha(
            usuarioId = usuarioId,
            fecha = hoy
        )

        val nuevaActividad = if (actividadActual == null) {
            ActividadDiariaEntity(
                usuarioId = usuarioId,
                fecha = hoy,
                elementosEstudiados = elementosEstudiados,
                tarjetasEstudiadas = tarjetasEstudiadas,
                preguntasQuizRespondidas = preguntasQuizRespondidas,
                quizzesCompletados = quizzesCompletados,
                objetivoDiario = objetivoDiario,
                objetivoCumplido = elementosEstudiados >= objetivoDiario,
                fechaCumplimiento = if (elementosEstudiados >= objetivoDiario) ahora else null,
                ultimaActualizacion = ahora
            )
        } else {
            val totalElementos = actividadActual.elementosEstudiados + elementosEstudiados
            val objetivoYaCumplido = actividadActual.objetivoCumplido
            val objetivoCumplidoAhora = totalElementos >= actividadActual.objetivoDiario

            actividadActual.copy(
                elementosEstudiados = totalElementos,
                tarjetasEstudiadas = actividadActual.tarjetasEstudiadas + tarjetasEstudiadas,
                preguntasQuizRespondidas = actividadActual.preguntasQuizRespondidas + preguntasQuizRespondidas,
                quizzesCompletados = actividadActual.quizzesCompletados + quizzesCompletados,
                objetivoCumplido = objetivoYaCumplido || objetivoCumplidoAhora,
                fechaCumplimiento = when {
                    objetivoYaCumplido -> actividadActual.fechaCumplimiento
                    objetivoCumplidoAhora -> ahora
                    else -> null
                },
                ultimaActualizacion = ahora
            )
        }

        actividadDiariaDao.insertarOActualizarActividad(nuevaActividad)

        val antesNoCumplia = actividadActual?.objetivoCumplido != true
        val ahoraCumple = nuevaActividad.objetivoCumplido

        if (antesNoCumplia && ahoraCumple) {
            actualizarRachaUsuario(
                usuarioId = usuarioId,
                fechaCumplida = hoy
            )
        } else {
            sincronizarActividadYUsuarioSiEsPosible(
                usuarioId = usuarioId,
                actividad = nuevaActividad
            )
        }
    }

    suspend fun registrarTarjetasEstudiadas(
        usuarioId: String,
        cantidad: Int
    ) {
        registrarActividadDiaria(
            usuarioId = usuarioId,
            elementosEstudiados = cantidad,
            tarjetasEstudiadas = cantidad
        )
    }

    suspend fun registrarQuizCompletado(
        usuarioId: String,
        preguntasRespondidas: Int
    ) {
        registrarActividadDiaria(
            usuarioId = usuarioId,
            elementosEstudiados = preguntasRespondidas,
            preguntasQuizRespondidas = preguntasRespondidas,
            quizzesCompletados = 1
        )
    }

    suspend fun sincronizarEstadisticasActuales(
        usuarioId: String
    ) {
        if (usuarioId.isBlank()) return

        val usuarioBase = usuarioDao.obtenerUsuarioPorFirebaseUid(usuarioId)
            ?: usuarioDao.obtenerUsuarioPorId(usuarioId)
            ?: return

        val usuarioActualizado = recalcularEstadisticasGeneralesUsuario(
            usuarioId = usuarioId,
            usuario = usuarioBase
        )

        val hoy = obtenerFechaActual()

        val actividadHoy = actividadDiariaDao.obtenerActividadPorFecha(
            usuarioId = usuarioId,
            fecha = hoy
        )

        if (actividadHoy != null) {
            sincronizarActividadYUsuarioSiEsPosible(
                usuarioId = usuarioId,
                actividad = actividadHoy,
                usuario = usuarioActualizado
            )
        } else {
            sincronizarUsuarioSiEsPosible(
                usuarioId = usuarioId,
                usuario = usuarioActualizado
            )
        }
    }

    private suspend fun actualizarRachaUsuario(
        usuarioId: String,
        fechaCumplida: String
    ) {
        val usuario = usuarioDao.obtenerUsuarioPorFirebaseUid(usuarioId)
            ?: usuarioDao.obtenerUsuarioPorId(usuarioId)
            ?: return

        val ultimaFechaRacha = usuario.ultimaFechaRacha

        if (ultimaFechaRacha == fechaCumplida) {
            val actividadHoy = actividadDiariaDao.obtenerActividadPorFecha(
                usuarioId = usuarioId,
                fecha = fechaCumplida
            )

            if (actividadHoy != null) {
                sincronizarActividadYUsuarioSiEsPosible(
                    usuarioId = usuarioId,
                    actividad = actividadHoy
                )
            }

            return
        }

        val ayer = obtenerFechaAnterior(fechaCumplida)

        val nuevaRacha = if (ultimaFechaRacha == ayer) {
            usuario.rachaActual + 1
        } else {
            1
        }

        val nuevaRachaMaxima = maxOf(
            usuario.rachaMaxima,
            nuevaRacha
        )

        val usuarioActualizado = usuario.copy(
            rachaActual = nuevaRacha,
            rachaMaxima = nuevaRachaMaxima,
            ultimaFechaRacha = fechaCumplida,
            fechaUltimaActividad = fechaCumplida,
            ultimoAcceso = System.currentTimeMillis()
        )

        usuarioDao.actualizarUsuario(usuarioActualizado)

        val actividadHoy = actividadDiariaDao.obtenerActividadPorFecha(
            usuarioId = usuarioId,
            fecha = fechaCumplida
        )

        if (actividadHoy != null) {
            sincronizarActividadYUsuarioSiEsPosible(
                usuarioId = usuarioId,
                actividad = actividadHoy,
                usuario = usuarioActualizado
            )
        }
    }

    private suspend fun recalcularEstadisticasGeneralesUsuario(
        usuarioId: String,
        usuario: UsuarioEntity
    ): UsuarioEntity {
        val progresosLotes = progresoDao
            .observarProgresosLotesUsuario(usuarioId)
            .first()

        val progresosUsuario = progresoDao
            .observarProgresoUsuario(usuarioId)
            .first()

        val palabrasAprendidas = progresosUsuario.count { progreso ->
            progreso.estadoAprendizaje == EstadoAprendizaje.APRENDIDA
        }

        if (progresosLotes.isEmpty()) {
            val usuarioActualizado = usuario.copy(
                palabrasAprendidas = palabrasAprendidas
            )

            usuarioDao.actualizarUsuario(usuarioActualizado)

            return usuarioActualizado
        }

        val lotesCompletados = progresosLotes.count { progreso ->
            progreso.progresoPorcentaje >= 100f ||
                    (
                            progreso.totalContenidos > 0 &&
                                    progreso.contenidosAprendidos >= progreso.totalContenidos
                            )
        }

        val porcentajeProgreso = progresosLotes
            .map { progreso -> progreso.progresoPorcentaje }
            .average()
            .toInt()
            .coerceIn(0, 100)

        val usuarioActualizado = usuario.copy(
            palabrasAprendidas = palabrasAprendidas,
            lotesCompletados = lotesCompletados,
            porcentajeProgreso = porcentajeProgreso
        )

        usuarioDao.actualizarUsuario(usuarioActualizado)

        return usuarioActualizado
    }

    private suspend fun sincronizarActividadYUsuarioSiEsPosible(
        usuarioId: String,
        actividad: ActividadDiariaEntity,
        usuario: UsuarioEntity? = null
    ) {
        val dataSource = estadisticasFirestoreDataSource ?: return

        try {
            dataSource.sincronizarActividadDiaria(
                firebaseUid = usuarioId,
                actividad = actividad
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando actividad diaria con Firestore", e)
        }

        val usuarioLocalBase = usuario
            ?: usuarioDao.obtenerUsuarioPorFirebaseUid(usuarioId)
            ?: usuarioDao.obtenerUsuarioPorId(usuarioId)

        if (usuarioLocalBase == null) return

        val usuarioLocal = recalcularEstadisticasGeneralesUsuario(
            usuarioId = usuarioId,
            usuario = usuarioLocalBase
        )

        sincronizarUsuarioSiEsPosible(
            usuarioId = usuarioId,
            usuario = usuarioLocal
        )
    }

    private suspend fun sincronizarUsuarioSiEsPosible(
        usuarioId: String,
        usuario: UsuarioEntity
    ) {
        val dataSource = estadisticasFirestoreDataSource ?: return

        try {
            dataSource.sincronizarEstadisticasUsuario(
                firebaseUid = usuarioId,
                rachaActual = usuario.rachaActual,
                rachaMaxima = usuario.rachaMaxima,
                ultimaFechaRacha = usuario.ultimaFechaRacha,
                fechaUltimaActividad = usuario.fechaUltimaActividad,
                palabrasAprendidas = usuario.palabrasAprendidas,
                quizzesRealizados = usuario.quizzesRealizados,
                lotesCompletados = usuario.lotesCompletados,
                porcentajeProgreso = usuario.porcentajeProgreso
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando estadísticas de usuario con Firestore", e)
        }
    }

    private fun obtenerFechaActual(): String {
        return FORMATO_FECHA.format(Date())
    }

    private fun obtenerFechaAnterior(fecha: String): String {
        val date = FORMATO_FECHA.parse(fecha) ?: Date()

        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, -1)
        }

        return FORMATO_FECHA.format(calendar.time)
    }

    companion object {
        private const val TAG = "StreakRepository"
        private const val OBJETIVO_DIARIO_DEFAULT = 10

        private val FORMATO_FECHA = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )
    }
}