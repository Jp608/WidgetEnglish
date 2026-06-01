package com.jp.widgetenglish.features.admin.activity

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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.jp.widgetenglish.data.remote.firestore.AdminUsuarioDto
import com.jp.widgetenglish.features.admin.AdminViewModel
import com.jp.widgetenglish.features.admin.CriterioActividad
import com.jp.widgetenglish.features.admin.components.AdminBottomBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton

private val BackgroundSoft = Color(0xFFF5F7FB)

private val HeaderBlue = Color(0xFF2468D8)
private val PrimaryBlue = Color(0xFF2468D8)
private val PrimaryBlueDark = Color(0xFF1F2A60)
private val PrimaryBlueSoft = Color(0xFFE3F1FF)
private val PrimaryBlueExtraSoft = Color(0xFFF0F7FF)

private val CardWhite = Color.White
private val TextMain = Color(0xFF1F2A60)
private val TextMuted = Color(0xFF7B8190)
private val BorderSoft = Color(0xFFE2E8F0)

private val GreenMain = Color(0xFF2E9D4D)
private val GreenSoft = Color(0xFFEAF7EC)

private val OrangeMain = Color(0xFFFF7A00)
private val OrangeSoft = Color(0xFFFFF1DF)

private val GraySoft = Color(0xFFF1F4F8)
private val GrayMain = Color(0xFF8A94A6)

