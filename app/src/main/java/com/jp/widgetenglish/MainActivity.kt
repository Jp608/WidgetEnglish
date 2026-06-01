package com.jp.widgetenglish

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.seed.DatabaseSeeder
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.jp.widgetenglish.features.widget.WordWidgetProvider
import com.jp.widgetenglish.ui.theme.WidgetEnglishTheme
import com.widgetenglish.app.ui.AppNavGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sincronizarWidgetAlIniciarApp()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        setContent {
            WidgetEnglishTheme {
                AppNavGraph()
            }
        }
    }

    private fun sincronizarWidgetAlIniciarApp() {
        lifecycleScope.launch {
            try {
                val db = DatabaseProvider.getDatabase(applicationContext)

                DatabaseSeeder.seed(db)

                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId.isNullOrBlank()) {
                    Log.d(TAG, "No hay usuario autenticado. No se sincroniza widget.")
                    return@launch
                }

                val repository = VocabularioRepositoryImpl(
                    palabraDao = db.palabraDao(),
                    verboDao = db.verboDao(),
                    loteDao = db.loteDao(),
                    progresoDao = db.progresoDao(),
                    usuarioFirestoreDataSource = UsuarioFirestoreDataSource(
                        FirebaseFirestore.getInstance()
                    )
                )

                repository.sincronizarLoteActivoConFirestore(userId)

                val loteActivo = repository.observarLoteActivo(userId).first()

                if (loteActivo == null) {
                    Log.d(TAG, "El usuario no tiene lote activo.")
                    WidgetPreferences.guardarUserId(applicationContext, userId)
                    WordWidgetProvider.updateAll(applicationContext)
                    return@launch
                }

                val loteInfo = repository.obtenerLotePorId(loteActivo.loteId)

                if (loteInfo == null) {
                    Log.d(TAG, "No se encontró información del lote activo.")
                    WidgetPreferences.guardarUserId(applicationContext, userId)
                    WordWidgetProvider.updateAll(applicationContext)
                    return@launch
                }

                WidgetPreferences.guardarUserId(applicationContext, userId)
                WidgetPreferences.guardarLoteActivo(
                    context = applicationContext,
                    loteId = loteInfo.idLote,
                    loteNombre = loteInfo.nombre
                )

                WordWidgetProvider.updateAll(applicationContext)

                Log.d(TAG, "Widget sincronizado correctamente al iniciar la app.")

            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando widget al iniciar la app", e)
            }
        }
    }
}