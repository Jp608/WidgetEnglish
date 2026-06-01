package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularioFiltro
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularioSeccion
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel
import androidx.compose.runtime.LaunchedEffect
private val BackgroundColor = Color(0xFFF8FAFC)
private val SurfaceColor = Color.White
private val BluePrimary = Color(0xFF1E63D7)
private val BlueDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF6B7280)

private val SoftIndigo = Color(0xFFE8EAF6)
private val SoftOrange = Color(0xFFFFF3E0)
private val SoftBlue = Color(0xFFE3F2FD)
private val SoftGreen = Color(0xFFE8F5E9)

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
    val focusManager = LocalFocusManager.current
    val ttsHelper = remember { TtsHelper(context) }

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarioActual()
    }
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            VocabularyTopHeader(uiState.nombreLote)
        },
        bottomBar = {
            AppBottomBar(
                selectedRoute = "vocabulario",
                onInicioClick = {
                    focusManager.clearFocus()
                    onBackClick()
                },
                onVocabularioClick = {
                    focusManager.clearFocus()
                    onVocabularioClick()
                },
                onLotesClick = {
                    focusManager.clearFocus()
                    onLotesClick()
                },
                onEstudioClick = {
                    focusManager.clearFocus()
                    onEstudioClick()
                },
                onIaClick = {
                    focusManager.clearFocus()
                    onIaClick()
                },
                onPerfilClick = {
                    focusManager.clearFocus()
                    onPerfilClick()
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            SectionSelector(
                seccionActual = uiState.seccionActual,
                onSeccionChanged = {
                    focusManager.clearFocus()
                    viewModel.onSeccionChanged(it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Total",
                    value = uiState.totalPalabras.toString(),
                    color = Color(0xFF1A237E),
                    backgroundColor = SoftIndigo,
                    isSelected = uiState.filtroActual == VocabularioFiltro.TODAS,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onFiltroChanged(VocabularioFiltro.TODAS)
                    }
                )

                StatCard(
                    label = "Pend.",
                    value = uiState.palabrasPendientes.toString(),
                    color = Color(0xFFE65100),
                    backgroundColor = SoftOrange,
                    isSelected = uiState.filtroActual == VocabularioFiltro.PENDIENTES,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onFiltroChanged(VocabularioFiltro.PENDIENTES)
                    }
                )

                StatCard(
                    label = "Progreso",
                    value = uiState.palabrasEnProgreso.toString(),
                    color = Color(0xFF1565C0),
                    backgroundColor = SoftBlue,
                    isSelected = uiState.filtroActual == VocabularioFiltro.EN_PROGRESO,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onFiltroChanged(VocabularioFiltro.EN_PROGRESO)
                    }
                )

                StatCard(
                    label = "Aprend.",
                    value = uiState.palabrasAprendidas.toString(),
                    color = Color(0xFF2E7D32),
                    backgroundColor = SoftGreen,
                    isSelected = uiState.filtroActual == VocabularioFiltro.APRENDIDAS,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onFiltroChanged(VocabularioFiltro.APRENDIDAS)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.textoBusqueda,
                onValueChange = { viewModel.onSearchTextChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    val placeholder =
                        if (uiState.seccionActual == VocabularioSeccion.VERBOS) {
                            "Buscar verbo..."
                        } else {
                            "Buscar sustantivo..."
                        }

                    Text(
                        text = placeholder,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = BluePrimary
                    )
                },
                trailingIcon = {
                    if (uiState.textoBusqueda.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.onSearchTextChanged("")
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = TextMuted
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceColor,
                    focusedContainerColor = SurfaceColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = BluePrimary,
                    cursorColor = BluePrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val palabrasFiltradas = uiState.palabrasFiltradas

            if (palabrasFiltradas.isEmpty()) {
                EmptyState(
                    filtro = uiState.filtroActual,
                    isSearch = uiState.textoBusqueda.isNotEmpty()
                ) {
                    viewModel.onSearchTextChanged("")
                    focusManager.clearFocus()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    uiState.palabrasAgrupadas.forEach { (tipo, palabras) ->
                        // Mostrar encabezado (Sustantivos, Expresiones, etc.)
                        if (uiState.seccionActual == VocabularioSeccion.PALABRAS) {
                            item {
                                val titulo = when(tipo) {
                                    TipoPalabra.EXPRESION -> "Otros / Expresiones"
                                    TipoPalabra.SUSTANTIVO -> "Sustantivos"
                                    else -> tipo.name.lowercase().replaceFirstChar { it.uppercase() } + "s"
                                }
                                
                                Text(
                                    text = titulo,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BluePrimary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }

                        items(palabras) { palabra ->
                            WordCard(
                                palabra = palabra,
                                onMarkLearned = {
                                    focusManager.clearFocus()
                                    viewModel.marcarComoAprendido(context, palabra.id, palabra.esVerbo)
                                },
                                onRevert = {
                                    focusManager.clearFocus()
                                    viewModel.mostrarConfirmacionRevertir(palabra)
                                },
                                onSpeak = {
                                    focusManager.clearFocus()
                                    ttsHelper.speak(palabra.termino)
                                },
                                onClick = {
                                    focusManager.clearFocus()
                                    onItemClick(palabra)
                                }
                            )
                        }
                    }
                }
            }
        }

        uiState.mostrarDialogoRevertir?.let { palabra ->
            AlertDialog(
                onDismissRequest = {
                    focusManager.clearFocus()
                    viewModel.ocultarConfirmacionRevertir()
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "¿Volver a estudiar esta palabra?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = BlueDark
                    )
                },
                text = {
                    Text(
                        text = "La palabra pasará a \"En progreso\" y volverá a aparecer en tu lista de estudio.",
                        textAlign = TextAlign.Center,
                        color = TextMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.revertirEstadoAprendido(context, palabra.id, palabra.esVerbo)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.ocultarConfirmacionRevertir()
                        },
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
private fun VocabularyTopHeader(nombreLote: String? = null) {
    Surface(
        color = BluePrimary,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nombreLote ?: "Vocabulario",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
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
            .background(Color(0xFFF1F5F9), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SectionButton(
            text = "Sustantivos",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            isSelected = seccionActual == VocabularioSeccion.PALABRAS,
            onClick = { onSeccionChanged(VocabularioSeccion.PALABRAS) },
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
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) BluePrimary else Color.Transparent,
        contentColor = if (isSelected) Color.White else TextMuted
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
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
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        border = if (isSelected) BorderStroke(1.5.dp, color.copy(alpha = 0.45f)) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
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
                                text = "P: ${palabra.pasadoSimple}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                maxLines = 1
                            )

                            Text(
                                text = "PP: ${palabra.participioPasado}",
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = onSpeak,
                            shape = RoundedCornerShape(10.dp),
                            color = SoftBlue,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (palabra.estado == EstadoAprendizaje.APRENDIDA) {
                            Button(
                                onClick = onRevert,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftGreen,
                                    contentColor = Color(0xFF388E3C)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(30.dp)
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
                                    containerColor = SoftBlue,
                                    contentColor = BluePrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(30.dp)
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
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(
    filtro: VocabularioFiltro,
    isSearch: Boolean,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSearch) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No se encontraron resultados",
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Intenta con otra palabra o revisa la ortografía.",
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar búsqueda")
            }
        } else if (filtro == VocabularioFiltro.APRENDIDAS) {
            Text(
                text = "Aún no tienes palabras aprendidas.",
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "¡Sigue estudiando para ver tu progreso aquí!",
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "No hay palabras en esta categoría.",
                color = TextMuted
            )
        }
    }
}