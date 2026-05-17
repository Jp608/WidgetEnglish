package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel

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

    val item = uiState.palabrasOriginales.find { it.id == itemId && it.esVerbo == isVerbo }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isVerbo) "Detalle de verbo" else "Detalle de palabra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Compartir */ }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        item?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it.termino,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )
                        }

                        if (!it.fonetica.isNullOrBlank()) {
                            Text(
                                text = it.fonetica,
                                fontSize = 22.sp,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { ttsHelper.speak(it.termino) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF1565C0)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reproducir pronunciación", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        DetailRow(label = "Traducción", value = it.traduccion, icon = Icons.Default.Translate)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                        
                        val tipoLabel = if (it.esVerbo) "Tipo de verbo" else "Categoría"
                        val tipoValue = if (it.esVerbo) {
                            if (it.esIrregular) "Irregular" else "Regular"
                        } else {
                            it.tipoPalabra.name.lowercase().replaceFirstChar { char -> char.uppercase() }
                        }
                        DetailRow(label = tipoLabel, value = tipoValue, icon = Icons.Default.Category)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                        DetailRow(label = "Dificultad", value = it.dificultad, icon = Icons.Default.Speed)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                        if (it.esVerbo) {
                            DetailRow(label = "Past", value = it.pasadoSimple ?: "-", icon = Icons.Default.History)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                            DetailRow(label = "Participle", value = it.participioPasado ?: "-", icon = Icons.Default.Description)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                        }

                        DetailRow(label = "Estado", value = it.estado.name.replace("_", " "), icon = Icons.Default.AccessTime, isBadge = true, estado = it.estado)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (it.estado != EstadoAprendizaje.APRENDIDA) {
                    Button(
                        onClick = { 
                            viewModel.marcarComoAprendido(it.id, it.esVerbo)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Marcar como aprendido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.mostrarConfirmacionRevertir(it) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Text("Volver a estudiar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 16.sp, color = Color.Gray)
        }
        
        if (isBadge) {
            StatusChip(estado)
        } else {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
