package com.jp.widgetenglish.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.UserProfileChangeRequest
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun iniciarSesionConGoogle(
        credential: AuthCredential
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth
                .signInWithCredential(credential)
                .await()

            val user = result.user
                ?: return Result.failure(Exception("No se pudo iniciar sesión con Google"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registrarConCorreo(
        nombre: String,
        correo: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(correo, password)
                .await()

            val user = result.user
                ?: return Result.failure(Exception("No se pudo crear el usuario"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesionConCorreo(
        correo: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(correo, password)
                .await()

            val user = result.user
                ?: return Result.failure(Exception("No se pudo iniciar sesión"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recuperarPassword(
        correo: String
    ): Result<Unit> {
        return try {
            firebaseAuth
                .sendPasswordResetEmail(correo)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarNombreUsuarioActual(
        nombre: String
    ): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No hay usuario autenticado"))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(nombre)
                .build()

            user.updateProfile(profileUpdates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun obtenerUsuarioActual(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun requiereInicioSesionReciente(
        maxAgeMillis: Long
    ): Boolean {
        val lastSignIn = firebaseAuth.currentUser
            ?.metadata
            ?.lastSignInTimestamp
            ?: return true

        return System.currentTimeMillis() - lastSignIn > maxAgeMillis
    }

    override suspend fun eliminarCuentaActual(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No hay usuario autenticado"))

            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun cerrarSesion() {
        firebaseAuth.signOut()
    }
}
