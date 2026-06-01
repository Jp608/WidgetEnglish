package com.jp.widgetenglish.features.home.presentation.viewmodel

import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.VerboEntity

data class HomeUiState(
    val cargando: Boolean = true,

    val nombreUsuario: String = "Usuario",
    val correoUsuario: String = "",

    val rachaActual: Int = 0,
    val rachaMaxima: Int = 0,

    val palabras: List<PalabraEntity> = emptyList(),
    val verbos: List<VerboEntity> = emptyList(),
    val lotes: List<LoteEntity> = emptyList(),

    val loteActivo: ProgresoLoteEntity? = null,
    val loteActivoInfo: LoteEntity? = null,

    val objetivoDiario: Int = 10,
    val progresoDiario: Int = 0,
    val objetivoDiarioCumplido: Boolean = false,

    val error: String? = null
)