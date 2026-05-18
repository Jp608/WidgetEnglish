package com.widgetenglish.app.ui
import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.features.admin.AdminDashboardScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.jp.widgetenglish.data.repository.auth.AuthRepositoryImpl
import com.jp.widgetenglish.features.auth.LoginScreen
import com.jp.widgetenglish.features.auth.RegisterScreen
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModelFactory
import com.jp.widgetenglish.features.home.presentation.screens.HomeScreen
import com.jp.widgetenglish.features.home.presentation.viewmodel.HomeViewModel
import com.jp.widgetenglish.features.home.presentation.viewmodel.HomeViewModelFactory
import com.jp.widgetenglish.features.profile.ProfileScreen
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModelFactory
import com.widgetenglish.app.ui.auth.ForgotPasswordScreen
import com.widgetenglish.app.ui.auth.NewPasswordScreen
import com.widgetenglish.app.ui.auth.VerifyResetCodeScreen
import com.jp.widgetenglish.features.common.ConstructionScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.VocabularyScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.VocabularyDetailScreen
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModelFactory

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = DatabaseProvider.getDatabase(context)
    val authRepository = AuthRepositoryImpl(
        firebaseAuth = FirebaseAuth.getInstance()
    )
    val usuarioFirestoreDataSource = UsuarioFirestoreDataSource(
        firestore = FirebaseFirestore.getInstance()
    )

    val vocabularioRepository = VocabularioRepositoryImpl(
        palabraDao = database.palabraDao(),
        verboDao = database.verboDao(),
        loteDao = database.loteDao(),
        progresoDao = database.progresoDao()
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = authRepository,
            usuarioDao = database.usuarioDao(),
            usuarioFirestoreDataSource = usuarioFirestoreDataSource
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            authRepository = authRepository,
            usuarioDao = database.usuarioDao()
        )
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            repository = vocabularioRepository,
            authRepository = authRepository,
            usuarioDao = database.usuarioDao()
        )
    )

    val vocabularyViewModel: VocabularyViewModel = viewModel(
        factory = VocabularyViewModelFactory(
            repository = vocabularioRepository,
            authRepository = authRepository,
            usuarioFirestoreDataSource = usuarioFirestoreDataSource
        )
    )


    val authUiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authUiState.autenticado, authUiState.rolUsuario) {
        if (authUiState.autenticado) {
            val destino = if (authUiState.rolUsuario == RolUsuario.ADMIN) {
                Screen.AdminDashboard.route
            } else {
                Screen.Home.route
            }

            navController.navigate(destino) {
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }
    fun navegar(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    )  {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onVocabularioClick = { navegar(Screen.Vocabulario.route) },
                onLotesClick = { navegar(Screen.Lotes.route) },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) },
                onPerfilClick = { navegar(Screen.Profile.route) }
            )
        }

        composable(Screen.Vocabulario.route) {
            VocabularyScreen(
                viewModel = vocabularyViewModel,
                onBackClick = { navegar(Screen.Home.route) },
                onVocabularioClick = { navegar(Screen.Vocabulario.route) },
                onLotesClick = { navegar(Screen.Lotes.route) },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) },
                onPerfilClick = { navegar(Screen.Profile.route) },
                onItemClick = { item ->
                    navController.navigate(Screen.VocabularyDetail.createRoute(item.id, item.esVerbo))
                }
            )
        }

        composable(Screen.VocabularyDetail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val isVerbo = backStackEntry.arguments?.getString("isVerbo")?.toBoolean() ?: false
            VocabularyDetailScreen(
                itemId = itemId,
                isVerbo = isVerbo,
                viewModel = vocabularyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Lotes.route) {
            ConstructionScreen(
                titulo = "Lotes en construcción",
                descripcion = "Aquí podrás ver y activar tus lotes de aprendizaje.",
                onVolverInicio = { navegar(Screen.Home.route) },
                onPerfilClick = { navegar(Screen.Profile.route) },
                onVocabularioClick = { navegar(Screen.Vocabulario.route) },
                onLotesClick = { navegar(Screen.Lotes.route) },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) }
            )
        }

        composable(Screen.Estudio.route) {
            ConstructionScreen(
                titulo = "Modo estudio en construcción",
                descripcion = "Aquí estudiarás el vocabulario del lote activo con tarjetas.",
                onVolverInicio = { navegar(Screen.Home.route) },
                onPerfilClick = { navegar(Screen.Profile.route) },
                onVocabularioClick = { navegar(Screen.Vocabulario.route) },
                onLotesClick = { navegar(Screen.Lotes.route) },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) }
            )
        }

        composable(Screen.Ia.route) {
            ConstructionScreen(
                titulo = "IA Chat en construcción",
                descripcion = "Aquí podrás consultar dudas de vocabulario y recibir ejemplos.",
                onVolverInicio = { navegar(Screen.Home.route) },
                onPerfilClick = { navegar(Screen.Profile.route) },
                onVocabularioClick = { navegar(Screen.Vocabulario.route) },
                onLotesClick = { navegar(Screen.Lotes.route) },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.VerifyResetCode.route) {
            VerifyResetCodeScreen(
                navController = navController
            )
        }

        composable(Screen.NewPassword.route) {
            NewPasswordScreen(
                navController = navController
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onCerrarSesionClick = {
                    authViewModel.cerrarSesion()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}