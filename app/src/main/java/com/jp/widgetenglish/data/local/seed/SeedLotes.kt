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
            cantidadContenido = 60,
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
        ),
        LoteEntity(
            idLote = "lote_emociones",
            nombre = "Emociones",
            descripcion = "Vocabulario para expresar emociones y estados de ánimo.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#D81B60",
            icono = "favorite",
            activo = true,
            orden = 10,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),

        LoteEntity(
            idLote = "lote_cuerpo",
            nombre = "Partes del cuerpo",
            descripcion = "Vocabulario relacionado con partes del cuerpo humano.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#D81B60",
            icono = "accessibility_new",
            activo = true,
            orden = 10,
            cantidadContenido = 108,
            cantidadSugeridaEstudio = 10
        ),



        // Inicio lotes importados desde CSV
        LoteEntity(
            idLote = "lote_expresiones_cotidianas",
            nombre = "Expresiones cotidianas",
            descripcion = "Frases y expresiones comunes para conversaciones del dia a dia.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#8E24AA",
            icono = "chat_bubble",
            activo = true,
            orden = 11,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_naturaleza_clima",
            nombre = "Naturaleza y clima",
            descripcion = "Vocabulario sobre naturaleza, paisaje, clima y fenomenos naturales.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#2E7D32",
            icono = "forest",
            activo = true,
            orden = 12,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_profesiones",
            nombre = "Profesiones",
            descripcion = "Vocabulario de trabajos, ocupaciones y roles profesionales.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#0277BD",
            icono = "work",
            activo = true,
            orden = 13,
            cantidadContenido = 50,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_phrasal_regulares",
            nombre = "Verbos frasales regulares",
            descripcion = "Verbos frasales regulares con pasado simple y participio.",
            tipoLote = TipoLote.VERBOS,
            nivel = NivelLote.GENERAL,
            colorHex = "#00897B",
            icono = "rule",
            activo = true,
            orden = 14,
            cantidadContenido = 54,
            cantidadSugeridaEstudio = 10
        ),
        LoteEntity(
            idLote = "lote_phrasal_irregulares",
            nombre = "Verbos frasales irregulares",
            descripcion = "Verbos frasales irregulares con sus formas principales.",
            tipoLote = TipoLote.VERBOS,
            nivel = NivelLote.GENERAL,
            colorHex = "#C62828",
            icono = "bolt",
            activo = true,
            orden = 15,
            cantidadContenido = 55,
            cantidadSugeridaEstudio = 10
        ),
        // Fin lotes importados desde CSV

        // Inicio lotes derivados
        LoteEntity(
            idLote = "lote_ingles_supervivencia",
            nombre = "Ingles de supervivencia",
            descripcion = "Expresiones esenciales para resolver situaciones basicas al comunicarse.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.A1,
            colorHex = "#1565C0",
            icono = "support_agent",
            activo = true,
            orden = 16,
            cantidadContenido = 25,
            cantidadSugeridaEstudio = 8
        ),
        LoteEntity(
            idLote = "lote_conversacion_a1",
            nombre = "Conversacion A1",
            descripcion = "Preguntas, respuestas y frases cortas para primeras conversaciones.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.A1,
            colorHex = "#6A1B9A",
            icono = "forum",
            activo = true,
            orden = 17,
            cantidadContenido = 22,
            cantidadSugeridaEstudio = 8
        ),
        LoteEntity(
            idLote = "lote_clima_basico",
            nombre = "Clima basico",
            descripcion = "Vocabulario frecuente para hablar del tiempo y fenomenos del clima.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.A1,
            colorHex = "#00838F",
            icono = "wb_sunny",
            activo = true,
            orden = 18,
            cantidadContenido = 22,
            cantidadSugeridaEstudio = 8
        ),
        LoteEntity(
            idLote = "lote_profesiones_servicio",
            nombre = "Profesiones de servicio",
            descripcion = "Trabajos de atencion, ayuda, servicios practicos y oficios cotidianos.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.GENERAL,
            colorHex = "#EF6C00",
            icono = "handyman",
            activo = true,
            orden = 19,
            cantidadContenido = 20,
            cantidadSugeridaEstudio = 8
        ),
        LoteEntity(
            idLote = "lote_phrasal_esenciales",
            nombre = "Phrasal verbs esenciales",
            descripcion = "Seleccion de verbos frasales comunes para comunicacion diaria.",
            tipoLote = TipoLote.VERBOS,
            nivel = NivelLote.GENERAL,
            colorHex = "#AD1457",
            icono = "stars",
            activo = true,
            orden = 20,
            cantidadContenido = 30,
            cantidadSugeridaEstudio = 10
        ),
        // Fin lotes derivados
    )
}
