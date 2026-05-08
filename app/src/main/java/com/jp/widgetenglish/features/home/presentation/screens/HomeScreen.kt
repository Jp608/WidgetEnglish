package com.jp.widgetenglish.features.home.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp.widgetenglish.features.home.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "WidgetEnglish")

        Spacer(modifier = Modifier.height(16.dp))

        if (state.cargando) {
            Text(text = "Cargando datos...")
        } else {
            Text(text = "Palabras cargadas: ${state.palabras.size}")
            Text(text = "Verbos cargados: ${state.verbos.size}")
            Text(text = "Lotes cargados: ${state.lotes.size}")

            Spacer(modifier = Modifier.height(16.dp))

            val loteActivo = state.loteActivo
            if (loteActivo != null) {
                Text(text = "Lote activo: ${loteActivo.loteId}")
                Text(text = "Progreso: ${loteActivo.progresoPorcentaje}%")
            } else {
                Text(text = "No hay lote activo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Primeras palabras:")
            state.palabras.take(5).forEach { palabra ->
                Text(text = "- ${palabra.termino} = ${palabra.traduccion}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Primeros verbos:")
            state.verbos.take(5).forEach { verbo ->
                Text(text = "- ${verbo.formaBase} / ${verbo.pasadoSimple} / ${verbo.participioPasado}")
            }
        }
    }
}