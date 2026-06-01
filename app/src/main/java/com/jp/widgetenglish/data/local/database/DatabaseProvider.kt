package com.jp.widgetenglish.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "widget_english_master_v1.db"
            )
                .fallbackToDestructiveMigration()
                .setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE)
                .enableMultiInstanceInvalidation()
                .build()

            INSTANCE = instance
            instance
        }
    }
}
