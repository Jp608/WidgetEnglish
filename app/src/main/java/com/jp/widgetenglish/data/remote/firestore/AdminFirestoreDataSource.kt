package com.jp.widgetenglish.data.remote.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AdminUsuarioDto(
    val id: String = "",
    val nombre: String = "Usuario",
    val correo: String = "",
    val activo: Boolean = false,
    val ultimoAcceso: Long = 0L,
    val palabrasAprendidas: Int = 0,
    val quizzesRealizados: Int = 0,
    val lotesCompletados: Int = 0,
    val rachaActual: Int = 0,
    val porcentajeProgreso: Int = 0
)

class AdminFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun obtenerUsuarios(): List<AdminUsuarioDto> {
        val snapshot = firestore
            .collection("usuarios")
            .get()
            .await()

        return snapshot.documents.map { document ->
            val data = document.data.orEmpty()

            AdminUsuarioDto(
                id = document.id,
                nombre = obtenerTexto(
                    data = data,
                    claves = listOf("nombre", "nombres", "displayName", "name"),
                    valorPorDefecto = "Usuario"
                ),
                correo = obtenerTexto(
                    data = data,
                    claves = listOf("correo", "email", "correoElectronico"),
                    valorPorDefecto = ""
                ),
                activo = obtenerBooleano(
                    data = data,
                    claves = listOf("activo", "isActive", "habilitado"),
                    valorPorDefecto = false
                ),
                ultimoAcceso = obtenerFechaMillis(
                    data = data,
                    claves = listOf("ultimoAcceso", "lastLogin", "lastAccess", "fechaUltimoAcceso")
                ),
                palabrasAprendidas = obtenerEntero(
                    data = data,
                    claves = listOf(
                        "palabrasAprendidas",
                        "totalPalabrasAprendidas",
                        "wordsLearned",
                        "learnedWords"
                    )
                ),
                quizzesRealizados = obtenerEntero(
                    data = data,
                    claves = listOf(
                        "quizzesRealizados",
                        "totalQuizzesRealizados",
                        "quizCompletados",
                        "completedQuizzes"
                    )
                ),
                lotesCompletados = obtenerEntero(
                    data = data,
                    claves = listOf(
                        "lotesCompletados",
                        "totalLotesCompletados",
                        "completedLots",
                        "completedBatches"
                    )
                ),
                rachaActual = obtenerEntero(
                    data = data,
                    claves = listOf("rachaActual", "currentStreak", "streak")
                ),
                porcentajeProgreso = obtenerEntero(
                    data = data,
                    claves = listOf(
                        "porcentajeProgreso",
                        "progresoPorcentaje",
                        "progressPercentage",
                        "cumplimiento"
                    )
                ).coerceIn(0, 100)
            )
        }
    }

    private fun obtenerTexto(
        data: Map<String, Any>,
        claves: List<String>,
        valorPorDefecto: String
    ): String {
        return claves.firstNotNullOfOrNull { clave ->
            data[clave] as? String
        }?.takeIf { it.isNotBlank() } ?: valorPorDefecto
    }

    private fun obtenerBooleano(
        data: Map<String, Any>,
        claves: List<String>,
        valorPorDefecto: Boolean
    ): Boolean {
        return claves.firstNotNullOfOrNull { clave ->
            when (val valor = data[clave]) {
                is Boolean -> valor
                is String -> valor.equals("true", ignoreCase = true) ||
                        valor.equals("activo", ignoreCase = true)
                else -> null
            }
        } ?: valorPorDefecto
    }

    private fun obtenerEntero(
        data: Map<String, Any>,
        claves: List<String>
    ): Int {
        return claves.firstNotNullOfOrNull { clave ->
            when (val valor = data[clave]) {
                is Long -> valor.toInt()
                is Int -> valor
                is Double -> valor.toInt()
                is Float -> valor.toInt()
                is String -> valor.toIntOrNull()
                else -> null
            }
        } ?: 0
    }

    private fun obtenerFechaMillis(
        data: Map<String, Any>,
        claves: List<String>
    ): Long {
        return claves.firstNotNullOfOrNull { clave ->
            when (val valor = data[clave]) {
                is Timestamp -> valor.toDate().time
                is Long -> valor
                is Int -> valor.toLong()
                is Double -> valor.toLong()
                is String -> valor.toLongOrNull()
                else -> null
            }
        } ?: 0L
    }
}