package com.jp.widgetenglish.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.remote.firestore.EstadisticasFirestoreDataSource
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.auth.presentation.state.AuthUiState
import com.jp.widgetenglish.features.common.resolveUserDisplayName
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
    private var autenticacionPendienteTerminos: AutenticacionPendienteTerminos? = null

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
            sincronizarLotesCompletadosRemotosALocal(
                firebaseUid = firebaseUid,
                lotesCompletadosIds = statsRemotas.lotesCompletadosIds
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun sincronizarLotesCompletadosRemotosALocal(
        firebaseUid: String,
        lotesCompletadosIds: List<String>
    ) {
        val idsUnicos = lotesCompletadosIds
            .filter { loteId -> loteId.isNotBlank() }
            .distinct()

        if (idsUnicos.isEmpty()) return

        val ahora = System.currentTimeMillis()

        idsUnicos.forEach { loteId ->
            val lote = vocabularioRepository.obtenerLotePorId(loteId)
                ?: return@forEach

            val contenidos = vocabularioRepository
                .observarContenidoDeLote(loteId)
                .first()

            contenidos.forEach { contenido ->
                val progresoContenido = vocabularioRepository.obtenerProgresoContenido(
                    usuarioId = firebaseUid,
                    contenidoId = contenido.contenidoId,
                    tipoContenido = contenido.tipoContenido
                )

                val progresoFinal = if (progresoContenido == null) {
                    ProgresoUsuarioEntity(
                        id = "pu_${firebaseUid}_${contenido.contenidoId}_${contenido.tipoContenido.name}",
                        usuarioId = firebaseUid,
                        contenidoId = contenido.contenidoId,
                        tipoContenido = contenido.tipoContenido,
                        estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                        nivelDominio = 1f,
                        aprendido = true,
                        ultimaRevision = ahora
                    )
                } else {
                    progresoContenido.copy(
                        estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                        nivelDominio = 1f,
                        aprendido = true,
                        ultimaRevision = progresoContenido.ultimaRevision ?: ahora
                    )
                }

                vocabularioRepository.guardarProgresoUsuario(progresoFinal)
            }

            val total = contenidos
                .size
                .takeIf { cantidad -> cantidad > 0 }
                ?: lote.cantidadContenido

            val existente = vocabularioRepository.obtenerProgresoLote(
                usuarioId = firebaseUid,
                loteId = loteId
            )

            val progresoFinal = if (existente == null) {
                ProgresoLoteEntity(
                    id = "pl_${firebaseUid}_$loteId",
                    usuarioId = firebaseUid,
                    loteId = loteId,
                    completado = true,
                    progresoPorcentaje = 100f,
                    contenidosAprendidos = total,
                    totalContenidos = total,
                    fechaInicio = ahora,
                    fechaUltimoEstudio = ahora,
                    fechaCompletado = ahora
                )
            } else {
                existente.copy(
                    completado = true,
                    progresoPorcentaje = 100f,
                    contenidosAprendidos = maxOf(existente.contenidosAprendidos, total),
                    totalContenidos = maxOf(existente.totalContenidos, total),
                    fechaUltimoEstudio = ahora,
                    fechaCompletado = existente.fechaCompletado ?: ahora
                )
            }

            vocabularioRepository.guardarProgresoLote(progresoFinal)
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
        usuarioBase: UsuarioEntity,
        aceptarTerminos: Boolean = false
    ): UsuarioEntity {
        val usuarioConRol = usuarioFirestoreDataSource.crearUsuarioSiNoExiste(
            usuario = usuarioBase,
            aceptarTerminos = aceptarTerminos
        )

        return guardarUsuarioRoomPreservandoEstadisticas(usuarioConRol)
    }

    private suspend fun continuarAutenticacion(
        firebaseUid: String,
        usuarioConRol: UsuarioEntity,
        mensaje: String?
    ) {
        val requiereAceptarTerminos = usuarioConRol.rol != RolUsuario.ADMIN &&
                !usuarioFirestoreDataSource.usuarioAceptoTerminos(firebaseUid)

        if (requiereAceptarTerminos) {
            autenticacionPendienteTerminos = AutenticacionPendienteTerminos(
                firebaseUid = firebaseUid,
                rolUsuario = usuarioConRol.rol,
                mensaje = mensaje
            )

            _uiState.value = _uiState.value.copy(
                cargando = false,
                autenticado = false,
                rolUsuario = usuarioConRol.rol,
                mostrarTerminos = true,
                aceptandoTerminos = false,
                mensaje = null,
                error = null
            )
            return
        }

        completarAutenticacion(
            firebaseUid = firebaseUid,
            rolUsuario = usuarioConRol.rol,
            mensaje = mensaje
        )
    }

    private suspend fun completarAutenticacion(
        firebaseUid: String,
        rolUsuario: RolUsuario,
        mensaje: String?
    ) {
        sincronizarEstadisticasRemotasALocal(firebaseUid)
        sincronizarActividadDiariaRemotaALocal(firebaseUid)
        sincronizarWidgetTrasLogin(firebaseUid)

        autenticacionPendienteTerminos = null

        _uiState.value = _uiState.value.copy(
            cargando = false,
            autenticado = true,
            rolUsuario = rolUsuario,
            mostrarTerminos = false,
            aceptandoTerminos = false,
            mensaje = mensaje,
            error = null
        )
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

    fun prepararRegistroConTerminos(): Boolean {
        val state = _uiState.value
        val errorRegistro = validarDatosRegistro(state)

        if (errorRegistro != null) {
            _uiState.value = state.copy(error = errorRegistro)
            return false
        }

        _uiState.value = state.copy(error = null)
        return true
    }

    private fun validarDatosRegistro(state: AuthUiState): String? {
        return when {
            state.nombre.isBlank() || state.correo.isBlank() || state.password.isBlank() ->
                "Completa todos los campos"

            !state.correo.matches(emailRegex) ->
                "El formato del correo no es válido"

            state.password.length < 6 ->
                "La contraseña debe tener mínimo 6 caracteres"

            state.password != state.confirmPassword ->
                "Las contraseñas no coinciden"

            else -> null
        }
    }

    fun registrar(aceptaTerminos: Boolean = false) {
        val state = _uiState.value

        val errorRegistro = validarDatosRegistro(state)
        if (errorRegistro != null) {
            _uiState.value = state.copy(error = errorRegistro)
            return
        }

        if (!aceptaTerminos) {
            _uiState.value = state.copy(error = "Debes aceptar los términos y condiciones")
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

                val usuarioConRol = guardarUsuarioFirestoreYRoom(
                    usuarioBase = usuario,
                    aceptarTerminos = true
                )

                continuarAutenticacion(
                    firebaseUid = firebaseUser.uid,
                    usuarioConRol = usuarioConRol,
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
                val correo = firebaseUser.email ?: state.correo
                val usuarioBase = UsuarioEntity(
                    idUsuario = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    nombre = resolveUserDisplayName(
                        firebaseDisplayName = firebaseUser.displayName,
                        email = correo
                    ),
                    correo = correo,
                    avatar = firebaseUser.photoUrl?.toString(),
                    rol = RolUsuario.USUARIO,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis(),
                    ultimoAcceso = System.currentTimeMillis()
                )

                val usuarioConRol = guardarUsuarioFirestoreYRoom(usuarioBase)

                continuarAutenticacion(
                    firebaseUid = firebaseUser.uid,
                    usuarioConRol = usuarioConRol,
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
                val correo = firebaseUser.email ?: ""
                val usuarioBase = UsuarioEntity(
                    idUsuario = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    nombre = resolveUserDisplayName(
                        firebaseDisplayName = firebaseUser.displayName,
                        email = correo
                    ),
                    correo = correo,
                    avatar = firebaseUser.photoUrl?.toString(),
                    rol = RolUsuario.USUARIO,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis(),
                    ultimoAcceso = System.currentTimeMillis()
                )

                val usuarioConRol = guardarUsuarioFirestoreYRoom(usuarioBase)

                continuarAutenticacion(
                    firebaseUid = firebaseUser.uid,
                    usuarioConRol = usuarioConRol,
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

                usuarioFirestoreDataSource.actualizarUltimoAcceso(firebaseUser.uid)

                continuarAutenticacion(
                    firebaseUid = firebaseUser.uid,
                    usuarioConRol = usuarioActualizado,
                    mensaje = null
                )

                return@launch
            }

            val correo = firebaseUser.email ?: ""
            val usuarioBase = UsuarioEntity(
                idUsuario = firebaseUser.uid,
                firebaseUid = firebaseUser.uid,
                nombre = resolveUserDisplayName(
                    firebaseDisplayName = firebaseUser.displayName,
                    email = correo
                ),
                correo = correo,
                avatar = firebaseUser.photoUrl?.toString(),
                rol = RolUsuario.USUARIO,
                activo = true,
                fechaRegistro = System.currentTimeMillis(),
                ultimoAcceso = System.currentTimeMillis()
            )

            val usuarioConRol = guardarUsuarioFirestoreYRoom(usuarioBase)

            continuarAutenticacion(
                firebaseUid = firebaseUser.uid,
                usuarioConRol = usuarioConRol,
                mensaje = null
            )
        }
    }

    fun aceptarTerminosPendientes() {
        val pendiente = autenticacionPendienteTerminos ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                aceptandoTerminos = true,
                error = null
            )

            try {
                usuarioFirestoreDataSource.registrarAceptacionTerminos(
                    firebaseUid = pendiente.firebaseUid
                )

                completarAutenticacion(
                    firebaseUid = pendiente.firebaseUid,
                    rolUsuario = pendiente.rolUsuario,
                    mensaje = pendiente.mensaje
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    aceptandoTerminos = false,
                    error = e.message ?: "No se pudo guardar la aceptación de términos"
                )
            }
        }
    }

    fun cancelarTerminosPendientes() {
        autenticacionPendienteTerminos = null
        authRepository.cerrarSesion()

        _uiState.value = AuthUiState(
            error = "Debes aceptar los términos y condiciones para usar WidgetEnglish"
        )

        viewModelScope.launch {
            WidgetPreferences.limpiarSesionWidget(context)
            WordWidgetProvider.updateAll(context)
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

private data class AutenticacionPendienteTerminos(
    val firebaseUid: String,
    val rolUsuario: RolUsuario,
    val mensaje: String?
)
