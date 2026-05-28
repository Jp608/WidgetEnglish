package com.jp.widgetenglish.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.remote.firestore.EstadisticasFirestoreDataSource
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.features.widget.WordWidgetProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val actividadDiariaDao: ActividadDiariaDao,
    private val usuarioFirestoreDataSource: UsuarioFirestoreDataSource,
    private val estadisticasFirestoreDataSource: EstadisticasFirestoreDataSource,
    private val vocabularioRepository: VocabularioRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    init {
        verificarSesionActiva()
    }


    private suspend fun sincronizarActividadDiariaRemotaALocal(
        firebaseUid: String
    ) {
        try {
            val hoy = obtenerFechaActual()

            val actividadRemota = estadisticasFirestoreDataSource.obtenerActividadDiaria(
                firebaseUid = firebaseUid,
                fecha = hoy
            ) ?: return

            val actividadLocal = actividadDiariaDao.obtenerActividadPorFecha(
                usuarioId = firebaseUid,
                fecha = hoy
            )

            val actividadFinal = if (actividadLocal == null) {
                actividadRemota
            } else {
                actividadLocal.copy(
                    elementosEstudiados = maxOf(
                        actividadLocal.elementosEstudiados,
                        actividadRemota.elementosEstudiados
                    ),
                    tarjetasEstudiadas = maxOf(
                        actividadLocal.tarjetasEstudiadas,
                        actividadRemota.tarjetasEstudiadas
                    ),
                    preguntasQuizRespondidas = maxOf(
                        actividadLocal.preguntasQuizRespondidas,
                        actividadRemota.preguntasQuizRespondidas
                    ),
                    quizzesCompletados = maxOf(
                        actividadLocal.quizzesCompletados,
                        actividadRemota.quizzesCompletados
                    ),
                    objetivoDiario = maxOf(
                        actividadLocal.objetivoDiario,
                        actividadRemota.objetivoDiario
                    ),
                    objetivoCumplido = actividadLocal.objetivoCumplido || actividadRemota.objetivoCumplido,
                    fechaCumplimiento = actividadLocal.fechaCumplimiento
                        ?: actividadRemota.fechaCumplimiento,
                    ultimaActualizacion = maxOf(
                        actividadLocal.ultimaActualizacion,
                        actividadRemota.ultimaActualizacion
                    )
                )
            }

            actividadDiariaDao.insertarOActualizarActividad(actividadFinal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        return formato.format(Date())
    }

    private fun sincronizarWidgetTrasLogin(userId: String) {
        viewModelScope.launch {
            val loteActivo = vocabularioRepository.observarLoteActivo(userId).first()

            if (loteActivo != null) {
                val info = vocabularioRepository.obtenerLotePorId(loteActivo.loteId)

                if (info != null) {
                    WidgetPreferences.guardarLoteActivo(
                        context = context,
                        loteId = info.idLote,
                        loteNombre = info.nombre
                    )

                    WidgetPreferences.guardarUserId(
                        context = context,
                        userId = userId
                    )
                }
            }
        }
    }

    private suspend fun sincronizarEstadisticasRemotasALocal(
        firebaseUid: String
    ) {
        try {
            val usuarioLocal = usuarioDao.obtenerUsuarioPorFirebaseUid(firebaseUid)
                ?: return

            val statsRemotas = estadisticasFirestoreDataSource.obtenerEstadisticasUsuario(
                firebaseUid = firebaseUid
            ) ?: return

            val usuarioActualizado = usuarioLocal.copy(
                rachaActual = maxOf(
                    usuarioLocal.rachaActual,
                    statsRemotas.rachaActual
                ),
                rachaMaxima = maxOf(
                    usuarioLocal.rachaMaxima,
                    statsRemotas.rachaMaxima
                ),
                ultimaFechaRacha = elegirFechaMasReciente(
                    local = usuarioLocal.ultimaFechaRacha,
                    remota = statsRemotas.ultimaFechaRacha
                ),
                fechaUltimaActividad = elegirFechaMasReciente(
                    local = usuarioLocal.fechaUltimaActividad,
                    remota = statsRemotas.fechaUltimaActividad
                ),
                palabrasAprendidas = maxOf(
                    usuarioLocal.palabrasAprendidas,
                    statsRemotas.palabrasAprendidas
                ),
                quizzesRealizados = maxOf(
                    usuarioLocal.quizzesRealizados,
                    statsRemotas.quizzesRealizados
                ),
                lotesCompletados = maxOf(
                    usuarioLocal.lotesCompletados,
                    statsRemotas.lotesCompletados
                ),
                porcentajeProgreso = maxOf(
                    usuarioLocal.porcentajeProgreso,
                    statsRemotas.porcentajeProgreso
                )
            )

            usuarioDao.actualizarUsuario(usuarioActualizado)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun elegirFechaMasReciente(
        local: String?,
        remota: String?
    ): String? {
        return when {
            local.isNullOrBlank() -> remota
            remota.isNullOrBlank() -> local
            remota > local -> remota
            else -> local
        }
    }

    private suspend fun guardarUsuarioFirestoreYRoom(
        usuarioBase: UsuarioEntity
    ): UsuarioEntity {
        val usuarioConRol = usuarioFirestoreDataSource.crearUsuarioSiNoExiste(usuarioBase)

        return guardarUsuarioRoomPreservandoEstadisticas(usuarioConRol)
    }

    private suspend fun guardarUsuarioRoomPreservandoEstadisticas(
        usuarioNuevo: UsuarioEntity
    ): UsuarioEntity {
        val usuarioLocal = usuarioDao.obtenerUsuarioPorFirebaseUid(
            usuarioNuevo.firebaseUid
        )

        val usuarioFinal = if (usuarioLocal != null) {
            usuarioLocal.copy(
                nombre = usuarioNuevo.nombre.ifBlank { usuarioLocal.nombre },
                correo = usuarioNuevo.correo.ifBlank { usuarioLocal.correo },
                avatar = usuarioNuevo.avatar ?: usuarioLocal.avatar,
                rol = usuarioNuevo.rol,
                activo = usuarioNuevo.activo,
                ultimoAcceso = System.currentTimeMillis(),

                rachaActual = usuarioLocal.rachaActual,
                rachaMaxima = usuarioLocal.rachaMaxima,
                palabrasAprendidas = usuarioLocal.palabrasAprendidas,
                quizzesRealizados = usuarioLocal.quizzesRealizados,
                lotesCompletados = usuarioLocal.lotesCompletados,
                porcentajeProgreso = usuarioLocal.porcentajeProgreso,

                ultimaFechaRacha = usuarioLocal.ultimaFechaRacha,
                fechaUltimaActividad = usuarioLocal.fechaUltimaActividad
            )
        } else {
            usuarioNuevo.copy(
                ultimoAcceso = System.currentTimeMillis()
            )
        }

        usuarioDao.insertarUsuario(usuarioFinal)

        return usuarioFinal
    }

    fun actualizarNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(
            nombre = nombre,
            error = null
        )
    }

    fun actualizarCorreo(correo: String) {
        _uiState.value = _uiState.value.copy(
            correo = correo,
            error = null
        )
    }

    fun actualizarPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            error = null
        )
    }

    fun actualizarConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            error = null
        )
    }

    fun actualizarError(error: String?) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun registrar() {
        val state = _uiState.value

        if (state.nombre.isBlank() || state.correo.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Completa todos los campos")
            return
        }

        if (!state.correo.matches(emailRegex)) {
            _uiState.value = state.copy(error = "El formato del correo no es válido")
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
            _uiState.value = state.copy(
                cargando = true,
                error = null,
                mensaje = null
            )

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
                    avatar = firebaseUser.photoUrl?.toString(),
                    rol = RolUsuario.USUARIO,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis(),
                    ultimoAcceso = System.currentTimeMillis()
                )

                val usuarioConRol = guardarUsuarioFirestoreYRoom(usuario)

                sincronizarEstadisticasRemotasALocal(firebaseUser.uid)
                sincronizarActividadDiariaRemotaALocal(firebaseUser.uid)

                sincronizarWidgetTrasLogin(firebaseUser.uid)

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    autenticado = true,
                    rolUsuario = usuarioConRol.rol,
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

        if (!state.correo.matches(emailRegex)) {
            _uiState.value = state.copy(error = "El formato del correo no es válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                cargando = true,
                error = null,
                mensaje = null
            )

            val result = authRepository.iniciarSesionConCorreo(
                correo = state.correo,
                password = state.password
            )

            result.onSuccess { firebaseUser ->
                val usuarioBase = UsuarioEntity(
                    idUsuario = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario",
                    correo = firebaseUser.email ?: state.correo,
                    avatar = firebaseUser.photoUrl?.toString(),
                    rol = RolUsuario.USUARIO,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis(),
                    ultimoAcceso = System.currentTimeMillis()
                )

                val usuarioConRol = guardarUsuarioFirestoreYRoom(usuarioBase)

                sincronizarEstadisticasRemotasALocal(firebaseUser.uid)
                sincronizarActividadDiariaRemotaALocal(firebaseUser.uid)

                sincronizarWidgetTrasLogin(firebaseUser.uid)

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    autenticado = true,
                    rolUsuario = usuarioConRol.rol,
                    mensaje = "Inicio de sesión exitoso"
                )
            }

            result.onFailure {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "Correo o contraseña incorrectos"
                )
            }
        }
    }

    fun iniciarSesionConGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null,
                mensaje = null
            )

            val result = authRepository.iniciarSesionConGoogle(credential)

            result.onSuccess { firebaseUser ->
                val usuarioBase = UsuarioEntity(
                    idUsuario = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario",
                    correo = firebaseUser.email ?: "",
                    avatar = firebaseUser.photoUrl?.toString(),
                    rol = RolUsuario.USUARIO,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis(),
                    ultimoAcceso = System.currentTimeMillis()
                )

                val usuarioConRol = guardarUsuarioFirestoreYRoom(usuarioBase)

                sincronizarEstadisticasRemotasALocal(firebaseUser.uid)
                sincronizarActividadDiariaRemotaALocal(firebaseUser.uid)

                sincronizarWidgetTrasLogin(firebaseUser.uid)

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    autenticado = true,
                    rolUsuario = usuarioConRol.rol,
                    mensaje = "Inicio de sesión con Google exitoso"
                )
            }

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = error.message ?: "No se pudo iniciar sesión con Google"
                )
            }
        }
    }

    fun verificarSesionActiva() {
        viewModelScope.launch {
            val firebaseUser = authRepository.obtenerUsuarioActual()

            if (firebaseUser == null) {
                _uiState.value = _uiState.value.copy(
                    autenticado = false,
                    cargando = false,
                    rolUsuario = RolUsuario.USUARIO
                )
                return@launch
            }

            val usuarioFirestore = usuarioFirestoreDataSource.obtenerUsuario(firebaseUser.uid)

            if (usuarioFirestore != null) {
                val usuarioActualizado = guardarUsuarioRoomPreservandoEstadisticas(
                    usuarioFirestore.copy(
                        ultimoAcceso = System.currentTimeMillis()
                    )
                )

                sincronizarEstadisticasRemotasALocal(firebaseUser.uid)
                sincronizarActividadDiariaRemotaALocal(firebaseUser.uid)

                usuarioFirestoreDataSource.actualizarUltimoAcceso(firebaseUser.uid)

                sincronizarWidgetTrasLogin(firebaseUser.uid)

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    autenticado = true,
                    rolUsuario = usuarioActualizado.rol
                )

                return@launch
            }

            val usuarioBase = UsuarioEntity(
                idUsuario = firebaseUser.uid,
                firebaseUid = firebaseUser.uid,
                nombre = firebaseUser.displayName ?: "Usuario",
                correo = firebaseUser.email ?: "",
                avatar = firebaseUser.photoUrl?.toString(),
                rol = RolUsuario.USUARIO,
                activo = true,
                fechaRegistro = System.currentTimeMillis(),
                ultimoAcceso = System.currentTimeMillis()
            )

            val usuarioConRol = guardarUsuarioFirestoreYRoom(usuarioBase)

            sincronizarEstadisticasRemotasALocal(firebaseUser.uid)


            sincronizarWidgetTrasLogin(firebaseUser.uid)

            _uiState.value = _uiState.value.copy(
                cargando = false,
                autenticado = true,
                rolUsuario = usuarioConRol.rol
            )
        }
    }

    fun recuperarPassword() {
        val state = _uiState.value
        val correo = state.correo

        if (correo.isBlank()) {
            _uiState.value = state.copy(error = "Ingresa tu correo")
            return
        }

        if (!correo.matches(emailRegex)) {
            _uiState.value = state.copy(error = "El formato del correo no es válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                cargando = true,
                error = null,
                mensaje = null
            )

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

        viewModelScope.launch {
            WidgetPreferences.limpiarSesionWidget(context)
            WordWidgetProvider.updateAll(context)
        }
    }
}
