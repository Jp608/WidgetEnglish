package com.jp.widgetenglish.features.admin.ranking

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.jp.widgetenglish.data.remote.firestore.AdminUsuarioDto
import com.jp.widgetenglish.features.admin.AdminViewModel
import com.jp.widgetenglish.features.admin.CriterioRanking
import com.jp.widgetenglish.features.admin.components.AdminBottomBar
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton

private val BackgroundSoft = Color(0xFFF5F7FB)

private val HeaderBlue = Color(0xFF2468D8)
private val PrimaryBlue = Color(0xFF2468D8)
private val PrimaryBlueSoft = Color(0xFFE3F1FF)
private val PrimaryBlueExtraSoft = Color(0xFFF0F7FF)

private val CardWhite = Color(0xFFFFFFFF)

private val TextMain = Color(0xFF1F2A60)
private val TextMuted = Color(0xFF7B8190)
private val BorderSoft = Color(0xFFE2E8F0)

private val Gold = Color(0xFFF2B705)
private val Silver = Color(0xFF7E8FA8)
private val Bronze = Color(0xFFD87A42)

@Composable
fun AdminRankingScreen(
    viewModel: AdminViewModel,
    onResumenClick: () -> Unit,
    onActividadClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    RankingStatusBarColor()

    LaunchedEffect(Unit) {
        viewModel.cargarDatosAdmin()
    }

    Scaffold(
        topBar = {
            FixedRankingHeader(
                isRefreshing = uiState.cargando,
                onRefreshClick = {
                    viewModel.cargarDatosAdmin(forzarActualizacion = true)
                }
            )
        },
        bottomBar = {
            AdminBottomBar(
                selected = "ranking",
                onResumenClick = onResumenClick,
                onRankingClick = {},
                onActividadClick = onActividadClick,
                onPerfilClick = onPerfilClick
            )
        },
        containerColor = BackgroundSoft
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundSoft)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            RankingInfoCard(
                criterio = when (uiState.criterioRanking) {
                    CriterioRanking.PALABRAS -> "Palabras aprendidas"
                    CriterioRanking.QUIZZES -> "Quizzes realizados"
                    CriterioRanking.RACHA -> "Racha actual"
                },
                totalUsuarios = uiState.rankingUsuarios.size
            )

            Spacer(modifier = Modifier.height(18.dp))

            RankingFilters(
                selected = uiState.criterioRanking,
                onPalabrasClick = {
                    viewModel.cambiarCriterioRanking(CriterioRanking.PALABRAS)
                },
                onQuizzesClick = {
                    viewModel.cambiarCriterioRanking(CriterioRanking.QUIZZES)
                },
                onRachaClick = {
                    viewModel.cambiarCriterioRanking(CriterioRanking.RACHA)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Tabla de posiciones",
                color = TextMain,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.rankingUsuarios.isEmpty()) {
                EmptyRankingCard()
            } else {
                uiState.rankingUsuarios.forEachIndexed { index, usuario ->
                    RankingUserCard(
                        position = index + 1,
                        usuario = usuario,
                        criterio = uiState.criterioRanking
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun RankingStatusBarColor() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window

        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)

            val previousStatusBarColor = window.statusBarColor
            val previousNavigationBarColor = window.navigationBarColor
            val previousLightStatusBars = controller.isAppearanceLightStatusBars
            val previousLightNavigationBars = controller.isAppearanceLightNavigationBars

            window.statusBarColor = HeaderBlue.toArgb()
            window.navigationBarColor = Color.White.toArgb()

            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = true

            onDispose {
                window.statusBarColor = previousStatusBarColor
                window.navigationBarColor = previousNavigationBarColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
                controller.isAppearanceLightNavigationBars = previousLightNavigationBars
            }
        } else {
            onDispose { }
        }
    }
}

@Composable
private fun FixedRankingHeader(
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HeaderBlue,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Ranking",
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isRefreshing) {
                        "Actualizando posiciones..."
                    } else {
                        "Clasificación de usuarios"
                    },
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Autorenew,
                            contentDescription = "Actualizar ranking",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingInfoCard(
    criterio: String,
    totalUsuarios: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlueSoft,
                            Color.White,
                            PrimaryBlueExtraSoft
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Criterio actual",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = criterio,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$totalUsuarios",
                        color = PrimaryBlue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "usuarios",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingFilters(
    selected: CriterioRanking,
    onPalabrasClick: () -> Unit,
    onQuizzesClick: () -> Unit,
    onRachaClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RankingFilterChip(
            text = "Palabras",
            selected = selected == CriterioRanking.PALABRAS,
            onClick = onPalabrasClick
        )

        RankingFilterChip(
            text = "Quizzes",
            selected = selected == CriterioRanking.QUIZZES,
            onClick = onQuizzesClick
        )

        RankingFilterChip(
            text = "Racha",
            selected = selected == CriterioRanking.RACHA,
            onClick = onRachaClick
        )
    }
}

@Composable
private fun RankingFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CardWhite,
            selectedContainerColor = PrimaryBlue,
            labelColor = TextMuted,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun EmptyRankingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderSoft,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = PrimaryBlueSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No hay usuarios registrados",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cuando existan usuarios, aparecerán aquí ordenados por el criterio seleccionado.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RankingUserCard(
    position: Int,
    usuario: AdminUsuarioDto,
    criterio: CriterioRanking
) {
    val metricValue = when (criterio) {
        CriterioRanking.PALABRAS -> "${usuario.palabrasAprendidas}"
        CriterioRanking.QUIZZES -> "${usuario.quizzesRealizados}"
        CriterioRanking.RACHA -> "${usuario.rachaActual}"
    }

    val metricLabel = when (criterio) {
        CriterioRanking.PALABRAS -> "palabras"
        CriterioRanking.QUIZZES -> "quizzes"
        CriterioRanking.RACHA -> "días"
    }

    val positionColor = when (position) {
        1 -> Gold
        2 -> Silver
        3 -> Bronze
        else -> PrimaryBlue
    }

    val cardColor = when (position) {
        1 -> Color(0xFFFFF4D6)
        2 -> Color(0xFFEFF4FA)
        3 -> Color(0xFFFFE7DA)
        else -> CardWhite
    }

    val avatarText = usuario.nombre
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "U" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (position <= 3) {
                    positionColor.copy(alpha = 0.48f)
                } else {
                    BorderSoft
                },
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (position <= 3) 5.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = positionColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$position",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PrimaryBlue,
                                Color(0xFF8ED6F8)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = usuario.nombre,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (position <= 3) {
                        Spacer(modifier = Modifier.width(5.dp))

                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = positionColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = usuario.correo,
                    fontSize = 13.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = metricValue,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = metricLabel,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
