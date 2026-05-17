package com.jp.widgetenglish.data.local.seed

import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.NivelLote
import com.jp.widgetenglish.data.local.entity.TipoLote

object SeedLotes {
    val lotes = listOf(
        LoteEntity(
            idLote = "lote_mascotas",
            nombre = "Mascotas",
            descripcion = "Vocabulario de animales domésticos y mascotas comunes.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#43A047",
            icono = "pets",
            activo = true,
            orden = 1,
            cantidadContenido = 21,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_A1",
            nombre = "Verbos A1",
            descripcion = "Verbos básicos frecuentes para nivel A1.",
            tipoLote = TipoLote.VERBOS,
            nivel = NivelLote.A1,
            colorHex = "#1565C0",
            icono = "school",
            activo = true,
            orden = 2,
            cantidadContenido = 29,
            cantidadSugeridaEstudio = 10
        )
    )
}