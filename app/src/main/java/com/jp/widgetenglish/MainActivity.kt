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
import com.jp.widgetenglish.data.local.database.DatabaseSeeder
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.widgetenglish.app.ui.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        /*lifecycleScope.launch {
            val database = DatabaseProvider.getDatabase(applicationContext)

            DatabaseSeeder.seed(database)

            val usuario = database.usuarioDao()
                .obtenerUsuarioPorId("usuario_prueba")

            val palabra = database.palabraDao()
                .obtenerPalabraPorId("palabra_dog")

            val verbo = database.verboDao()
                .obtenerVerboPorId("verbo_go")

            val lote = database.loteDao()
                .obtenerLotePorId("lote_basico")

            Log.d("ROOM_TEST", "Usuario: $usuario")
            Log.d("ROOM_TEST", "Palabra: $palabra")
            Log.d("ROOM_TEST", "Verbo: $verbo")
            Log.d("ROOM_TEST", "Lote: $lote")
        }*/
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