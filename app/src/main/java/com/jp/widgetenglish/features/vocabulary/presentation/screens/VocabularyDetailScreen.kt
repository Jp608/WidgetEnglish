package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.common.UserHeaderBlue
import com.jp.widgetenglish.features.common.UserHeaderSystemBars
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel

private val DetailBackgroundColor = Color(0xFFF8FAFC)
private val DetailBluePrimary = Color(0xFF1E63D7)
private val DetailBlueDark = Color(0xFF0F172A)
private val DetailTextMuted = Color(0xFF6B7280)

private val DetailSoftBlue = Color(0xFFE3F2FD)
private val DetailSoftGreen = Color(0xFFE8F5E9)
private val DetailSoftRed = Color(0xFFFFEBEE)
private val DetailSoftPurple = Color(0xFFF3E8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyDetailScreen(
    itemId: String,
    isVerbo: Boolean,
    viewModel: VocabularyViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    UserHeaderSystemBars()

    val item = uiState.palabrasOriginales.find {
        it.id == itemId && it.esVerbo == isVerbo
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        topBar = {
            DetailTopHeader(
                title = if (isVerbo) "Detalle verbo" else " Detalle palabra",
                onBack = onBack
            )
        },
        containerColor = DetailBackgroundColor
    ) { paddingValues ->

        item?.let { palabra ->

            val ejemploIngles = if (palabra.esVerbo) {
                palabra.ejemploIngles
            } else {
                palabra.ejemplo
            }

            val ejemploEspanol = if (palabra.esVerbo) {
                palabra.ejemploEspanol
            } else {
                palabra.ejemploTraduccion
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = palabra.termino,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A237E),
                            textAlign = TextAlign.Center
                        )

                        if (!palabra.fonetica.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = palabra.fonetica,
                                fontSize = 22.sp,
                                color = DetailTextMuted,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                ttsHelper.speak(palabra.termino)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DetailSoftBlue,
                                contentColor = DetailBluePrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Reproducir pronunciación",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!ejemploIngles.isNullOrBlank() || !ejemploEspanol.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))

                            ContextExampleCard(
                                ejemploIngles = ejemploIngles.orEmpty(),
                                ejemploEspanol = ejemploEspanol.orEmpty()
                            )
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        SectionLabel(text = "Información")

                        Spacer(modifier = Modifier.height(14.dp))

                        DetailRow(
                            label = "Traducción",
                            value = palabra.traduccion,
                            icon = Icons.Default.Translate
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF0F0F0)
                        )

                        val tipoLabel = if (palabra.esVerbo) {
                            "Tipo de verbo"
                        } else {
                            "Categoría"
                        }

                        val tipoValue = if (palabra.esVerbo) {
                            if (palabra.esIrregular) "Irregular" else "Regular"
                        } else {
                            palabra.tipoPalabra.name
                                .lowercase()
                                .replaceFirstChar { char -> char.uppercase() }
                        }

                        DetailRow(
                            label = tipoLabel,
                            value = tipoValue,
                            icon = Icons.Default.Category
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF0F0F0)
                        )

                        DetailRow(
                            label = "Dificultad",
                            value = palabra.dificultad,
                            icon = Icons.Default.Speed
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF0F0F0)
                        )

                        if (palabra.esVerbo) {
                            DetailRow(
                                label = "Pasado",
                                value = palabra.pasadoSimple ?: "-",
                                icon = Icons.Default.History
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFF0F0F0)
                            )

                            DetailRow(
                                label = "Participio",
                                value = palabra.participioPasado ?: "-",
                                icon = Icons.Default.Description
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFF0F0F0)
                            )
                        }

                        DetailRow(
                            label = "Estado",
                            value = palabra.estado.name.replace("_", " "),
                            icon = Icons.Default.AccessTime,
                            isBadge = true,
                            estado = palabra.estado
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (palabra.estado != EstadoAprendizaje.APRENDIDA) {
                    Button(
                        onClick = {
                            viewModel.marcarComoAprendido(
                                context,
                                palabra.id,
                                palabra.esVerbo
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DetailBluePrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 1.dp
                        )
                    ) {
                        Text(
                            text = "Marcar como aprendido",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            viewModel.mostrarConfirmacionRevertir(palabra)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DetailSoftGreen,
                            contentColor = Color(0xFF2E7D32)
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = Color(0xFF2E7D32).copy(alpha = 0.45f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Volver a estudiar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DetailBluePrimary)
            }
        }

        uiState.mostrarDialogoRevertir?.let { palabraDialogo ->
            AlertDialog(
                onDismissRequest = {
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
                        text = if (palabraDialogo.esVerbo) {
                            "¿Volver a estudiar este verbo?"
                        } else {
                            "¿Volver a estudiar esta palabra?"
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = DetailBlueDark
                    )
                },
                text = {
                    Text(
                        text = if (palabraDialogo.esVerbo) {
                            "El verbo pasará a \"En progreso\" y volverá a aparecer en tu lista de estudio."
                        } else {
                            "La palabra pasará a \"En progreso\" y volverá a aparecer en tu lista de estudio."
                        },
                        textAlign = TextAlign.Center,
                        color = DetailTextMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.revertirEstadoAprendido(
                                context,
                                palabraDialogo.id,
                                palabraDialogo.esVerbo
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DetailBluePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Confirmar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.ocultarConfirmacionRevertir()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = DetailSoftRed,
                            contentColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailTopHeader(
    title: String,
    onBack: () -> Unit
) {
    Surface(
        color = UserHeaderBlue,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = { /* Compartir */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ContextExampleCard(
    ejemploIngles: String,
    ejemploEspanol: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF5F8FF),
        border = BorderStroke(
            width = 1.dp,
            color = DetailBluePrimary.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DetailSoftBlue
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = DetailBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ejemplo:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DetailBlueDark
                    )

                    Text(
                        text = "Observa cómo se usa naturalmente",
                        fontSize = 12.sp,
                        color = DetailTextMuted
                    )
                }
            }

            if (ejemploIngles.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = ejemploIngles,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DetailBlueDark,
                    lineHeight = 24.sp
                )
            }

            if (ejemploEspanol.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ejemploEspanol,
                    fontSize = 15.sp,
                    color = DetailTextMuted,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = DetailBlueDark,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.width(10.dp))

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE5E7EB)
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    isBadge: Boolean = false,
    estado: EstadoAprendizaje = EstadoAprendizaje.NO_VISTA
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DetailTextMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                fontSize = 16.sp,
                color = DetailTextMuted
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isBadge) {
            StatusChip(estado)
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DetailBlueDark,
                textAlign = TextAlign.End
            )
        }
    }
}
