package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.common.UserHeaderBlue
import com.jp.widgetenglish.features.common.UserHeaderSystemBars
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso

private val DetailBackground = Color(0xFFF8FAFC)
private val BluePrimary = Color(0xFF1E63D7)
private val TextMuted = Color(0xFF6B7280)
private val SoftBlue = Color(0xFFE3F2FD)

@Composable
fun LoteDetailScreen(
    loteId: String,
    viewModel: LotesViewModel,
    onBack: () -> Unit,
    onItemClick: (String, Boolean) -> Unit,
    onEstudiarClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    UserHeaderSystemBars()

    DisposableEffect(Unit) {
        onDispose { ttsHelper.shutdown() }
    }

    LaunchedEffect(loteId) {
        viewModel.cargarDetalleLote(loteId)
    }

    Scaffold(
        topBar = {
            LoteDetailHeader(
                title = state.loteSeleccionado?.nombre ?: "Detalle del lote",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DetailBackground)
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
                            OutlinedButton(onClick = { onEstudiarClick(lote.idLote) }) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.palabrasDelLote) { palabra ->
                    LoteContentCard(
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
private fun LoteDetailHeader(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(UserHeaderBlue)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    }
}

@Composable
private fun LoteContentCard(
    palabra: PalabraConProgreso,
    onPronounce: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(indicatorColorFor(palabra.estado))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = palabra.termino,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF1A237E),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = if (palabra.esVerbo) {
                                "• Verbo ${if (palabra.esIrregular) "irr." else "reg."}"
                            } else {
                                "• ${palabra.tipoPalabra.name.lowercase().take(4).replaceFirstChar { it.uppercase() }}."
                            },
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = palabra.traduccion,
                        color = TextMuted,
                        fontSize = 14.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (palabra.esVerbo) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "P: ${palabra.pasadoSimple.orEmpty()}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                maxLines = 1
                            )

                            Text(
                                text = "PP: ${palabra.participioPasado.orEmpty()}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    } else if (!palabra.fonetica.isNullOrBlank()) {
                        Text(
                            text = palabra.fonetica,
                            color = Color(0xFF039BE5),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    StatusChip(palabra.estado)

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        onClick = onPronounce,
                        shape = RoundedCornerShape(10.dp),
                        color = SoftBlue,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronunciar",
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun indicatorColorFor(estado: EstadoAprendizaje): Color {
    return when (estado) {
        EstadoAprendizaje.APRENDIDA -> Color(0xFF388E3C)
        EstadoAprendizaje.EN_PROGRESO -> Color(0xFF1565C0)
        EstadoAprendizaje.DIFICIL -> Color(0xFFD32F2F)
        EstadoAprendizaje.NO_VISTA -> Color(0xFFF57C00)
    }
}
