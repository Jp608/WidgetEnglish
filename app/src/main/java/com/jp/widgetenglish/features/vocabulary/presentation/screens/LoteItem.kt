package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.dao.LoteConProgreso

@Composable
fun LoteItem(
    loteConProgreso: LoteConProgreso,
    estaActivo: Boolean,
    onActivar: () -> Unit,
    onVerContenido: () -> Unit,
    onReiniciar: () -> Unit
) {
    val lote = loteConProgreso.lote
    val progreso = loteConProgreso.progreso
    
    // Colores dinámicos (Cambio solicitado: verde si está activo)
    val cardColor = if (estaActivo) Color(0xFFF1FDF4) else Color.White
    val primaryColor = if (estaActivo) Color(0xFF2E7D32) else Color(0xFF1565C0)
    val iconBgColor = if (estaActivo) Color(0xFFDCFCE7) else Color(0xFFE3F2FD)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (estaActivo) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = primaryColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lote.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (estaActivo) Color(0xFFDCFCE7) else Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = lote.tipoLote.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = if (estaActivo) Color(0xFF166534) else Color(0xFF1D4ED8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lote.descripcion ?: "",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1
                        )
                    }
                }

                if (estaActivo) {
                    Surface(
                        color = Color(0xFF166534),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ACTIVO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val porcentaje = progreso?.progresoPorcentaje ?: 0f
            val aprendidas = progreso?.contenidosAprendidos ?: 0
            val total = lote.cantidadContenido

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progreso: ${porcentaje.toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (estaActivo) Color(0xFF166534) else Color.Black
                )
                Text(
                    text = "$aprendidas / $total palabras",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { porcentaje / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primaryColor,
                trackColor = if (estaActivo) Color(0xFFDCFCE7) else Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onVerContenido,
                    colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
                ) {
                    Text("VER CONTENIDO")
                }
                
                if (porcentaje > 0) {
                    IconButton(onClick = onReiniciar) {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Reiniciar", 
                            tint = if (estaActivo) Color(0xFF166534) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onActivar,
                    enabled = !estaActivo,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text(if (estaActivo) "ACTIVADO" else "ACTIVAR")
                }
            }
        }
    }
}
