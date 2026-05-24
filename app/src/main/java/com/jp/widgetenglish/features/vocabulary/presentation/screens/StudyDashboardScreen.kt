package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDashboardScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onStartQuiz: (String, Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Estudio", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Estadísticas
                item {
                    Text("Tus Estadísticas", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatsSmallCard(
                            label = "Aprendidas",
                            value = state.palabrasAprendidas.toString(),
                            icon = Icons.Default.EmojiEvents,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                        StatsSmallCard(
                            label = "Precisión",
                            value = "${state.precisionGlobal}%",
                            icon = Icons.Default.TrackChanges,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Lote Activo
                item {
                    Text("Lote Activo", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Book, null, tint = Color(0xFF2563EB), modifier = Modifier.padding(12.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(state.loteActivo?.nombre ?: "Sin lote activo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Progreso: ${state.progresoLote.toInt()}%", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { state.progresoLote / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF2563EB),
                                trackColor = Color(0xFFE5E7EB)
                            )
                        }
                    }
                }

                // Configuración de Quiz
                item {
                    Text("Configurar Próximo Quiz", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Cantidad de palabras", fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 15, 20).forEach { cant ->
                                    val selected = state.cantidadPalabrasQuiz == cant
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) Color(0xFF2563EB) else Color(0xFFF3F4F6))
                                            .clickable { viewModel.setCantidadPalabras(cant) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            cant.toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            state.loteActivo?.let { onStartQuiz(it.idLote, state.cantidadPalabrasQuiz) }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        enabled = state.loteActivo != null
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar Quiz", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    if (state.loteActivo == null) {
                        Text(
                            "Activa un lote para comenzar a estudiar",
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            color = Color.Red,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSmallCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
        }
    }
}
