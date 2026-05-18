package com.jp.widgetenglish.features.admin

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.jp.widgetenglish.features.admin.components.AdminBottomBar

private val BackgroundSoft = Color(0xFFF5F7FB)

private val HeaderBlue = Color(0xFF2468D8)
private val HeaderBlueDark = Color(0xFF1554BE)
private val PrimaryBlue = Color(0xFF2468D8)
private val PrimaryBlueSoft = Color(0xFFE3F1FF)
private val PrimaryBlueExtraSoft = Color(0xFFF0F7FF)

private val CardWhite = Color.White
private val TextMain = Color(0xFF1F2A60)
private val TextMuted = Color(0xFF7B8190)
private val BorderSoft = Color(0xFFE2E8F0)

private val OrangeSoft = Color(0xFFFFF1DF)
private val OrangeMain = Color(0xFFFF7A00)

private val GreenSoft = Color(0xFFEAF7EC)
private val GreenMain = Color(0xFF2E9D4D)

private val PurpleSoft = Color(0xFFEDEBFF)
private val PurpleMain = Color(0xFF5B5EDB)

private val RedSoft = Color(0xFFFFEBEE)
private val RedMain = Color(0xFFC62828)

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onRankingClick: () -> Unit,
    onActividadClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminStatusBarColor()

    LaunchedEffect(Unit) {
        viewModel.cargarDatosAdmin()
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showConstructionDialog by remember { mutableStateOf(false) }
    var constructionTitle by remember { mutableStateOf("") }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onCerrarSesionClick()
            }
        )
    }

    if (showConstructionDialog) {
        ConstructionDialog(
            title = constructionTitle,
            onDismiss = { showConstructionDialog = false }
        )
    }

    Scaffold(
        bottomBar = {
            AdminBottomBar(
                selected = "resumen",
                onResumenClick = {},
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
                .background(BackgroundSoft)
                .verticalScroll(rememberScrollState())
        ) {
            AdminHeader()

            Spacer(modifier = Modifier.height(22.dp))

            if (uiState.cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryBlue
                    )
                }
            }

            uiState.error?.let { error ->
                ErrorCard(error = error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            AdminSummaryCard(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(
                title = "Panel de gestión",
                subtitle = "Accesos rápidos para administrar la app"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AdminActionCard(
                    title = "Usuarios",
                    subtitle = "Actividad reciente",
                    icon = Icons.Filled.Groups,
                    background = PrimaryBlueSoft,
                    iconColor = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onActividadClick
                )

                AdminActionCard(
                    title = "Ranking",
                    subtitle = "Participación",
                    icon = Icons.Filled.BarChart,
                    background = PurpleSoft,
                    iconColor = PurpleMain,
                    modifier = Modifier.weight(1f),
                    onClick = onRankingClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AdminActionCard(
                    title = "Vocabulario",
                    subtitle = "En construcción",
                    icon = Icons.Filled.Book,
                    background = OrangeSoft,
                    iconColor = OrangeMain,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        constructionTitle = "Vocabulario"
                        showConstructionDialog = true
                    }
                )

                AdminActionCard(
                    title = "Lotes",
                    subtitle = "En construcción",
                    icon = Icons.Filled.Layers,
                    background = GreenSoft,
                    iconColor = GreenMain,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        constructionTitle = "Lotes"
                        showConstructionDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RecentActivityPreviewCard(uiState = uiState)

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cerrar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
private fun AdminStatusBarColor() {
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
private fun AdminHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(285.dp)
            .clip(RoundedCornerShape(bottomStart = 42.dp, bottomEnd = 42.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HeaderBlue,
                        HeaderBlueDark
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(82.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "¡Hola, Administrador!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Panel de control de WidgetEnglish",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AdminSummaryCard(
    uiState: AdminUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = PrimaryBlueSoft
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Dashboard,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Resumen administrativo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Text(
                        text = "Vista general de la actividad",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMiniStat(
                    value = "${uiState.totalUsuarios}",
                    label = "Usuarios",
                    icon = Icons.Filled.Groups,
                    background = PrimaryBlueSoft,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )

                AdminMiniStat(
                    value = "${uiState.totalPalabrasAprendidas}",
                    label = "Aprendidas",
                    icon = Icons.Filled.Book,
                    background = GreenSoft,
                    color = GreenMain,
                    modifier = Modifier.weight(1f)
                )

                AdminMiniStat(
                    value = "${uiState.totalQuizzesRealizados}",
                    label = "Quizzes",
                    icon = Icons.Filled.BarChart,
                    background = OrangeSoft,
                    color = OrangeMain,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMiniStat(
                    value = "${uiState.usuariosActivos}",
                    label = "Activos",
                    icon = Icons.Filled.Groups,
                    background = PrimaryBlueExtraSoft,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )

                AdminMiniStat(
                    value = "${uiState.totalLotesCompletados}",
                    label = "Lotes",
                    icon = Icons.Filled.Layers,
                    background = PurpleSoft,
                    color = PurpleMain,
                    modifier = Modifier.weight(1f)
                )

                AdminMiniStat(
                    value = "${uiState.rankingUsuarios.size}",
                    label = "Ranking",
                    icon = Icons.Filled.Dashboard,
                    background = PrimaryBlueSoft,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun RecentActivityPreviewCard(
    uiState: AdminUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = PrimaryBlueSoft
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Report,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Actividad reciente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Text(
                        text = "Usuarios más activos",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.usuariosMasActivos.isEmpty()) {
                EmptyActivityMessage()
            } else {
                uiState.usuariosMasActivos.take(3).forEachIndexed { index, usuario ->
                    ActivityUserRow(
                        position = index + 1,
                        name = usuario.nombre,
                        words = usuario.palabrasAprendidas,
                        streak = usuario.rachaActual
                    )

                    if (index < uiState.usuariosMasActivos.take(3).lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityUserRow(
    position: Int,
    name: String,
    words: Int,
    streak: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = PrimaryBlueExtraSoft,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = PrimaryBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$position",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$words aprendidas · racha $streak",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EmptyActivityMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = PrimaryBlueExtraSoft,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay actividad registrada todavía.",
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdminMiniStat(
    value: String,
    label: String,
    icon: ImageVector,
    background: Color,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(112.dp)
            .background(
                color = background,
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.18f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = value,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    background: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(132.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.72f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = iconColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = iconColor.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ErrorCard(
    error: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RedSoft
        )
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(16.dp),
            color = RedMain,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = PrimaryBlueSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "¿Cerrar sesión?",
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        },
        text = {
            Text(
                text = "¿Deseas cerrar tu sesión de administrador?",
                textAlign = TextAlign.Center,
                color = TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun ConstructionDialog(
    title: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = OrangeSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = null,
                        tint = OrangeMain,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "$title en construcción",
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Esta sección todavía se está preparando. Pronto estará disponible.",
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Entendido")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}