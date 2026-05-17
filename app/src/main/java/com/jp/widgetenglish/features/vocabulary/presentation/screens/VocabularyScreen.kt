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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularioFiltro
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularioSeccion
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
    onIaClick: () -> Unit,
    onItemClick: (PalabraConProgreso) -> Unit
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
                    .height(100.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
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
                    Text(
                        text = "Vocabulario",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Secciones (Sustantivos / Verbos)
                SectionSelector(
                    seccionActual = uiState.seccionActual,
                    onSeccionChanged = { viewModel.onSeccionChanged(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Estadísticas estilo Home con navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    placeholder = { 
                        val placeholder = when (uiState.seccionActual) {
                            VocabularioSeccion.VERBOS -> "Buscar verbo..."
                            VocabularioSeccion.ADJETIVOS -> "Buscar adjetivo..."
                            else -> "Buscar sustantivo..."
                        }
                        Text(placeholder, color = Color.Gray) 
                    },
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
                        val (_, chipContentColor) = when (filtro) {
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
                                onMarkLearned = { viewModel.marcarComoAprendido(palabra.id, palabra.esVerbo) },
                                onRevert = { viewModel.mostrarConfirmacionRevertir(palabra) },
                                onSpeak = { ttsHelper.speak(palabra.termino) },
                                onClick = { onItemClick(palabra) }
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
                        onClick = { viewModel.revertirEstadoAprendido(palabra.id, palabra.esVerbo) },
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
fun SectionSelector(
    seccionActual: VocabularioSeccion,
    onSeccionChanged: (VocabularioSeccion) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEEEEE), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SectionButton(
            text = "Sust.",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            isSelected = seccionActual == VocabularioSeccion.PALABRAS,
            onClick = { onSeccionChanged(VocabularioSeccion.PALABRAS) },
            modifier = Modifier.weight(1f)
        )
        SectionButton(
            text = "Adj.",
            icon = Icons.Default.Description,
            isSelected = seccionActual == VocabularioSeccion.ADJETIVOS,
            onClick = { onSeccionChanged(VocabularioSeccion.ADJETIVOS) },
            modifier = Modifier.weight(1f)
        )
        SectionButton(
            text = "Verbos",
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            isSelected = seccionActual == VocabularioSeccion.VERBOS,
            onClick = { onSeccionChanged(VocabularioSeccion.VERBOS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SectionButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1565C0) else Color.Transparent,
        contentColor = if (isSelected) Color.White else Color.Gray
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
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
    onSpeak: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
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
                    .fillMaxHeight()
                    .background(indicatorColor)
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = palabra.termino,
                            fontWeight = FontWeight.Bold,
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
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = palabra.traduccion,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    
                    if (palabra.esVerbo) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "P: ${palabra.pasadoSimple}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                            Text(text = "PP: ${palabra.participioPasado}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
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

                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(palabra.estado)
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (palabra.estado == EstadoAprendizaje.APRENDIDA) {
                            Button(
                                onClick = onRevert,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE8F5E9),
                                    contentColor = Color(0xFF388E3C)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Volver a estudiar",
                                    fontSize = 11.sp,
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
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Marcar aprendida",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
            Text("Intenta con otra palabra o revisa la ortografía.", color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearSearch) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar búsqueda")
            }
        } else if (filtro == VocabularioFiltro.APRENDIDAS) {
            Text("Aún no tienes palabras aprendidas.", fontWeight = FontWeight.Bold)
            Text("¡Sigue estudiando para ver tu progreso aquí!", color = Color.Gray, textAlign = TextAlign.Center)
        } else {
            Text("No hay palabras en esta categoría.")
        }
    }
}
