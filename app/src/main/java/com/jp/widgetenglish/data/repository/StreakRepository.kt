package com.jp.widgetenglish.data.repository

import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StreakRepository(
    private val actividadDiariaDao: ActividadDiariaDao,
    private val usuarioDao: UsuarioDao
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

    private suspend fun actualizarRachaUsuario(
        usuarioId: String,
        fechaCumplida: String
    ) {
        val usuario = usuarioDao.obtenerUsuarioPorFirebaseUid(usuarioId)
            ?: usuarioDao.obtenerUsuarioPorId(usuarioId)
            ?: return

        val ultimaFechaRacha = usuario.ultimaFechaRacha

        if (ultimaFechaRacha == fechaCumplida) {
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

        usuarioDao.actualizarUsuario(
            usuario.copy(
                rachaActual = nuevaRacha,
                rachaMaxima = nuevaRachaMaxima,
                ultimaFechaRacha = fechaCumplida,
                fechaUltimaActividad = fechaCumplida,
                ultimoAcceso = System.currentTimeMillis()
            )
        )
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
        private const val OBJETIVO_DIARIO_DEFAULT = 10

        private val FORMATO_FECHA = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )
    }
}