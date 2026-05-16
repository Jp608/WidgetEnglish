package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.BorderStroke
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularioFiltro
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedRoute = "vocabulario",
                onInicioClick = onBackClick,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(Color(0xFFF5F5F5))
        ) {
            // Header con Gradiente al estilo Home
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF1565C0),
                                Color(0xFF039BE5)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val titulo = if (uiState.filtroActual == VocabularioFiltro.APRENDIDAS) "Palabras Aprendidas" else "Vocabulario"
                    val subtitulo = if (uiState.filtroActual == VocabularioFiltro.APRENDIDAS) "¡Excelente! Sigue reforzando tu conocimiento." else "Aprende nuevas palabras cada día"

                    Text(
                        text = titulo,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = subtitulo,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Estadísticas estilo Home con navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Total",
                        value = uiState.totalPalabras.toString(),
                        color = Color(0xFF1A237E),
                        backgroundColor = Color(0xFFE8EAF6),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onFiltroChanged(VocabularioFiltro.TODAS) }
                    )
                    StatCard(
                        label = "Pendientes",
                        value = uiState.palabrasPendientes.toString(),
                        color = Color(0xFFE65100),
                        backgroundColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onFiltroChanged(VocabularioFiltro.PENDIENTES) }
                    )
                    StatCard(
                        label = "En progreso",
                        value = uiState.palabrasEnProgreso.toString(),
                        color = Color(0xFF1565C0),
                        backgroundColor = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onFiltroChanged(VocabularioFiltro.EN_PROGRESO) }
                    )
                    StatCard(
                        label = "Aprendidas",
                        value = uiState.palabrasAprendidas.toString(),
                        color = Color(0xFF2E7D32),
                        backgroundColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onFiltroChanged(VocabularioFiltro.APRENDIDAS) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buscador refinado
                OutlinedTextField(
                    value = uiState.textoBusqueda,
                    onValueChange = { viewModel.onSearchTextChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar palabra...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF1565C0)) },
                    trailingIcon = {
                        if (uiState.textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchTextChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF039BE5)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filtros con colores temáticos
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(VocabularioFiltro.entries.toTypedArray()) { filtro ->
                        val selected = uiState.filtroActual == filtro
                        val (chipColor, chipContentColor) = when (filtro) {
                            VocabularioFiltro.TODAS -> Color(0xFFE8EAF6) to Color(0xFF1A237E)
                            VocabularioFiltro.PENDIENTES -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                            VocabularioFiltro.EN_PROGRESO -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
                            VocabularioFiltro.APRENDIDAS -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onFiltroChanged(filtro) },
                            color = if (selected) chipContentColor else Color.White,
                            border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(
                                text = filtro.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (selected) Color.White else Color.Gray,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de palabras
                val palabrasFiltradas = uiState.palabrasFiltradas
                if (palabrasFiltradas.isEmpty()) {
                    EmptyState(uiState.filtroActual, uiState.textoBusqueda.isNotEmpty()) {
                        viewModel.onSearchTextChanged("")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(palabrasFiltradas) { palabra ->
                            WordCard(
                                palabra = palabra,
                                onMarkLearned = { viewModel.marcarComoAprendido(palabra.id) },
                                onRevert = { viewModel.mostrarConfirmacionRevertir(palabra) },
                                onSpeak = { ttsHelper.speak(palabra.termino) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }

        // Dialogo de confirmación para revertir mejorado
        uiState.mostrarDialogoRevertir?.let { palabra ->
            AlertDialog(
                onDismissRequest = { viewModel.ocultarConfirmacionRevertir() },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(40.dp)) },
                title = { Text("¿Volver a estudiar esta palabra?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color(0xFF1A237E)) },
                text = { Text("La palabra pasará a \"En progreso\" y volverá a aparecer en tu lista de estudio.", textAlign = TextAlign.Center, color = Color.Gray) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.revertirEstadoAprendido(palabra.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.ocultarConfirmacionRevertir() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun WordCard(
    palabra: PalabraConProgreso,
    onMarkLearned: () -> Unit,
    onRevert: () -> Unit,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color lateral basado en estado
            val indicatorColor = when (palabra.estado) {
                EstadoAprendizaje.APRENDIDA -> Color(0xFF388E3C)
                EstadoAprendizaje.EN_PROGRESO -> Color(0xFF1565C0)
                EstadoAprendizaje.DIFICIL -> Color(0xFFD32F2F)
                else -> Color(0xFFF57C00)
            }
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(indicatorColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = palabra.termino,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A237E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${palabra.tipoPalabra.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = palabra.traduccion,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                if (!palabra.fonetica.isNullOrBlank()) {
                    Text(
                        text = palabra.fonetica,
                        color = Color(0xFF039BE5),
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(palabra.estado)
                }
                Text(
                    text = palabra.dificultad,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    if (palabra.estado == EstadoAprendizaje.APRENDIDA) {
                        Button(
                            onClick = onRevert,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8F5E9),
                                contentColor = Color(0xFF388E3C)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Volver a estudiar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onMarkLearned,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE3F2FD),
                                contentColor = Color(0xFF1565C0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Marcar aprendida",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(estado: EstadoAprendizaje) {
    val (text, color) = when (estado) {
        EstadoAprendizaje.NO_VISTA -> "Pendiente" to Color(0xFFF57C00)
        EstadoAprendizaje.EN_PROGRESO -> "En progreso" to Color(0xFF1976D2)
        EstadoAprendizaje.DIFICIL -> "Difícil" to Color(0xFFD32F2F)
        EstadoAprendizaje.APRENDIDA -> "Aprendida" to Color(0xFF388E3C)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(filtro: VocabularioFiltro, isSearch: Boolean, onClearSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSearch) {
            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No se encontraron resultados", fontWeight = FontWeight.Bold)
            Text("Intenta con otra palabra o revisa la ortografía.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearSearch) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar búsqueda")
            }
        } else if (filtro == VocabularioFiltro.APRENDIDAS) {
            // HU11: Si no hay palabras aprendidas, se muestra un mensaje motivacional.
            Text("Aún no tienes palabras aprendidas.", fontWeight = FontWeight.Bold)
            Text("¡Sigue estudiando para ver tu progreso aquí!", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        } else {
            Text("No hay palabras en esta categoría.")
        }
    }
}

