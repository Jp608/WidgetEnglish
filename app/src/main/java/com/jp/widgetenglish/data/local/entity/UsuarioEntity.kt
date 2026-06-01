package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val idUsuario: String,

    val firebaseUid: String,
    val nombre: String,
    val correo: String,
    val avatar: String? = null,

    val rol: RolUsuario = RolUsuario.USUARIO,
    val activo: Boolean = true,

    val fechaRegistro: Long = System.currentTimeMillis(),
    val ultimoAcceso: Long? = null,

    val rachaActual: Int = 0,
    val rachaMaxima: Int = 0,

    val palabrasAprendidas: Int = 0,
    val quizzesRealizados: Int = 0,
    val lotesCompletados: Int = 0,
    val porcentajeProgreso: Int = 0,

    val ultimaFechaRacha: String? = null,
    val fechaUltimaActividad: String? = null

)