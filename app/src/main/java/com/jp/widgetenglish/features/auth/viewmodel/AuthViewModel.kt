package com.jp.widgetenglish.features.auth.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.features.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun actualizarNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun actualizarCorreo(correo: String) {
        _uiState.value = _uiState.value.copy(correo = correo)
    }

    fun actualizarPassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun actualizarConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun registrar() {
        val state = _uiState.value

        if (state.nombre.isBlank() || state.correo.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Completa todos los campos")
            return
        }

        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "La contraseña debe tener mínimo 6 caracteres")
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(cargando = true, error = null, mensaje = null)

            val result = authRepository.registrarConCorreo(
                nombre = state.nombre,
                correo = state.correo,
                password = state.password
            )

            result.onSuccess { firebaseUser ->
                val usuario = UsuarioEntity(
                    idUsuario = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    nombre = state.nombre,
                    correo = state.correo,
                    rol = RolUsuario.USUARIO,
                    activo = true
                )

                usuarioDao.insertarUsuario(usuario)

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    autenticado = true,
                    mensaje = "Registro exitoso"
                )
            }

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = error.message ?: "Error al registrar usuario"
                )
            }
        }
    }

    fun iniciarSesion() {
        val state = _uiState.value

        if (state.correo.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa correo y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(cargando = true, error = null, mensaje = null)

            val result = authRepository.iniciarSesionConCorreo(
                correo = state.correo,
                password = state.password
            )

            result.onSuccess { firebaseUser ->
                val usuarioExistente = usuarioDao.obtenerUsuarioPorFirebaseUid(firebaseUser.uid)

                if (usuarioExistente == null) {
                    val usuario = UsuarioEntity(
                        idUsuario = firebaseUser.uid,
                        firebaseUid = firebaseUser.uid,
                        nombre = firebaseUser.displayName ?: "Usuario",
                        correo = firebaseUser.email ?: state.correo,
                        rol = RolUsuario.USUARIO,
                        activo = true,
                        ultimoAcceso = System.currentTimeMillis()
                    )

                    usuarioDao.insertarUsuario(usuario)
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    autenticado = true,
                    mensaje = "Inicio de sesión exitoso"
                )
            }

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = error.message ?: "Credenciales incorrectas"
                )
            }
        }
    }

    fun recuperarPassword() {
        val correo = _uiState.value.correo

        if (correo.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa tu correo")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val result = authRepository.recuperarPassword(correo)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensaje = "Si el correo está registrado, recibirás instrucciones"
                )
            }

            result.onFailure {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensaje = "Si el correo está registrado, recibirás instrucciones"
                )
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.value = AuthUiState()
    }
}