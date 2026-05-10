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
fun ForgotPasswordScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isEmailSent by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Te ayudamos a recuperarla",
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

                    if (!isEmailSent) {

                        Text(
                            text = "Recuperar contraseña",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A237E)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ingresa tu correo y te enviaremos un código para restablecer tu contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null,
                                    tint = Color(0xFF1565C0))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message,
                                color = if (isSuccess)
                                    Color(0xFF2E7D32)
                                else
                                    MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    message = "Por favor ingresa tu correo"
                                    isSuccess = false
                                } else {
                                    // Firebase Auth se conectará aquí
                                    navController.navigate(Screen.VerifyResetCode.route)
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
                            Text("Enviar enlace", fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                        }

                    } else {

                        // ─── PANTALLA DE CONFIRMACIÓN ────────────
                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            imageVector = Icons.Filled.MarkEmailRead,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "¡Correo enviado!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contraseña.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { isEmailSent = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0)
                            )
                        ) {
                            Text("Reenviar correo", fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Volver al inicio de sesión",
                            color = Color(0xFF1565C0), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}