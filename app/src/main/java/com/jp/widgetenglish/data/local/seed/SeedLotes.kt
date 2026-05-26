package com.jp.widgetenglish.data.local.seed

import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.NivelLote
import com.jp.widgetenglish.data.local.entity.TipoLote

object SeedLotes {
    val lotes = listOf(
        LoteEntity(
            idLote = "lote_mascotas",
            nombre = "Animales",
            descripcion = "Vocabulario de animales comunes.",
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
            idLote = "lote_saludos",
            nombre = "Saludos y Cortesía",
            descripcion = "Palabras básicas para saludar y cortesía.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#673AB7",
            icono = "waving_hand",
            activo = true,
            orden = 3,
            cantidadContenido = 5,
            cantidadSugeridaEstudio = 5
        ),
        LoteEntity(
            idLote = "lote_comida",
            nombre = "Comida",
            descripcion = "Vocabulario sobre alimentos y bebidas.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#E64A19",
            icono = "restaurant",
            activo = true,
            orden = 4,
            cantidadContenido = 5,
            cantidadSugeridaEstudio = 10
        ),

        LoteEntity(
            idLote = "lote_adjetivos_A1",
            nombre = "Adjetivos Básicos",
            descripcion = "Adjetivos esenciales para describir cosas y sentimientos.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.A1,
            colorHex = "#FF8F00",
            icono = "description",
            activo = true,
            orden = 3,
            cantidadContenido = 22,
            cantidadSugeridaEstudio = 10
        ),

        // Pegar estos elementos dentro de listOf(...) en SeedLotes.kt, antes del cierre final.

        LoteEntity(
            idLote = "lote_comida_bebidas",
            nombre = "Comida y Bebidas",
            descripcion = "Vocabulario sobre alimentos, bebidas y productos comunes de comida.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#E64A19",
            icono = "restaurant",
            activo = true,
            orden = 6,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_tecnologia",
            nombre = "Tecnología",
            descripcion = "Vocabulario básico relacionado con dispositivos, internet y herramientas digitales.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#00897B",
            icono = "devices",
            activo = true,
            orden = 7,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),

        LoteEntity(
            idLote = "lote_casa",
            nombre = "La Casa",
            descripcion = "Vocabulario de objetos, espacios y elementos comunes del hogar.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#5D4037",
            icono = "home",
            activo = true,
            orden = 5,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_escuela",
            nombre = "Escuela",
            descripcion = "Vocabulario relacionado con clases, útiles escolares y vida académica.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#3949AB",
            icono = "school",
            activo = true,
            orden = 8,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),

        LoteEntity(
            idLote = "lote_regulares",
            nombre = "Verbos Regulares",
            descripcion = "Verbos regulares en inglés con pasado simple y participio.",
            tipoLote = TipoLote.VERBOS,
            nivel = NivelLote.GENERAL,
            colorHex = "#00897B",
            icono = "edit_note",
            activo = true,
            orden = 8,
            cantidadContenido = 132,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_irregulares",
            nombre = "Verbos Irregulares",
            descripcion = "Verbos irregulares en inglés con sus formas principales.",
            tipoLote = TipoLote.VERBOS,
            nivel = NivelLote.GENERAL,
            colorHex = "#7B1FA2",
            icono = "bolt",
            activo = true,
            orden = 9,
            cantidadContenido = 144,
            cantidadSugeridaEstudio = 10
        )


    )
}
