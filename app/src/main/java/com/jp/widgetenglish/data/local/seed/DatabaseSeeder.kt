package com.jp.widgetenglish.data.local.seed

import com.jp.widgetenglish.data.local.database.AppDatabase

object DatabaseSeeder {
    suspend fun seed(database: AppDatabase) {
        database.palabraDao().insertarPalabras(SeedPalabras.palabras)
        database.verboDao().insertarVerbos(SeedVerbos.verbos)
        database.loteDao().insertarLotes(SeedLotes.lotes)
        database.loteDao().insertarContenidosLote(SeedLoteContenido.contenidos)
    }
}