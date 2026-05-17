package com.jp.widgetenglish.data.local.seed

import com.jp.widgetenglish.data.local.database.AppDatabase
import android.util.Log

object DatabaseSeeder {
    suspend fun seed(database: AppDatabase) {
        try {
            // Log para debug
            Log.d("DatabaseSeeder", "DATABASE_SEED_CHECK_START")
            
            val palabraDao = database.palabraDao()
            val verboDao = database.verboDao()
            val loteDao = database.loteDao()

            // Verificar si hay palabras existentes
            val gato = palabraDao.obtenerPalabraPorId("palabra_cat")
            
            if (gato == null) {
                Log.d("DatabaseSeeder", "DB is empty. Force seeding all content...")
                
                // Limpiar por si acaso quedaron restos corruptos
                palabraDao.eliminarPalabras()
                verboDao.eliminarVerbos()
                loteDao.eliminarLotes()

                // Insertar datos maestros
                palabraDao.insertarPalabras(SeedPalabras.palabras)
                verboDao.insertarVerbos(SeedVerbos.verbos)
                loteDao.insertarLotes(SeedLotes.lotes)
                loteDao.insertarContenidosLote(SeedLoteContenido.contenidos)
                
                Log.d("DatabaseSeeder", "Force seed finished successfully.")
            } else {
                Log.d("DatabaseSeeder", "Data found. Skipping seed to protect user progress.")
            }
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "CRITICAL ERROR in Seeder", e)
        }
    }
}
