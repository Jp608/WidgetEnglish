package com.jp.widgetenglish.features.admin.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.remote.firestore.CategoriaStatsDto
import com.jp.widgetenglish.features.admin.AdminViewModel
import com.jp.widgetenglish.features.admin.components.AdminBottomBar

private val BackgroundSoft = Color(0xFFF5F7FB)
private val PrimaryBlue = Color(0xFF2468D8)
private val TextMain = Color(0xFF1F2A60)
private val TextMuted = Color(0xFF7B8190)
private val CardWhite = Color.White

@Composable
fun AdminCategoriasScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onResumenClick: () -> Unit,
    onRankingClick: () -> Unit,
    onActividadClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    AdminStatsStatusBarColor()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatosAdmin()
    }

    Scaffold(
        bottomBar = {
            AdminBottomBar(
                selected = "resumen",
                onResumenClick = onResumenClick,
                onRankingClick = onRankingClick,
                onActividadClick = onActividadClick,
                onPerfilClick = onPerfilClick
            )
        },
        containerColor = BackgroundSoft
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            AdminStatsHeader(
                title = "Categorías más usadas",
                subtitle = "Análisis de participación por tema",
                isRefreshing = uiState.cargando,
                onBack = onBack,
                onRefreshClick = {
                    viewModel.cargarDatosAdmin(forzarActualizacion = true)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (uiState.error != null && uiState.categoriasStats.isEmpty()) {
                StatsErrorState(
                    text = uiState.error.orEmpty(),
                    onRetry = {
                        viewModel.cargarDatosAdmin(forzarActualizacion = true)
                    }
                )
            } else if (uiState.categoriasStats.isEmpty()) {
                StatsEmptyState(
                    text = "No hay datos de categorías registrados aún.",
                    icon = Icons.Default.BarChart
                )
            } else {
                uiState.categoriasStats.forEachIndexed { index, stat ->
                    CategoriaStatItem(
                        stat = stat,
                        index = index + 1,
                        maxUso = uiState.categoriasStats.firstOrNull()?.vecesEstudiada ?: 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoriaStatItem(
    stat: CategoriaStatsDto,
    index: Int,
    maxUso: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$index",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.nombre,
                    color = TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${stat.vecesEstudiada} sesiones",
                    color = TextMuted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val progress = if (maxUso > 0) {
                    stat.vecesEstudiada.toFloat() / maxUso.toFloat()
                } else {
                    0f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                            .fillMaxSize()
                            .background(PrimaryBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = PrimaryBlue.copy(alpha = 0.5f)
            )
        }
    }
}
