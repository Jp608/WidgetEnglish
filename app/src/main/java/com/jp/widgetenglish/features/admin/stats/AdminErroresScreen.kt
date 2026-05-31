package com.jp.widgetenglish.features.admin.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.remote.firestore.PalabraErrorStatsDto
import com.jp.widgetenglish.features.admin.AdminViewModel
import com.jp.widgetenglish.features.admin.components.AdminBottomBar

private val BackgroundSoft = Color(0xFFF5F7FB)
private val PrimaryBlue = Color(0xFF2468D8)
private val TextMain = Color(0xFF1F2A60)
private val TextMuted = Color(0xFF7B8190)
private val CardWhite = Color.White
private val RedMain = Color(0xFFD32F2F)

@Composable
fun AdminErroresScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onResumenClick: () -> Unit,
    onRankingClick: () -> Unit,
    onActividadClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    AdminStatsStatusBarColor()

    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarDatosAdmin()
    }

    val filteredErrors = if (selectedCategory == null) {
        uiState.erroresStats
    } else {
        uiState.erroresStats.filter { it.loteId == selectedCategory }
    }

    val categoriesWithErrors = uiState.erroresStats
        .map { it.loteId }
        .filter { it.isNotBlank() }
        .distinct()

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
        ) {
            AdminStatsHeader(
                title = "Palabras con más errores",
                subtitle = "Top 50 términos de mayor dificultad",
                isRefreshing = uiState.cargando,
                onBack = onBack,
                onRefreshClick = { viewModel.cargarDatosAdmin() }
            )

            if (categoriesWithErrors.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Todas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )

                    categoriesWithErrors.forEach { categoryId ->
                        Spacer(modifier = Modifier.width(8.dp))

                        FilterChip(
                            selected = selectedCategory == categoryId,
                            onClick = { selectedCategory = categoryId },
                            label = {
                                val categoryName = uiState.categoriasStats
                                    .find { it.id == categoryId }
                                    ?.nombre
                                    ?: categoryId

                                Text(categoryName)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (uiState.cargando) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (filteredErrors.isEmpty()) {
                    StatsEmptyState(
                        text = if (selectedCategory == null) {
                            "No hay registros de errores aún."
                        } else {
                            "No hay errores registrados para esta categoría."
                        },
                        icon = Icons.Default.FilterList
                    )
                } else {
                    filteredErrors.forEachIndexed { index, stat ->
                        PalabraErrorItem(
                            stat = stat,
                            index = index + 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PalabraErrorItem(
    stat: PalabraErrorStatsDto,
    index: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RedMain.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = RedMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.termino,
                    color = TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "ID: ${stat.id}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${stat.cantidadErrores}",
                    color = RedMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "errores",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
