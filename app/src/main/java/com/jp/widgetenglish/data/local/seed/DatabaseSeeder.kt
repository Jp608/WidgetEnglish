package com.jp.widgetenglish.data.local.seed

import android.util.Log
import com.jp.widgetenglish.data.local.database.AppDatabase

object DatabaseSeeder {

    suspend fun seed(database: AppDatabase) {
        try {
            Log.d("DatabaseSeeder", "DATABASE_SEED_START")

            val palabraDao = database.palabraDao()
            val verboDao = database.verboDao()
            val loteDao = database.loteDao()

            // 1. Sincronizar nombres y metadatos de lotes sin borrar progreso
            // Importante: NO eliminamos lotes, porque eso podría afectar datos relacionados.
            SeedLotes.lotes.forEach { lote ->
                val existe = loteDao.obtenerLotePorId(lote.idLote)

                if (existe == null) {
                    Log.d("DatabaseSeeder", "Inserting new batch: ${lote.nombre}")

                    loteDao.insertarLote(lote)
                } else {
                    Log.d(
                        "DatabaseSeeder",
                        "Updating metadata for batch: ${lote.nombre} (count: ${lote.cantidadContenido})"
                    )

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

            // 2. Cargar palabras y verbos maestros
            // Si los DAO usan OnConflictStrategy.REPLACE o IGNORE, esto mantiene sincronizada la data base.
            Log.d("DatabaseSeeder", "Syncing master words and verbs...")

            palabraDao.insertarPalabras(SeedPalabras.palabras)
            verboDao.insertarVerbos(SeedVerbos.verbos)

            // 3. Sincronizar relaciones lote-contenido
            // Antes solo se limpiaba lote_A1.
            // Ahora limpiamos todos los lotes definidos en SeedLotes para poder cargar comida, tecnología, etc.
            Log.d("DatabaseSeeder", "Syncing lote-contenido relations...")

            SeedLotes.lotes.forEach { lote ->
                Log.d("DatabaseSeeder", "Clearing content relations for lote: ${lote.idLote}")
                loteDao.eliminarContenidoDeLote(lote.idLote)
            }

            loteDao.insertarContenidosLote(SeedLoteContenido.contenidos)

            Log.d("DatabaseSeeder", "Database Sync Finished.")
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "CRITICAL ERROR in Seeder", e)
        }
    }
}