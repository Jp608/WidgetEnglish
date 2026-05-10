package com.widgetenglish.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jp.widgetenglish.features.profile.ProfileScreen
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel

sealed class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    object Home       : BottomNavItem("Inicio",      Icons.Filled.Home,                       "home_tab")
    object Vocabulary : BottomNavItem("Vocabulario", Icons.AutoMirrored.Filled.MenuBook,       "vocabulary")
    object Lots       : BottomNavItem("Lotes",       Icons.Filled.Layers,                     "lots")
    object Study      : BottomNavItem("Estudio",     Icons.Filled.School,                     "study")
    object AIChat     : BottomNavItem("IA",          Icons.Filled.SmartToy,                   "ai_chat")
    object Profile    : BottomNavItem("Perfil",      Icons.Filled.Person,                     "profile")
}

@Composable
fun HomeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    authViewModel: com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Vocabulary,
        BottomNavItem.Lots,
        BottomNavItem.Study,
        BottomNavItem.AIChat,
        BottomNavItem.Profile
    )

    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1565C0),
                            selectedTextColor = Color(0xFF1565C0),
                            indicatorColor = Color(0xFFE3F2FD)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeContent()
                1 -> PlaceholderScreen("Vocabulario", Icons.AutoMirrored.Filled.MenuBook)
                2 -> PlaceholderScreen("Lotes", Icons.Filled.Layers)
                3 -> PlaceholderScreen("Estudio", Icons.Filled.School)
                4 -> PlaceholderScreen("IA Chat", Icons.Filled.SmartToy)
                5 -> ProfileScreen(
                    navController = navController,
                    viewModel = profileViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

@Composable
fun HomeContent() {

    val blueGradient = listOf(
        Color(0xFF1A237E),
        Color(0xFF1565C0),
        Color(0xFF0288D1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // ─── FRANJA SUPERIOR ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
                .background(brush = Brush.verticalGradient(colors = blueGradient))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    text = "¡Hola! 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sigue aprendiendo cada día",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = "Racha",
                        tint = Color(0xFFFFCC02),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0 días de racha",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // ─── LOTE ACTIVO ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Layers,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lote activo",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sin lote activo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF1565C0),
                        trackColor = Color(0xFFE3F2FD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "0 / 0 palabras",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ─── OBJETIVO DIARIO ──────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.TrackChanges,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Objetivo diario",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ver 10 palabras",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF0288D1),
                        trackColor = Color(0xFFE3F2FD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "0 / 10 completadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ─── ACCESOS RÁPIDOS ──────────────────────────
            Text(
                text = "Accesos rápidos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    label = "Vocabulario",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    color = Color(0xFFE8EAF6),
                    iconColor = Color(0xFF3949AB),
                    modifier = Modifier.weight(1f)
                )
                QuickCard(
                    label = "Lotes",
                    icon = Icons.Filled.Layers,
                    color = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    label = "Estudio",
                    icon = Icons.Filled.School,
                    color = Color(0xFFE0F7FA),
                    iconColor = Color(0xFF00838F),
                    modifier = Modifier.weight(1f)
                )
                QuickCard(
                    label = "IA Chat",
                    icon = Icons.Filled.SmartToy,
                    color = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF7B1FA2),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickCard(
    label: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(85.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = iconColor
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF1565C0).copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Próximamente...",
                color = Color.Gray
            )
        }
    }
}