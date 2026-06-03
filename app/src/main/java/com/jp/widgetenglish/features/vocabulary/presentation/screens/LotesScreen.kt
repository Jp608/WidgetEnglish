package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModel

private val BackgroundColor = Color(0xFFF8FAFC)
private val HeaderBlue = Color(0xFF0B63F6)

@Composable
fun LotesScreen(
    viewModel: LotesViewModel,
    onBackClick: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onVocabularioClick: () -> Unit = {},
    onLotesClick: () -> Unit = {},
    onEstudioClick: () -> Unit = {},
    onIaClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onVerContenido: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var loteParaReiniciar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarLotes()
    }

    if (loteParaReiniciar != null) {
        AlertDialog(
            onDismissRequest = { loteParaReiniciar = null },
            title = { Text("¿Reiniciar progreso?") },
            text = { Text("Todas las palabras de este lote volverán a estar pendientes. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reiniciarProgreso(context, loteParaReiniciar!!)
                        loteParaReiniciar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { loteParaReiniciar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            LotesHeader(onBackClick = onBackClick)
        },
        bottomBar = {
            AppBottomBar(
                selectedRoute = "lotes",
                onInicioClick = onInicioClick,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Selecciona un lote para activarlo en tu widget o ver su contenido.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(state.lotes) { loteConProgreso ->
                        LoteItem(
                            loteConProgreso = loteConProgreso,
                            estaActivo = loteConProgreso.lote.idLote == state.idLoteActivo,
                            onActivar = { viewModel.activarLote(context, loteConProgreso) },
                            onVerContenido = { onVerContenido(loteConProgreso.lote.idLote) },
                            onReiniciar = { loteParaReiniciar = loteConProgreso.lote.idLote }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LotesHeader(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBlue)
            .statusBarsPadding()
            .height(72.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(29.dp)
            )
        }

        Text(
            text = "Lotes de Vocabulario",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
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
                    imageVector = Icons.Filled.Book,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    }
}