@Composable
fun AdminActivityScreen(
    viewModel: AdminViewModel,
    onResumenClick: () -> Unit,
    onRankingClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ActivityStatusBarColor()

    LaunchedEffect(Unit) {
        viewModel.cargarDatosAdmin()
    }

    Scaffold(
        topBar = {
            FixedActivityHeader(
                isRefreshing = uiState.cargando,
                onRefreshClick = {
                    viewModel.cargarDatosAdmin(forzarActualizacion = true)
                }
            )
        },
        bottomBar = {
            AdminBottomBar(
                selected = "actividad",
                onResumenClick = onResumenClick,
                onRankingClick = onRankingClick,
                onActividadClick = {},
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
            ActivitySummaryCard(
                totalUsuarios = uiState.usuariosMasActivos.size,
                criterioActividad = uiState.criterioActividad
            )

            Spacer(modifier = Modifier.height(18.dp))

            ActivityFilters(
                selected = uiState.criterioActividad,
                onActividadClick = {
                    viewModel.cambiarCriterioActividad(CriterioActividad.ACTIVIDAD)
                },
                onRachaClick = {
                    viewModel.cambiarCriterioActividad(CriterioActividad.RACHA)
                },
                onCumplimientoClick = {
                    viewModel.cambiarCriterioActividad(CriterioActividad.CUMPLIMIENTO)
                }
            )

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Detalle de actividad",
                color = TextMain,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta el avance y participación de los usuarios.",
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.usuariosMasActivos.isEmpty()) {
                EmptyActivityCard()
            } else {
                uiState.usuariosMasActivos.forEach { usuario ->
                    ActiveUserCard(
                        usuario = usuario,
                        criterioActividad = uiState.criterioActividad
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ActivityStatusBarColor() {
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
private fun FixedActivityHeader(
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
                .padding(
                    start = 22.dp,
                    end = 22.dp,
                    top = 36.dp,
                    bottom = 21.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Actividad",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isRefreshing) {
                        "Actualizando datos..."
                    } else {
                        "Seguimiento de usuarios"
                    },
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }

            Surface(
                modifier = Modifier.size(46.dp),
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
                            contentDescription = "Actualizar actividad",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityFilters(
    selected: CriterioActividad,
    onActividadClick: () -> Unit,
    onRachaClick: () -> Unit,
    onCumplimientoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActivityFilterChip(
            text = "Actividad",
            selected = selected == CriterioActividad.ACTIVIDAD,
            onClick = onActividadClick
        )

        ActivityFilterChip(
            text = "Racha",
            selected = selected == CriterioActividad.RACHA,
            onClick = onRachaClick
        )

        ActivityFilterChip(
            text = "Cumplimiento",
            selected = selected == CriterioActividad.CUMPLIMIENTO,
            onClick = onCumplimientoClick
        )
    }
}

@Composable
private fun ActivityFilterChip(
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
private fun ActivitySummaryCard(
    totalUsuarios: Int,
    criterioActividad: CriterioActividad
) {
    val titulo = when (criterioActividad) {
        CriterioActividad.ACTIVIDAD -> "Usuarios más activos"
        CriterioActividad.RACHA -> "Usuarios con mayor racha"
        CriterioActividad.CUMPLIMIENTO -> "Mayor cumplimiento diario"
    }

    val subtitulo = when (criterioActividad) {
        CriterioActividad.ACTIVIDAD -> "Ordenados por último acceso"
        CriterioActividad.RACHA -> "Ordenados por racha actual"
        CriterioActividad.CUMPLIMIENTO -> "Ordenados por porcentaje de progreso"
    }

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
                            imageVector = Icons.Filled.TrendingUp,
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
                        text = titulo,
                        color = TextMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitulo,
                        color = TextMuted,
                        fontSize = 13.sp,
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
private fun ActiveUserCard(
    usuario: AdminUsuarioDto,
    criterioActividad: CriterioActividad
) {
    val fechaTexto = if (usuario.ultimoAcceso > 0L) {
        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(usuario.ultimoAcceso))
    } else {
        "Sin actividad"
    }

    val progreso = (usuario.porcentajeProgreso / 100f).coerceIn(0f, 1f)

    val avatarText = usuario.nombre
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { palabra ->
            palabra.first().uppercase()
        }
        .ifBlank { "U" }

    val metricaPrincipal = when (criterioActividad) {
        CriterioActividad.ACTIVIDAD -> "Último acceso: $fechaTexto"
        CriterioActividad.RACHA -> "Racha actual: ${usuario.rachaActual} días"
        CriterioActividad.CUMPLIMIENTO -> "Cumplimiento: ${usuario.porcentajeProgreso}%"
    }

    val etiquetaEstado = when {
        criterioActividad == CriterioActividad.ACTIVIDAD && usuario.activo -> "Activo"
        criterioActividad == CriterioActividad.ACTIVIDAD && !usuario.activo -> "Inactivo"
        criterioActividad == CriterioActividad.RACHA -> "${usuario.rachaActual} días"
        else -> "${usuario.porcentajeProgreso}%"
    }

    val estadoColor = when {
        criterioActividad == CriterioActividad.ACTIVIDAD && usuario.activo -> GreenMain
        criterioActividad == CriterioActividad.ACTIVIDAD && !usuario.activo -> GrayMain
        criterioActividad == CriterioActividad.RACHA -> OrangeMain
        else -> PrimaryBlue
    }

    val estadoBackground = when {
        criterioActividad == CriterioActividad.ACTIVIDAD && usuario.activo -> GreenSoft
        criterioActividad == CriterioActividad.ACTIVIDAD && !usuario.activo -> GraySoft
        criterioActividad == CriterioActividad.RACHA -> OrangeSoft
        else -> PrimaryBlueSoft
    }

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
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
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

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = usuario.nombre,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = metricaPrincipal,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = estadoBackground
                ) {
                    Text(
                        text = etiquetaEstado,
                        color = estadoColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryBlueExtraSoft,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityMiniData(
                    label = "Racha",
                    value = "${usuario.rachaActual} días"
                )

                ActivityMiniData(
                    label = "Palabras",
                    value = "${usuario.palabrasAprendidas}"
                )

                ActivityMiniData(
                    label = "Quizzes",
                    value = "${usuario.quizzesRealizados}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progreso",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${usuario.porcentajeProgreso}%",
                    color = estadoColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = estadoColor,
                trackColor = Color(0xFFE8EDF5)
            )
        }
    }
}

@Composable
private fun ActivityMiniData(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = TextMain,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyActivityCard() {
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
                        imageVector = Icons.Filled.PersonOff,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No hay actividad registrada",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cuando los usuarios interactúen con la app, su actividad aparecerá aquí.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
