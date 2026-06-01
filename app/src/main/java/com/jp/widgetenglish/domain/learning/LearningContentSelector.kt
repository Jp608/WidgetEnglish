package com.jp.widgetenglish.domain.learning

import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import java.time.LocalDate
import kotlin.math.max
import kotlin.random.Random

object LearningContentSelector {

    fun seleccionarContenido(
        contenidos: List<LoteContenidoEntity>,
        progresos: List<ProgresoUsuarioEntity>,
        modo: ModoSeleccionContenido,
        limite: Int,
        diaInicioSecuencial: Int? = null,
        diaActual: Int = obtenerDiaLocalActual()
    ): List<LoteContenidoEntity> {
        if (contenidos.isEmpty()) return emptyList()

        val limiteSeguro = limite.coerceIn(1, contenidos.size)

        return when (modo) {
            ModoSeleccionContenido.SECUENCIAL -> {
                seleccionarSecuencial(
                    contenidos = contenidos,
                    progresos = progresos,
                    limite = limiteSeguro,
                    diasTranscurridos = calcularDiasTranscurridos(
                        diaInicio = diaInicioSecuencial,
                        diaActual = diaActual
                    )
                )
            }

            ModoSeleccionContenido.ALEATORIO -> {
                seleccionarAleatorioEstablePorDia(
                    contenidos = contenidos,
                    limite = limiteSeguro
                )
            }

            ModoSeleccionContenido.INTELIGENTE -> {
                seleccionarInteligente(
                    contenidos = contenidos,
                    progresos = progresos,
                    limite = limiteSeguro
                )
            }
        }
    }

    private fun seleccionarSecuencial(
        contenidos: List<LoteContenidoEntity>,
        progresos: List<ProgresoUsuarioEntity>,
        limite: Int,
        diasTranscurridos: Int
    ): List<LoteContenidoEntity> {
        val contenidosOrdenados = contenidos.sortedBy { it.orden }
        val inicioDelDia = (diasTranscurridos * limite) % contenidosOrdenados.size
        val contenidosDesdeInicioDelDia = contenidosOrdenados.rotarDesde(inicioDelDia)
        val aprendidas = progresos
            .filter { it.estadoAprendizaje == EstadoAprendizaje.APRENDIDA }
            .map {
                claveProgreso(
                    contenidoId = it.contenidoId,
                    tipoContenido = it.tipoContenido
                )
            }
            .toSet()

        val pendientes = contenidosDesdeInicioDelDia.filterNot { contenido ->
            claveProgreso(
                contenidoId = contenido.contenidoId,
                tipoContenido = contenido.tipoContenido
            ) in aprendidas
        }

        val aprendidasParaRepaso = contenidosDesdeInicioDelDia.filter { contenido ->
            claveProgreso(
                contenidoId = contenido.contenidoId,
                tipoContenido = contenido.tipoContenido
            ) in aprendidas
        }

        return (pendientes + aprendidasParaRepaso).take(limite)
    }

    private fun seleccionarAleatorioEstablePorDia(
        contenidos: List<LoteContenidoEntity>,
        limite: Int
    ): List<LoteContenidoEntity> {
        val semillaDiaria = obtenerSemillaDiaria()

        return contenidos
            .sortedBy { it.orden }
            .shuffled(Random(semillaDiaria))
            .take(limite)
    }

    private fun seleccionarInteligente(
        contenidos: List<LoteContenidoEntity>,
        progresos: List<ProgresoUsuarioEntity>,
        limite: Int
    ): List<LoteContenidoEntity> {
        val progresosPorContenido = progresos.associateBy {
            claveProgreso(
                contenidoId = it.contenidoId,
                tipoContenido = it.tipoContenido
            )
        }

        return contenidos
            .map { contenido ->
                val progreso = progresosPorContenido[
                    claveProgreso(
                        contenidoId = contenido.contenidoId,
                        tipoContenido = contenido.tipoContenido
                    )
                ]

                contenido to calcularPrioridad(progreso)
            }
            .sortedWith(
                compareByDescending<Pair<LoteContenidoEntity, Int>> { it.second }
                    .thenBy { it.first.orden }
            )
            .take(limite)
            .map { it.first }
    }

    private fun calcularPrioridad(
        progreso: ProgresoUsuarioEntity?
    ): Int {
        if (progreso == null) {
            return 100
        }

        val prioridadBase = when (progreso.estadoAprendizaje) {
            EstadoAprendizaje.NO_VISTA -> 100
            EstadoAprendizaje.DIFICIL -> 95
            EstadoAprendizaje.EN_PROGRESO -> 70
            EstadoAprendizaje.APRENDIDA -> 20
        }

        val penalizacionDominio = (progreso.nivelDominio * 30).toInt()

        val bonusErrores = progreso.respuestasIncorrectas * 5

        val bonusPocoRepaso = max(
            0,
            5 - progreso.vecesRepasado
        ) * 3

        val bonusRevisionPendiente = if (
            progreso.proximaRevision != null &&
            progreso.proximaRevision <= System.currentTimeMillis()
        ) {
            25
        } else {
            0
        }

        return prioridadBase -
                penalizacionDominio +
                bonusErrores +
                bonusPocoRepaso +
                bonusRevisionPendiente
    }

    private fun obtenerSemillaDiaria(): Int {
        val calendar = java.util.Calendar.getInstance()

        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        return year * 10_000 + month * 100 + day
    }

    fun obtenerDiaLocalActual(): Int {
        return LocalDate.now().toEpochDay().toInt()
    }

    private fun calcularDiasTranscurridos(
        diaInicio: Int?,
        diaActual: Int
    ): Int {
        return (diaActual - (diaInicio ?: diaActual)).coerceAtLeast(0)
    }

    private fun <T> List<T>.rotarDesde(indice: Int): List<T> {
        if (isEmpty()) return emptyList()

        val indiceSeguro = indice.coerceIn(0, lastIndex)

        return drop(indiceSeguro) + take(indiceSeguro)
    }

    private fun claveProgreso(
        contenidoId: String,
        tipoContenido: TipoContenido
    ): String {
        return "${contenidoId}_${tipoContenido.name}"
    }
}
