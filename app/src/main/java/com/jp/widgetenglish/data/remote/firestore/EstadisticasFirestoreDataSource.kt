package com.jp.widgetenglish.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import kotlinx.coroutines.tasks.await

class EstadisticasFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {

    private val usuariosCollection = firestore.collection("usuarios")

    suspend fun sincronizarEstadisticasUsuario(
        firebaseUid: String,
        rachaActual: Int,
        rachaMaxima: Int,
        ultimaFechaRacha: String?,
        fechaUltimaActividad: String?,
        palabrasAprendidas: Int,
        quizzesRealizados: Int,
        lotesCompletados: Int,
        porcentajeProgreso: Int
    ) {
        if (firebaseUid.isBlank()) return

        val data = mapOf(
            "rachaActual" to rachaActual,
            "rachaMaxima" to rachaMaxima,
            "ultimaFechaRacha" to ultimaFechaRacha,
            "fechaUltimaActividad" to fechaUltimaActividad,
            "palabrasAprendidas" to palabrasAprendidas,
            "quizzesRealizados" to quizzesRealizados,
            "lotesCompletados" to lotesCompletados,
            "porcentajeProgreso" to porcentajeProgreso,
            "ultimaSincronizacionEstadisticas" to System.currentTimeMillis()
        )

        usuariosCollection
            .document(firebaseUid)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun sincronizarActividadDiaria(
        firebaseUid: String,
        actividad: ActividadDiariaEntity
    ) {
        if (firebaseUid.isBlank()) return

        val data = mapOf(
            "usuarioId" to actividad.usuarioId,
            "fecha" to actividad.fecha,
            "elementosEstudiados" to actividad.elementosEstudiados,
            "tarjetasEstudiadas" to actividad.tarjetasEstudiadas,
            "preguntasQuizRespondidas" to actividad.preguntasQuizRespondidas,
            "quizzesCompletados" to actividad.quizzesCompletados,
            "objetivoDiario" to actividad.objetivoDiario,
            "objetivoCumplido" to actividad.objetivoCumplido,
            "fechaCumplimiento" to actividad.fechaCumplimiento,
            "ultimaActualizacion" to actividad.ultimaActualizacion,
            "ultimaSincronizacion" to System.currentTimeMillis()
        )

        usuariosCollection
            .document(firebaseUid)
            .collection("actividadDiaria")
            .document(actividad.fecha)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun obtenerActividadDiaria(
        firebaseUid: String,
        fecha: String
    ): ActividadDiariaEntity? {
        if (firebaseUid.isBlank() || fecha.isBlank()) return null

        val snapshot = usuariosCollection
            .document(firebaseUid)
            .collection("actividadDiaria")
            .document(fecha)
            .get()
            .await()

        if (!snapshot.exists()) return null

        return ActividadDiariaEntity(
            usuarioId = snapshot.getString("usuarioId") ?: firebaseUid,
            fecha = snapshot.getString("fecha") ?: fecha,
            elementosEstudiados = snapshot.getLong("elementosEstudiados")?.toInt() ?: 0,
            tarjetasEstudiadas = snapshot.getLong("tarjetasEstudiadas")?.toInt() ?: 0,
            preguntasQuizRespondidas = snapshot.getLong("preguntasQuizRespondidas")?.toInt() ?: 0,
            quizzesCompletados = snapshot.getLong("quizzesCompletados")?.toInt() ?: 0,
            objetivoDiario = snapshot.getLong("objetivoDiario")?.toInt() ?: 10,
            objetivoCumplido = snapshot.getBoolean("objetivoCumplido") ?: false,
            fechaCumplimiento = snapshot.getLong("fechaCumplimiento"),
            ultimaActualizacion = snapshot.getLong("ultimaActualizacion") ?: System.currentTimeMillis()
        )
    }

    suspend fun obtenerEstadisticasUsuario(
        firebaseUid: String
    ): FirebaseUserStats? {
        if (firebaseUid.isBlank()) return null

        val snapshot = usuariosCollection
            .document(firebaseUid)
            .get()
            .await()

        if (!snapshot.exists()) return null

        return FirebaseUserStats(
            rachaActual = snapshot.getLong("rachaActual")?.toInt() ?: 0,
            rachaMaxima = snapshot.getLong("rachaMaxima")?.toInt() ?: 0,
            ultimaFechaRacha = snapshot.getString("ultimaFechaRacha"),
            fechaUltimaActividad = snapshot.getString("fechaUltimaActividad"),
            palabrasAprendidas = snapshot.getLong("palabrasAprendidas")?.toInt() ?: 0,
            quizzesRealizados = snapshot.getLong("quizzesRealizados")?.toInt() ?: 0,
            lotesCompletados = snapshot.getLong("lotesCompletados")?.toInt() ?: 0,
            porcentajeProgreso = snapshot.getLong("porcentajeProgreso")?.toInt() ?: 0
        )
    }
}

data class FirebaseUserStats(
    val rachaActual: Int = 0,
    val rachaMaxima: Int = 0,
    val ultimaFechaRacha: String? = null,
    val fechaUltimaActividad: String? = null,
    val palabrasAprendidas: Int = 0,
    val quizzesRealizados: Int = 0,
    val lotesCompletados: Int = 0,
    val porcentajeProgreso: Int = 0
)