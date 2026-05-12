package com.jp.widgetenglish.data.repository.auth

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

    suspend fun recuperarPassword(
        correo: String
    ): Result<Unit>

    fun obtenerUsuarioActual(): FirebaseUser?

    fun cerrarSesion()
}