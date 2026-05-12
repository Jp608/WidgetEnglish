package com.jp.widgetenglish.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminDashboardScreen(
    onCerrarSesionClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFF1565C0)
                )
            },
            title = {
                Text(
                    text = "¿Cerrar sesión?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas cerrar tu sesión de administrador?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onCerrarSesionClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }

    Scaffold(
        bottomBar = {
            AdminBottomBar(
                selected = "dashboard",
                onDashboardClick = {},
                onUsuariosClick = {},
                onStatsClick = {},
                onVocabularioClick = {},
                onPerfilClick = {}
            )
        },
        containerColor = Color(0xFFF5F7FB)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(Color(0xFFF5F7FB))
                .verticalScroll(rememberScrollState())
        ) {
            AdminHeader()

            Spacer(modifier = Modifier.height(24.dp))

            AdminSummaryCard()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Panel de gestión",
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
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
                    subtitle = "Gestión y actividad",
                    icon = Icons.Filled.Groups,
                    background = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )

                AdminActionCard(
                    title = "Estadísticas",
                    subtitle = "Resumen general",
                    icon = Icons.Filled.BarChart,
                    background = Color(0xFFE8EAF6),
                    iconColor = Color(0xFF3949AB),
                    modifier = Modifier.weight(1f),
                    onClick = {}
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
                    subtitle = "Palabras y verbos",
                    icon = Icons.Filled.Book,
                    background = Color(0xFFE0F7FA),
                    iconColor = Color(0xFF00838F),
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )

                AdminActionCard(
                    title = "Lotes",
                    subtitle = "Contenido agrupado",
                    icon = Icons.Filled.Layers,
                    background = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF8E24AA),
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
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
                        Icon(
                            imageVector = Icons.Filled.Report,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Próximas funciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Aquí se mostrarán usuarios activos, ranking, palabras más falladas, progreso por lote y estadísticas generales de aprendizaje.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
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
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(265.dp)
            .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D47A1),
                        Color(0xFF1565C0),
                        Color(0xFF039BE5)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White.copy(alpha = 0.20f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡Hola, Administrador! 👋",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Panel de control de WidgetEnglish",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AdminSummaryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Dashboard,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Resumen administrativo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminMiniStat(
                    value = "0",
                    label = "Usuarios",
                    icon = Icons.Filled.Groups
                )

                AdminMiniStat(
                    value = "0",
                    label = "Palabras",
                    icon = Icons.Filled.Book
                )

                AdminMiniStat(
                    value = "0",
                    label = "Lotes",
                    icon = Icons.Filled.Layers
                )
            }
        }
    }
}

@Composable
private fun AdminMiniStat(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1565C0),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
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
            .height(122.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = iconColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = iconColor.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AdminBottomBar(
    selected: String,
    onDashboardClick: () -> Unit,
    onUsuariosClick: () -> Unit,
    onStatsClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminBottomItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            selected = selected == "dashboard",
            onClick = onDashboardClick
        )

        AdminBottomItem(
            title = "Usuarios",
            icon = Icons.Filled.Groups,
            selected = selected == "usuarios",
            onClick = onUsuariosClick
        )

        AdminBottomItem(
            title = "Stats",
            icon = Icons.Filled.BarChart,
            selected = selected == "stats",
            onClick = onStatsClick
        )

        AdminBottomItem(
            title = "Vocab.",
            icon = Icons.Filled.Book,
            selected = selected == "vocabulario",
            onClick = onVocabularioClick
        )

        AdminBottomItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            selected = selected == "perfil",
            onClick = onPerfilClick
        )
    }
}

@Composable
private fun AdminBottomItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF1565C0) else Color(0xFFC2C5D1)
    val background = if (selected) Color(0xFFE3F2FD) else Color.Transparent

    Column(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}