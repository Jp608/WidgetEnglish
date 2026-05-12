package com.jp.widgetenglish.data.local.entity



enum class RolUsuario {
    USUARIO,
    ADMIN
}

enum class TipoPalabra {
    SUSTANTIVO,
    ADJETIVO,
    ADVERBIO,
    PRONOMBRE,
    PREPOSICION,
    CONJUNCION,
    EXPRESION,
    OTRO
}

enum class TipoContenido {
    PALABRA,
    VERBO
}

enum class EstadoAprendizaje {
    NO_VISTA,
    EN_PROGRESO,
    DIFICIL,
    APRENDIDA
}

enum class Dificultad {
    FACIL,
    MEDIA,
    DIFICIL
}

enum class TipoLote {
    TEMATICO,
    VERBOS,
    NIVEL,
    PERSONALIZADO
}

enum class NivelLote {
    A1,
    A2,
    B1,
    B2,
    GENERAL
}