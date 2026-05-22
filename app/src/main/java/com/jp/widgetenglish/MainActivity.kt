package com.jp.widgetenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jp.widgetenglish.ui.theme.WidgetEnglishTheme
import android.util.Log
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.seed.DatabaseSeeder
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.widgetenglish.app.ui.AppNavGraph
import android.graphics.Color
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import com.google.firebase.auth.FirebaseAuth
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.features.widget.WordWidget
import kotlinx.coroutines.flow.first
import androidx.glance.appwidget.updateAll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sincronizar estado del widget al iniciar la app
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(applicationContext)
            DatabaseSeeder.seed(db)
            
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val repo = VocabularioRepositoryImpl(
                    db.palabraDao(), db.verboDao(), db.loteDao(), db.progresoDao(),
                    usuarioFirestoreDataSource = com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource(com.google.firebase.firestore.FirebaseFirestore.getInstance())
                )
                
                // Sincronizar lote activo desde Firestore (Fix Persistencia Total)
                repo.sincronizarLoteActivoConFirestore(userId)

                val loteActivo = repo.observarLoteActivo(userId).first()
                if (loteActivo != null) {
                    val info = repo.obtenerLotePorId(loteActivo.loteId)
                    if (info != null) {
                        WidgetPreferences.guardarLoteActivo(applicationContext, info.idLote, info.nombre)
                        WidgetPreferences.guardarUserId(applicationContext, userId)
                        
                        // Notificar al widget inmediatamente
                        try {
                            WordWidget().updateAll(applicationContext)
                        } catch (e: Exception) {}
                    }
                }
            }
        }

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
                val database = DatabaseProvider.getDatabase(applicationContext)

                val repository = VocabularioRepositoryImpl(
                    palabraDao = database.palabraDao(),
                    verboDao = database.verboDao(),
                    loteDao = database.loteDao(),
                    progresoDao = database.progresoDao()
                )

                AppNavGraph()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WidgetEnglishTheme {
        Greeting("Android")
    }
}