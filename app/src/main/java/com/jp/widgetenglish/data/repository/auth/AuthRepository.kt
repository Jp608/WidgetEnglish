package com.jp.widgetenglish.data.repository.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun registrarConCorreo(
        nombre: String,
        correo: String,
        password: String
    ): Result<FirebaseUser>

    suspend fun iniciarSesionConCorreo(
        correo: String,
        password: String
    ): Result<FirebaseUser>

    suspend fun iniciarSesionConGoogle(
        credential: AuthCredential
    ): Result<FirebaseUser>

    suspend fun recuperarPassword(
        correo: String
    ): Result<Unit>

    suspend fun actualizarNombreUsuarioActual(
        nombre: String
    ): Result<Unit>

    fun obtenerUsuarioActual(): FirebaseUser?

    fun requiereInicioSesionReciente(
        maxAgeMillis: Long = 4 * 60 * 1000L
    ): Boolean

    suspend fun eliminarCuentaActual(): Result<Unit>

    fun cerrarSesion()
}
