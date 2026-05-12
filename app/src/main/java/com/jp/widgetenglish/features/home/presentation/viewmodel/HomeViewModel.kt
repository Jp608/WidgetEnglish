package com.jp.widgetenglish.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarHome()
    }

    fun cargarHome() {
        viewModelScope.launch {
            val firebaseUser = authRepository.obtenerUsuarioActual()

            if (firebaseUser == null) {
                _uiState.value = HomeUiState(
                    cargando = false,
                    error = "No hay usuario autenticado"
                )
                return@launch
            }

            val usuarioLocal = usuarioDao.obtenerUsuarioPorFirebaseUid(firebaseUser.uid)

            val nombre = usuarioLocal?.nombre
                ?: firebaseUser.displayName
                ?: "Usuario"

            val correo = usuarioLocal?.correo
                ?: firebaseUser.email
                ?: ""

            val racha = usuarioLocal?.rachaActual ?: 0

            combine(
                repository.observarPalabras(),
                repository.observarVerbos(),
                repository.observarLotes(),
                repository.observarLoteActivo(firebaseUser.uid)
            ) { palabras, verbos, lotes, loteActivo ->

                val loteActivoInfo = lotes.firstOrNull { lote ->
                    lote.idLote == loteActivo?.loteId
                }

                HomeUiState(
                    cargando = false,
                    nombreUsuario = nombre,
                    correoUsuario = correo,
                    rachaActual = racha,
                    palabras = palabras,
                    verbos = verbos,
                    lotes = lotes,
                    loteActivo = loteActivo,
                    loteActivoInfo = loteActivoInfo,
                    objetivoDiario = 10,
                    progresoDiario = 0
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}