package com.jp.widgetenglish.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val usuariosCollection = firestore.collection("usuarios")

    suspend fun obtenerUsuarios(): List<AdminUsuarioDto> {
        val snapshot = usuariosCollection
            .whereEqualTo("rol", "USUARIO")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            AdminUsuarioDto(
                idUsuario = doc.id,
                nombre = doc.getString("nombre") ?: "Usuario",
                correo = doc.getString("correo") ?: "",
                activo = doc.getBoolean("activo") ?: true,
                palabrasAprendidas = doc.getLong("palabrasAprendidas")?.toInt() ?: 0,
                quizzesRealizados = doc.getLong("quizzesRealizados")?.toInt() ?: 0,
                lotesCompletados = doc.getLong("lotesCompletados")?.toInt() ?: 0,
                porcentajeProgreso = doc.getLong("porcentajeProgreso")?.toInt() ?: 0,
                rachaActual = doc.getLong("rachaActual")?.toInt() ?: 0,
                rachaMaxima = doc.getLong("rachaMaxima")?.toInt() ?: 0,
                ultimoAcceso = doc.getLong("ultimoAcceso") ?: 0L
            )
        }
    }
}

data class AdminUsuarioDto(
    val idUsuario: String,
    val nombre: String,
    val correo: String,
    val activo: Boolean,
    val palabrasAprendidas: Int,
    val quizzesRealizados: Int,
    val lotesCompletados: Int,
    val porcentajeProgreso: Int,
    val rachaActual: Int,
    val rachaMaxima: Int,
    val ultimoAcceso: Long
)