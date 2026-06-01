package com.jp.widgetenglish.domain.learning

import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningContentSelectorTest {

    @Test
    fun secuencial_enPrimerDiaTomaPrimerBloque() {
        val seleccion = LearningContentSelector.seleccionarContenido(
            contenidos = contenidos(25),
            progresos = emptyList(),
            modo = ModoSeleccionContenido.SECUENCIAL,
            limite = 10,
            diaInicioSecuencial = 100,
            diaActual = 100
        )

        assertEquals(
            (1..10).map { "contenido_$it" },
            seleccion.map { it.contenidoId }
        )
    }

    @Test
    fun secuencial_enSegundoDiaTomaSiguienteBloque() {
        val seleccion = LearningContentSelector.seleccionarContenido(
            contenidos = contenidos(25),
            progresos = emptyList(),
            modo = ModoSeleccionContenido.SECUENCIAL,
            limite = 10,
            diaInicioSecuencial = 100,
            diaActual = 101
        )

        assertEquals(
            (11..20).map { "contenido_$it" },
            seleccion.map { it.contenidoId }
        )
    }

    @Test
    fun secuencial_reemplazaAprendidasPorPendientes() {
        val seleccion = LearningContentSelector.seleccionarContenido(
            contenidos = contenidos(25),
            progresos = listOf(
                progresoAprendido(1),
                progresoAprendido(2)
            ),
            modo = ModoSeleccionContenido.SECUENCIAL,
            limite = 10,
            diaInicioSecuencial = 100,
            diaActual = 100
        )

        assertEquals(
            (3..12).map { "contenido_$it" },
            seleccion.map { it.contenidoId }
        )
    }

    private fun contenidos(total: Int): List<LoteContenidoEntity> {
        return (1..total).map { orden ->
            LoteContenidoEntity(
                id = "lc_$orden",
                loteId = "lote_test",
                contenidoId = "contenido_$orden",
                tipoContenido = TipoContenido.PALABRA,
                orden = orden
            )
        }
    }

    private fun progresoAprendido(orden: Int): ProgresoUsuarioEntity {
        return ProgresoUsuarioEntity(
            id = "user_contenido_${orden}_PALABRA",
            usuarioId = "user",
            contenidoId = "contenido_$orden",
            tipoContenido = TipoContenido.PALABRA,
            estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
            aprendido = true
        )
    }
}
