package com.jp.widgetenglish.data.local.seed

import com.jp.widgetenglish.data.local.database.AppDatabase
import android.util.Log

object DatabaseSeeder {
    suspend fun seed(database: AppDatabase) {
        try {
            Log.d("DatabaseSeeder", "DATABASE_SEED_START")
            
            val palabraDao = database.palabraDao()
            val verboDao = database.verboDao()
            val loteDao = database.loteDao()

            // 1. Sincronizar nombres y metadatos de lotes sin borrar progreso (Evita CASCADE DELETE)
            SeedLotes.lotes.forEach { lote ->
                val existe = loteDao.obtenerLotePorId(lote.idLote)
                if (existe == null) {
                    Log.d("DatabaseSeeder", "Inserting new batch: ${lote.nombre}")
                    loteDao.insertarLote(lote)
                } else {
                    Log.d("DatabaseSeeder", "Updating metadata for batch: ${lote.nombre} (count: ${lote.cantidadContenido})")
                    loteDao.actualizarMetadatos(
                        lote.idLote, 
                        lote.nombre, 
                        lote.descripcion, 
                        lote.nivel, 
                        lote.tipoLote,
                        lote.cantidadContenido
                    )
                }
            }

            // 2. Cargar palabras y verbos maestros (Sincronización total)
            Log.d("DatabaseSeeder", "Syncing master words and verbs...")
            palabraDao.insertarPalabras(SeedPalabras.palabras)
            verboDao.insertarVerbos(SeedVerbos.verbos)

            // 3. Sincronizar relaciones de contenido (junction table)
            // Se usa REPLACE aquí porque no tiene hijos en cascada
            loteDao.eliminarContenidoDeLote("lote_A1") // Limpiar para asegurar sincronización completa de verbos
            loteDao.insertarContenidosLote(SeedLoteContenido.contenidos)
            
            Log.d("DatabaseSeeder", "Database Sync Finished.")
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "CRITICAL ERROR in Seeder", e)
        }
    }
}
