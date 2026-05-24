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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
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
                title = { Text("Plan de Estudio", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con Gradiente y Stats
                StudyHeader(
                    aprendidas = state.palabrasAprendidas,
                    precision = state.precisionGlobal
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Lote Activo - Mejorado
                    item {
                        SectionTitle("Tu Progreso Actual")
                        Spacer(Modifier.height(16.dp))
                        ActiveLotCard(
                            nombre = state.loteActivo?.nombre ?: "Sin lote activo",
                            progreso = state.progresoLote
                        )
                    }

                    // Configuración de Quiz - Mejorado
                    item {
                        SectionTitle("Configurar Sesión")
                        Spacer(Modifier.height(16.dp))
                        QuizConfigCard(
                            selectedAmount = state.cantidadPalabrasQuiz,
                            onAmountSelected = { viewModel.setCantidadPalabras(it) }
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                state.loteActivo?.let { onStartQuiz(it.idLote, state.cantidadPalabrasQuiz) }
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                disabledContainerColor = Color(0xFFE5E7EB)
                            ),
                            enabled = state.loteActivo != null,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Iniciar Entrenamiento", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }

                        if (state.loteActivo == null) {
                            Text(
                                "Selecciona un lote en la sección de Lotes para comenzar",
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyHeader(aprendidas: Int, precision: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                )
            )
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderStatItem("Aprendidas", aprendidas.toString(), Icons.Default.EmojiEvents)
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
            HeaderStatItem("Precisión", "$precision%", Icons.Default.TrackChanges)
        }
    }
}

@Composable
private fun HeaderStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF111827)
    )
}

@Composable
private fun ActiveLotCard(nombre: String, progreso: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Book, null, tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF111827))
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progreso / 100f },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFE5E7EB)
                )
                Spacer(Modifier.height(8.dp))
                Text("${progreso.toInt()}% completado", color = Color(0xFF6B7280), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuizConfigCard(selectedAmount: Int, onAmountSelected: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "¿Cuántas palabras quieres practicar?",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                fontSize = 15.sp
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(5, 10, 15, 20).forEach { cant ->
                    val selected = selectedAmount == cant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Color(0xFF2563EB) else Color(0xFFF3F4F6))
                            .clickable { onAmountSelected(cant) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            cant.toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (selected) Color.White else Color(0xFF4B5563)
                        )
                    }
                }
            }
        }
    }
}
