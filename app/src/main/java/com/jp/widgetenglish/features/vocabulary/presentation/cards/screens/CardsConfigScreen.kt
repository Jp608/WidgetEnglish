package com.jp.widgetenglish.features.vocabulary.presentation.cards.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsStudyFilter
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsStudyMode
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsViewModel

private val PrimaryBlue = Color(0xFF1565C0)
private val StrongBlue = Color(0xFF0057E7)
private val Purple = Color(0xFF7C3AED)
private val ScreenBg = Color(0xFFF7F9FD)
private val TextDark = Color(0xFF08145F)
private val TextMuted = Color(0xFF6B7280)
private val SoftBlue = Color(0xFFEAF2FF)
private val BorderSoft = Color(0xFFE1E7F5)
private val Green = Color(0xFF2E7D32)
private val Orange = Color(0xFFE97B00)
private val Red = Color(0xFFD32F2F)

@Composable
fun CardsConfigScreen(
    viewModel: CardsViewModel,
    onBack: () -> Unit,
    onSaveConfig: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val config = state.config

    Scaffold(
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            CardsConfigHeader(
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 18.dp, bottom = 22.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    border = BorderStroke(1.dp, BorderSoft)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "¿Qué deseas repasar?",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterOptionChip(
                                modifier = Modifier.weight(1f),
                                text = "Todas",
                                icon = Icons.Filled.ViewModule,
                                selected = config.filtro == CardsStudyFilter.TODAS,
                                color = TextMuted,
                                onClick = {
                                    viewModel.seleccionarFiltro(CardsStudyFilter.TODAS)
                                }
                            )

                            FilterOptionChip(
                                modifier = Modifier.weight(1.35f),
                                text = "En progreso",
                                icon = Icons.Filled.Replay,
                                selected = config.filtro == CardsStudyFilter.EN_PROGRESO,
                                color = StrongBlue,
                                onClick = {
                                    viewModel.seleccionarFiltro(CardsStudyFilter.EN_PROGRESO)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterOptionChip(
                                modifier = Modifier.weight(1.15f),
                                text = "Aprendidas",
                                icon = Icons.Filled.CheckCircle,
                                selected = config.filtro == CardsStudyFilter.APRENDIDAS,
                                color = Green,
                                onClick = {
                                    viewModel.seleccionarFiltro(CardsStudyFilter.APRENDIDAS)
                                }
                            )

                            FilterOptionChip(
                                modifier = Modifier.weight(1f),
                                text = "Difíciles",
                                icon = Icons.Filled.Warning,
                                selected = config.filtro == CardsStudyFilter.DIFICILES,
                                color = Red,
                                onClick = {
                                    viewModel.seleccionarFiltro(CardsStudyFilter.DIFICILES)
                                }
                            )
                        }

                        SectionDivider()

                        Text(
                            text = "¿Cuántas palabras deseas revisar?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(5, 10, 15, 20).forEach { cantidad ->
                                QuantityOption(
                                    modifier = Modifier.weight(1f),
                                    text = cantidad.toString(),
                                    selected = !config.usarTodas && config.cantidad == cantidad,
                                    onClick = {
                                        viewModel.seleccionarCantidad(cantidad)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        QuantityOption(
                            modifier = Modifier.fillMaxWidth(),
                            text = "∞  Todas",
                            selected = config.usarTodas,
                            onClick = {
                                viewModel.seleccionarTodasLasTarjetas()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Tiempo estimado: ${config.tiempoEstimadoMinutos()} min",
                                color = TextMuted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        SectionDivider()

                        Text(
                            text = "Modo de estudio",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StudyModeOption(
                                modifier = Modifier.weight(1f),
                                text = "Aleatorio",
                                icon = Icons.Filled.Shuffle,
                                selected = config.modo == CardsStudyMode.ALEATORIO,
                                onClick = {
                                    viewModel.seleccionarModo(CardsStudyMode.ALEATORIO)
                                }
                            )

                            StudyModeOption(
                                modifier = Modifier.weight(1f),
                                text = "Orden del lote",
                                icon = Icons.Filled.LibraryBooks,
                                selected = config.modo == CardsStudyMode.ORDEN_LOTE,
                                onClick = {
                                    viewModel.seleccionarModo(CardsStudyMode.ORDEN_LOTE)
                                }
                            )
                        }

                        SectionDivider()

                        Text(
                            text = "Preferencias",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        PreferenceRow(
                            icon = Icons.Filled.Language,
                            text = "Mostrar traducción al inicio",
                            checked = config.mostrarTraduccionAlInicio,
                            onCheckedChange = {
                                viewModel.toggleMostrarTraduccion()
                            }
                        )

                        PreferenceRow(
                            icon = Icons.Filled.RecordVoiceOver,
                            text = "Incluir pronunciación",
                            checked = config.incluirPronunciacion,
                            onCheckedChange = {
                                viewModel.toggleIncluirPronunciacion()
                            }
                        )

                        PreferenceRow(
                            icon = Icons.Filled.Sms,
                            text = "Mostrar ejemplo de uso",
                            checked = config.mostrarEjemploUso,
                            onCheckedChange = {
                                viewModel.toggleMostrarEjemploUso()
                            }
                        )

                        PreferenceRow(
                            icon = Icons.Filled.GpsFixed,
                            text = "Saltar palabras ya dominadas",
                            checked = config.saltarDominadas,
                            onCheckedChange = {
                                viewModel.toggleSaltarDominadas()
                            }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        InfoBox()
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onSaveConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StrongBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 5.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Guardar configuración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = StrongBlue
                    ),
                    border = BorderStroke(2.dp, StrongBlue)
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CardsConfigHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0057E7),
                        Color(0xFF1565C0),
                        Purple
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
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
                modifier = Modifier.size(31.dp)
            )
        }

        Text(
            text = "Configuración detallada",
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Icon(
            imageVector = Icons.Filled.CropFree,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
                .size(34.dp)
        )
    }
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(22.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE5E7EB))
    )

    Spacer(modifier = Modifier.height(22.dp))
}

@Composable
private fun FilterOptionChip(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) SoftBlue else Color.White,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) StrongBlue else BorderSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) StrongBlue else color,
                modifier = Modifier.size(23.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = text,
                color = if (selected) StrongBlue else TextDark,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuantityOption(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = if (selected) SoftBlue else Color(0xFFF8FAFC),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) StrongBlue else BorderSoft
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) StrongBlue else TextMuted,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun StudyModeOption(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = if (selected) SoftBlue else Color.White,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) StrongBlue else BorderSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) StrongBlue else TextMuted,
                modifier = Modifier.size(25.dp)
            )

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = text,
                color = if (selected) StrongBlue else TextDark,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    text: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            color = TextMuted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = {
                onCheckedChange()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StrongBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD1D5DB)
            )
        )
    }
}

@Composable
private fun InfoBox() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SoftBlue,
        border = BorderStroke(1.dp, Color(0xFFC7DBFF))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = StrongBlue,
                        modifier = Modifier.size(29.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Podrás clasificar cada palabra como La conozco, No la conozco, Difícil o Aprendida durante la sesión.",
                color = StrongBlue,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}