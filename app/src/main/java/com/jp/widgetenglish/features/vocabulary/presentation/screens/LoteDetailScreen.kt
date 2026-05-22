package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoteDetailScreen(
    loteId: String,
    viewModel: LotesViewModel,
    onBack: () -> Unit,
    onItemClick: (String, Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    DisposableEffect(Unit) {
        onDispose { ttsHelper.shutdown() }
    }

    LaunchedEffect(loteId) {
        viewModel.cargarDetalleLote(loteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.loteSeleccionado?.nombre ?: "Detalle del Lote") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            state.loteSeleccionado?.let { lote ->
                // Header del Lote (HU15)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, null, tint = Color(0xFF1565C0))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = lote.tipoLote.name,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Barra de progreso (HU15)
                        val idActivo = state.idLoteActivo == lote.idLote
                        val loteConProg = state.lotes.find { it.lote.idLote == lote.idLote }
                        val progreso = loteConProg?.progreso?.progresoPorcentaje ?: 0f
                        
                        Text("Progreso del lote: ${progreso.toInt()}%", fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { progreso / 100f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF1565C0)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { viewModel.activarLote(context, loteConProg!!) },
                                enabled = !idActivo
                            ) {
                                Text(if (idActivo) "Activo" else "Activar Lote")
                            }
                            OutlinedButton(onClick = { /* Modo Estudio */ }) {
                                Text("Estudiar")
                            }
                        }
                    }
                }
            }

            // Lista de palabras (HU15)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.palabrasDelLote) { palabra ->
                    PalabraItemMini(
                        palabra = palabra,
                        onPronounce = { ttsHelper.speak(palabra.termino) },
                        onClick = { onItemClick(palabra.id, palabra.esVerbo) }
                    )
                }
            }
        }
    }
}

@Composable
fun PalabraItemMini(
    palabra: PalabraConProgreso,
    onPronounce: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPronounce) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color(0xFF1565C0))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(palabra.termino, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(palabra.traduccion, color = Color.Gray, fontSize = 14.sp)
            }
            
            StatusChipMini(palabra.estado)
        }
    }
}

@Composable
fun StatusChipMini(estado: EstadoAprendizaje) {
    val color = when (estado) {
        EstadoAprendizaje.APRENDIDA -> Color(0xFF2E7D32)
        EstadoAprendizaje.EN_PROGRESO -> Color(0xFF1565C0)
        EstadoAprendizaje.DIFICIL -> Color(0xFFC62828)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = estado.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
