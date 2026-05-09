package com.jp.widgetenglish.features.auth.presentation.state

data class AuthUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    val cargando: Boolean = false,
    val autenticado: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)