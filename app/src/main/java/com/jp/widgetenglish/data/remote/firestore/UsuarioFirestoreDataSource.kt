package com.jp.widgetenglish.data.remote.firestore


import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import kotlinx.coroutines.tasks.await

class UsuarioFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val usuariosCollection = firestore.collection("usuarios")

    suspend fun crearUsuarioSiNoExiste(usuario: UsuarioEntity): UsuarioEntity {
        val documentRef = usuariosCollection.document(usuario.firebaseUid)
        val snapshot = documentRef.get().await()

        return if (snapshot.exists()) {
            val rolTexto = snapshot.getString("rol") ?: RolUsuario.USUARIO.name

            val camposFaltantes = mutableMapOf<String, Any>()

            if (!snapshot.contains("palabrasAprendidas")) {
                camposFaltantes["palabrasAprendidas"] = 0
            }

            if (!snapshot.contains("quizzesRealizados")) {
                camposFaltantes["quizzesRealizados"] = 0
            }

            if (!snapshot.contains("lotesCompletados")) {
                camposFaltantes["lotesCompletados"] = 0
            }

            if (!snapshot.contains("porcentajeProgreso")) {
                camposFaltantes["porcentajeProgreso"] = 0
            }

            camposFaltantes["ultimoAcceso"] = System.currentTimeMillis()

            if (camposFaltantes.isNotEmpty()) {
                documentRef.update(camposFaltantes).await()
            }

            usuario.copy(
                nombre = snapshot.getString("nombre") ?: usuario.nombre,
                correo = snapshot.getString("correo") ?: usuario.correo,
                avatar = snapshot.getString("avatar") ?: usuario.avatar,
                rol = runCatching { RolUsuario.valueOf(rolTexto) }
                    .getOrDefault(RolUsuario.USUARIO),
                activo = snapshot.getBoolean("activo") ?: true,
                ultimoAcceso = System.currentTimeMillis()
            )
        } else {
            val data = hashMapOf(
                "idUsuario" to usuario.idUsuario,
                "firebaseUid" to usuario.firebaseUid,
                "nombre" to usuario.nombre,
                "correo" to usuario.correo,
                "avatar" to usuario.avatar,
                "rol" to RolUsuario.USUARIO.name,
                "activo" to true,
                "fechaRegistro" to usuario.fechaRegistro,
                "ultimoAcceso" to System.currentTimeMillis(),
                "rachaActual" to usuario.rachaActual,
                "rachaMaxima" to usuario.rachaMaxima,

                // Estadísticas base para ranking/admin
                "palabrasAprendidas" to 0,
                "quizzesRealizados" to 0,
                "lotesCompletados" to 0,
                "porcentajeProgreso" to 0
            )

            documentRef.set(data).await()

            usuario.copy(
                rol = RolUsuario.USUARIO,
                activo = true,
                ultimoAcceso = System.currentTimeMillis()
            )
        }
    }

    suspend fun obtenerUsuario(firebaseUid: String): UsuarioEntity? {
        val snapshot = usuariosCollection.document(firebaseUid).get().await()

        if (!snapshot.exists()) return null

        val rolTexto = snapshot.getString("rol") ?: RolUsuario.USUARIO.name

        return UsuarioEntity(
            idUsuario = snapshot.getString("idUsuario") ?: firebaseUid,
            firebaseUid = snapshot.getString("firebaseUid") ?: firebaseUid,
            nombre = snapshot.getString("nombre") ?: "Usuario",
            correo = snapshot.getString("correo") ?: "",
            avatar = snapshot.getString("avatar"),
            rol = runCatching { RolUsuario.valueOf(rolTexto) }
                .getOrDefault(RolUsuario.USUARIO),
            activo = snapshot.getBoolean("activo") ?: true,
            fechaRegistro = snapshot.getLong("fechaRegistro") ?: System.currentTimeMillis(),
            ultimoAcceso = snapshot.getLong("ultimoAcceso"),
            rachaActual = snapshot.getLong("rachaActual")?.toInt() ?: 0,
            rachaMaxima = snapshot.getLong("rachaMaxima")?.toInt() ?: 0
        )
    }

    suspend fun actualizarPalabrasAprendidas(
        firebaseUid: String,
        cantidad: Int
    ) {
        usuariosCollection.document(firebaseUid)
            .update(
                mapOf(
                    "palabrasAprendidas" to cantidad,
                    "ultimoAcceso" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun actualizarUltimoAcceso(firebaseUid: String) {
        usuariosCollection.document(firebaseUid)
            .update("ultimoAcceso", System.currentTimeMillis())
            .await()
    }
}