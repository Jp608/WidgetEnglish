package com.widgetenglish.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.repository.auth.AuthRepositoryImpl
import com.jp.widgetenglish.features.auth.LoginScreen
import com.jp.widgetenglish.features.auth.RegisterScreen
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModelFactory
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModelFactory
import com.widgetenglish.app.ui.auth.ForgotPasswordScreen
import com.widgetenglish.app.ui.home.HomeScreen
import com.widgetenglish.app.ui.auth.VerifyResetCodeScreen
import com.widgetenglish.app.ui.auth.NewPasswordScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Inicialización de dependencias
    val database = DatabaseProvider.getDatabase(context)
    val authRepository = AuthRepositoryImpl(
        firebaseAuth = FirebaseAuth.getInstance()
    )
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, database.usuarioDao())
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(authRepository, database.usuarioDao())
    )

    val authUiState by authViewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(authUiState.autenticado) {
        if (!authUiState.autenticado) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Screen.VerifyResetCode.route) {
            VerifyResetCodeScreen(navController = navController)
        }
        composable(Screen.NewPassword.route) {
            NewPasswordScreen(navController = navController)
        }
    }
}
