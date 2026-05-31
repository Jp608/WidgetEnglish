package com.jp.widgetenglish.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.ChatDao
import com.jp.widgetenglish.data.local.dao.LoteDao
import com.jp.widgetenglish.data.local.dao.PalabraDao
import com.jp.widgetenglish.data.local.dao.ProgresoDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.dao.VerboDao
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import com.jp.widgetenglish.data.local.entity.ChatMessageEntity
import com.jp.widgetenglish.data.local.entity.ChatSessionEntity
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.local.entity.VerboEntity

@Database(
    entities = [
        UsuarioEntity::class,
        PalabraEntity::class,
        VerboEntity::class,
        LoteEntity::class,
        LoteContenidoEntity::class,
        ProgresoUsuarioEntity::class,
        ProgresoLoteEntity::class,
        ActividadDiariaEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun palabraDao(): PalabraDao

    abstract fun verboDao(): VerboDao

    abstract fun loteDao(): LoteDao

    abstract fun progresoDao(): ProgresoDao

    abstract fun actividadDiariaDao(): ActividadDiariaDao

    abstract fun chatDao(): ChatDao
}
