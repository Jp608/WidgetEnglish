package com.widgetenglish.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.widgetenglish.app.ui.Screen

@Composable
fun VerifyResetCodeScreen(navController: NavController) {

    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var resendMessage by remember { mutableStateOf("") }

    val blueGradient = listOf(
        Color(0xFF1A237E),
        Color(0xFF1565C0),
        Color(0xFF0288D1)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // ─── FRANJA SEMICIRCULAR SUPERIOR ───────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
                .background(brush = Brush.verticalGradient(colors = blueGradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.MarkEmailRead,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Código de verificación",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Revisa tu bandeja de entrada",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // ─── CONTENIDO PRINCIPAL ─────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Ingresa el código",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A237E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Te enviamos un código de 6 dígitos a tu correo electrónico.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = { Text("Código de 6 dígitos") },
                        leadingIcon = {
                            Icon(Icons.Filled.Password, contentDescription = null,
                                tint = Color(0xFF1565C0))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                            letterSpacing = 8.sp
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (resendMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resendMessage,
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                code.isBlank() ->
                                    errorMessage = "Por favor ingresa el código"
                                code.length < 6 ->
                                    errorMessage = "El código debe tener 6 dígitos"
                                else -> {
                                    // Firebase verificará el código aquí
                                    navController.navigate(Screen.NewPassword.route)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        Text("Verificar código", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            // Firebase reenviará el código aquí
                            resendMessage = "Código reenviado. Revisa tu correo."
                            errorMessage = ""
                        }
                    ) {
                        Text("¿No recibiste el código? ",
                            color = Color.Gray, fontSize = 13.sp)
                        Text("Reenviar",
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp)
                    }

                    TextButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Volver", color = Color(0xFF1565C0), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}