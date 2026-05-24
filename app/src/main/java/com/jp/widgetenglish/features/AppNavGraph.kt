package com.widgetenglish.app.ui

import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.features.admin.AdminDashboardScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.jp.widgetenglish.features.vocabulary.presentation.screens.LoteDetailScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.LotesScreen
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.QuizViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.QuizViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.StudyViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.StudyViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.screens.QuizScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.QuizResultScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.StudyDashboardScreen
import com.jp.widgetenglish.data.remote.firestore.AdminFirestoreDataSource
import com.jp.widgetenglish.features.admin.AdminViewModel
import com.jp.widgetenglish.features.admin.AdminViewModelFactory
import com.jp.widgetenglish.features.admin.ranking.AdminRankingScreen
import com.jp.widgetenglish.features.admin.activity.AdminActivityScreen
import com.jp.widgetenglish.features.admin.profile.AdminProfileScreen

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

    val adminFirestoreDataSource = AdminFirestoreDataSource(
        firestore = FirebaseFirestore.getInstance()
    )

    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(
            adminFirestoreDataSource = adminFirestoreDataSource
        )
    )

    val vocabularioRepository = VocabularioRepositoryImpl(
        palabraDao = database.palabraDao(),
        verboDao = database.verboDao(),
        loteDao = database.loteDao(),
        progresoDao = database.progresoDao(),
        usuarioFirestoreDataSource = usuarioFirestoreDataSource
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = authRepository,
            usuarioDao = database.usuarioDao(),
            usuarioFirestoreDataSource = usuarioFirestoreDataSource,
            vocabularioRepository = vocabularioRepository,
            context = context.applicationContext
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

    val lotesViewModel: LotesViewModel = viewModel(
        factory = LotesViewModelFactory(
            vocabularioRepository = vocabularioRepository,
            authRepository = authRepository
        )
    )

    val quizViewModel: QuizViewModel = viewModel(
        factory = QuizViewModelFactory(
            vocabularioRepository = vocabularioRepository,
            authRepository = authRepository
        )
    )

    val studyViewModel: StudyViewModel = viewModel(
        factory = StudyViewModelFactory(
            vocabularioRepository = vocabularioRepository,
            authRepository = authRepository
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
    ) {
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
                onVocabularioClick = { 
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Vocabulario.route) 
                },
                onLotesClick = { 
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Lotes.route) 
                },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) },
                onPerfilClick = { navegar(Screen.Profile.route) },
                onItemClick = { item ->
                    navController.navigate(
                        Screen.VocabularyDetail.createRoute(
                            item.id,
                            item.esVerbo
                        )
                    )
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
            LotesScreen(
                viewModel = lotesViewModel,
                onBackClick = { navController.popBackStack() },
                onInicioClick = { navegar(Screen.Home.route) },
                onVocabularioClick = { 
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Vocabulario.route) 
                },
                onLotesClick = { 
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Lotes.route) 
                },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) },
                onPerfilClick = { navegar(Screen.Profile.route) },
                onVerContenido = { loteId ->
                    navController.navigate(Screen.LoteDetail.createRoute(loteId))
                }
            )
        }

        composable(Screen.LoteDetail.route) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getString("loteId") ?: ""
            LoteDetailScreen(
                loteId = loteId,
                viewModel = lotesViewModel,
                onBack = { navController.popBackStack() },
                onItemClick = { itemId, isVerbo ->
                    navController.navigate(Screen.VocabularyDetail.createRoute(itemId, isVerbo))
                },
                onEstudiarClick = { id ->
                    navController.navigate(Screen.Quiz.createRoute(id))
                }
            )
        }

        composable(Screen.Quiz.route) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getString("loteId") ?: ""
            val repasarFalladas = backStackEntry.arguments?.getString("repasarFalladas").toBoolean()
            val limite = backStackEntry.arguments?.getString("limite")?.toIntOrNull() ?: 10
            
            // Obtenemos el estado actual del quizViewModel antes de que se limpie (si es posible)
            // o simplemente confiamos en que el ViewModel ya tiene la info si no ha sido destruido.
            // Pero para ser más robustos, podemos pasar los IDs fallados como argumento de navegación si fuera necesario.
            // Por ahora, vamos a forzar que el ViewModel use lo que tiene en memoria antes de resetear.
            
            QuizScreen(
                loteId = loteId,
                repasarFalladas = repasarFalladas,
                limite = limite,
                viewModel = quizViewModel,
                onBack = { navController.popBackStack() },
                onFinish = { _, _, _ ->
                    navController.navigate(Screen.QuizResult.route) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QuizResult.route) {
            val quizState by quizViewModel.uiState.collectAsState()
            val failedIds = remember { quizState.respuestasFalladas.map { it.id } }
            
            QuizResultScreen(
                score = quizState.score,
                total = quizState.preguntas.size,
                failedWords = quizState.respuestasFalladas,
                onRepasarFalladas = {
                    // Al navegar, pasamos la información de que queremos repasar Y los IDs
                    // Pero nuestra ruta actual no soporta pasar una lista de IDs fácilmente.
                    // Vamos a disparar el inicio del quiz en el ViewModel justo ANTES de navegar
                    // para que el estado ya esté preparado.
                    quizViewModel.iniciarQuiz(quizState.loteId, true, failedIds)
                    
                    navController.navigate(Screen.Quiz.createRoute(quizState.loteId, true)) {
                        popUpTo(Screen.QuizResult.route) { inclusive = true }
                    }
                },
                onRepetirQuiz = {
                    quizViewModel.iniciarQuiz(quizState.loteId, false)

                    navController.navigate(Screen.Quiz.createRoute(quizState.loteId, false)) {
                        popUpTo(Screen.QuizResult.route) { inclusive = true }
                    }
                },
                onVolverInicio = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Estudio.route) {
            StudyDashboardScreen(
                viewModel = studyViewModel,
                onBack = { navController.popBackStack() },
                onStartQuiz = { loteId, limite ->
                    quizViewModel.iniciarQuiz(loteId, false, limite = limite)
                    navController.navigate(Screen.Quiz.createRoute(loteId, false, limite))
                }
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
                viewModel = adminViewModel,
                onRankingClick = {
                    navController.navigate(Screen.AdminRanking.route) {
                        launchSingleTop = true
                    }
                },
                onActividadClick = {
                    navController.navigate(Screen.AdminActivity.route) {
                        launchSingleTop = true
                    }
                },
                onPerfilClick = {
                    navController.navigate(Screen.AdminProfile.route) {
                        launchSingleTop = true
                    }
                },
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

        composable(Screen.AdminRanking.route) {
            AdminRankingScreen(
                viewModel = adminViewModel,
                onResumenClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        launchSingleTop = true
                    }
                },
                onActividadClick = {
                    navController.navigate(Screen.AdminActivity.route) {
                        launchSingleTop = true
                    }
                },
                onPerfilClick = {
                    navController.navigate(Screen.AdminProfile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.AdminActivity.route) {
            AdminActivityScreen(
                viewModel = adminViewModel,
                onResumenClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        launchSingleTop = true
                    }
                },
                onRankingClick = {
                    navController.navigate(Screen.AdminRanking.route) {
                        launchSingleTop = true
                    }
                },
                onPerfilClick = {
                    navController.navigate(Screen.AdminProfile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.AdminProfile.route) {
            AdminProfileScreen(
                profileViewModel = profileViewModel,
                onResumenClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        launchSingleTop = true
                    }
                },
                onRankingClick = {
                    navController.navigate(Screen.AdminRanking.route) {
                        launchSingleTop = true
                    }
                },
                onActividadClick = {
                    navController.navigate(Screen.AdminActivity.route) {
                        launchSingleTop = true
                    }
                },
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
