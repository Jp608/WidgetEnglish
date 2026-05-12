package com.jp.widgetenglish.data.repository.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioDao: UsuarioDao
) : AuthRepository {

    // --- MODO DE PRUEBA LOCAL (Simulación sin Firebase) ---

    override suspend fun registrarConCorreo(
        nombre: String,
        correo: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            // Simulamos un ID único como si viniera de Firebase
            val mockUid = UUID.randomUUID().toString()

            val nuevoUsuario = UsuarioEntity(
                idUsuario = mockUid,
                firebaseUid = mockUid,
                nombre = nombre,
                correo = correo
            )
            usuarioDao.insertarUsuario(nuevoUsuario)

            // Retornamos éxito manual (FirebaseUser será null en mock, pero el ViewModel lo manejará)
            // Para fines de prueba, lanzamos una excepción controlada o devolvemos éxito vacío
            // Como Result necesita un FirebaseUser, y no podemos crear uno manualmente fácil,
            // vamos a simular el éxito devolviendo una excepción de "Modo Offline" o ajustando el ViewModel.
            Result.success(null as FirebaseUser) 
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesionConCorreo(
        correo: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            // Buscamos si el usuario existe en Room (local)
            // Como no tenemos búsqueda por correo en el DAO actual, simulamos éxito si hay datos
            Result.success(null as FirebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesionConGoogle(credential: AuthCredential): Result<FirebaseUser> {
        return Result.failure(Exception("Google no disponible en modo offline"))
    }

    override suspend fun recuperarPassword(correo: String): Result<Unit> = Result.success(Unit)
    override fun obtenerUsuarioActual(): FirebaseUser? = null
    override fun cerrarSesion() {}
}
