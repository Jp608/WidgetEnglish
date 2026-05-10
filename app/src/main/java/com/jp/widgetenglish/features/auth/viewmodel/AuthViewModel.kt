package com.jp.widgetenglish.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun actualizarNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre, error = null)
    }

    fun actualizarCorreo(correo: String) {
        _uiState.value = _uiState.value.copy(correo = correo, error = null)
    }

    fun actualizarPassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun actualizarConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun actualizarError(error: String?) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun registrar() {
        val state = _uiState.value

        if (state.nombre.isBlank() || state.correo.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank()) {
            _uiState.value = state.copy(error = "Por favor, completa todos los campos")
            return
        }

        if (!state.correo.matches(emailRegex)) {
            _uiState.value = state.copy(error = "El formato del correo no es válido")
            return
        }

        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(cargando = true, error = null)

            // SIMULACIÓN DE ÉXITO LOCAL
            val mockId = UUID.randomUUID().toString()
            val usuario = UsuarioEntity(
                idUsuario = mockId,
                firebaseUid = mockId,
                nombre = state.nombre,
                correo = state.correo
            )
            
            usuarioDao.insertarUsuario(usuario)

            _uiState.value = _uiState.value.copy(
                cargando = false,
                autenticado = true,
                mensaje = "¡Bienvenido (Modo Offline)!"
            )
        }
    }

    fun iniciarSesion() {
        val state = _uiState.value

        if (state.correo.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa tu correo y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(cargando = true, error = null)

            // SIMULACIÓN DE INICIO DE SESIÓN
            _uiState.value = _uiState.value.copy(
                cargando = false,
                autenticado = true,
                mensaje = "Sesión iniciada correctamente"
            )
        }
    }

    fun iniciarSesionConGoogle(credential: AuthCredential) {
        // En modo offline no funcionará Google, pero evitamos el crash
        _uiState.value = _uiState.value.copy(error = "Google requiere configuración de Firebase")
    }

    fun recuperarPassword() {
        _uiState.value = _uiState.value.copy(mensaje = "Simulación: Enlace enviado")
    }

    fun cerrarSesion() {
        _uiState.value = AuthUiState()
    }
}
