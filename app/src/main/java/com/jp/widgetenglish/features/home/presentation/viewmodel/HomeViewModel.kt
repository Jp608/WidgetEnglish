package com.jp.widgetenglish.features.home.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.repository.VocabularioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: VocabularioRepository
) : ViewModel() {

    private val usuarioIdPrueba = "usuario_prueba"

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observarPalabras(),
        repository.observarVerbos(),
        repository.observarLotes(),
        repository.observarLoteActivo(usuarioIdPrueba)
    ) { palabras, verbos, lotes, loteActivo ->
        HomeUiState(
            cargando = false,
            palabras = palabras,
            verbos = verbos,
            lotes = lotes,
            loteActivo = loteActivo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}