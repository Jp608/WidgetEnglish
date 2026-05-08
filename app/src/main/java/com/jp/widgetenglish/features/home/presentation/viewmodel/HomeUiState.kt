package com.jp.widgetenglish.features.home.presentation.viewmodel


import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.VerboEntity

data class HomeUiState(
    val cargando: Boolean = true,
    val palabras: List<PalabraEntity> = emptyList(),
    val verbos: List<VerboEntity> = emptyList(),
    val lotes: List<LoteEntity> = emptyList(),
    val loteActivo: ProgresoLoteEntity? = null,
    val error: String? = null
)