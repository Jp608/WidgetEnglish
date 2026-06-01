package com.jp.widgetenglish.features.auth.presentation.state

import com.jp.widgetenglish.data.local.entity.RolUsuario

data class AuthUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    val cargando: Boolean = false,
    val autenticado: Boolean = false,
    val rolUsuario: RolUsuario = RolUsuario.USUARIO,
    val mostrarTerminos: Boolean = false,
    val aceptandoTerminos: Boolean = false,

    val mensaje: String? = null,
    val error: String? = null
)
