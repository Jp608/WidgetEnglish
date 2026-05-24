package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    score: Int,
    total: Int,
    failedWords: List<PalabraConProgreso>,
    onRepasarFalladas: () -> Unit,
    onRepetirQuiz: () -> Unit,
    onVolverInicio: () -> Unit,
    onBack: () -> Unit
) {
    val precision = if (total > 0) (score.toFloat() / total * 100).toInt() else 0
    val mensaje = when {
        precision >= 90 -> "¡Excelente trabajo! 🌟"
        precision >= 70 -> "¡Muy bien hecho! 👍"
        else -> "¡Buen intento! Sigue así 💪"
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Resultado", fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color(0xFF111827))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Stats */ }) {
                        Icon(Icons.Default.BarChart, null, tint = Color(0xFF111827))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                null,
                                modifier = Modifier.size(54.dp),
                                tint = Color(0xFF2563EB)
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                score.toString(),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                " / $total",
                                fontSize = 28.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                        
                        Text(
                            "respuestas correctas", 
                            color = Color(0xFF111827), 
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            mensaje, 
                            color = Color(0xFF6B7280), 
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Correctas",
                        value = score.toString(),
                        color = Color(0xFF2563EB),
                        icon = Icons.Default.Check,
                        background = Color(0xFFEFF6FF)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Incorrectas",
                        value = (total - score).toString(),
                        color = Color(0xFFEF4444),
                        icon = Icons.Default.Close,
                        background = Color(0xFFFEF2F2)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Precisión",
                        value = "$precision%",
                        color = Color(0xFF7C3AED),
                        icon = Icons.Default.TrackChanges,
                        background = Color(0xFFF3E8FF)
                    )
                }
            }

            if (failedWords.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Palabras a reforzar",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                }

                items(failedWords) { palabra ->
                    FailedWordItem(palabra)
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (failedWords.isNotEmpty()) {
                        Button(
                            onClick = onRepasarFalladas,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text("Repasar falladas", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    } else {
                        // Felicitación si no hay errores
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFEAFBF2),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Color(0xFF10B981), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    "¡Perfecto! Dominas todo el vocabulario de este lote.",
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onRepetirQuiz,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF2563EB))
                    ) {
                        Text("Repetir quiz", color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = onVolverInicio,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Volver al inicio", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    background: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun FailedWordItem(palabra: PalabraConProgreso) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(palabra.termino, fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 16.sp)
                Text(palabra.traduccion, color = Color(0xFF6B7280), fontSize = 14.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(18.dp))
        }
    }
}
