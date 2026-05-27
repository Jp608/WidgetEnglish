package com.widgetenglish.app.ui

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
import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.remote.firestore.AdminFirestoreDataSource
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.jp.widgetenglish.data.repository.auth.AuthRepositoryImpl
import com.jp.widgetenglish.features.admin.AdminDashboardScreen
import com.jp.widgetenglish.features.admin.AdminViewModel
import com.jp.widgetenglish.features.admin.AdminViewModelFactory
import com.jp.widgetenglish.features.admin.activity.AdminActivityScreen
import com.jp.widgetenglish.features.admin.profile.AdminProfileScreen
import com.jp.widgetenglish.features.admin.ranking.AdminRankingScreen
import com.jp.widgetenglish.features.auth.LoginScreen
import com.jp.widgetenglish.features.auth.RegisterScreen
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModelFactory
import com.jp.widgetenglish.features.common.ConstructionScreen
import com.jp.widgetenglish.features.home.presentation.screens.HomeScreen
import com.jp.widgetenglish.features.home.presentation.viewmodel.HomeViewModel
import com.jp.widgetenglish.features.home.presentation.viewmodel.HomeViewModelFactory
import com.jp.widgetenglish.features.profile.ProfileScreen
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.screens.LoteDetailScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.LotesScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.QuizResultScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.QuizScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.StudyDashboardScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.StudyModeScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.VocabularyDetailScreen
import com.jp.widgetenglish.features.vocabulary.presentation.screens.VocabularyScreen
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.LotesViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.QuizViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.QuizViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.StudyViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.StudyViewModelFactory
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.VocabularyViewModelFactory
import com.widgetenglish.app.ui.auth.ForgotPasswordScreen
import com.widgetenglish.app.ui.auth.NewPasswordScreen
import com.widgetenglish.app.ui.auth.VerifyResetCodeScreen
import com.jp.widgetenglish.features.vocabulary.presentation.cards.screens.CardsConfigScreen
import com.jp.widgetenglish.features.vocabulary.presentation.cards.screens.CardsPlanScreen
import com.jp.widgetenglish.features.vocabulary.presentation.cards.screens.CardsResultScreen
import com.jp.widgetenglish.features.vocabulary.presentation.cards.screens.CardsSessionScreen
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsViewModelFactory
import com.jp.widgetenglish.data.repository.StreakRepository
import com.jp.widgetenglish.data.remote.firestore.EstadisticasFirestoreDataSource
import com.jp.widgetenglish.features.profile.statistics.screens.StatisticsScreen
import com.jp.widgetenglish.features.profile.statistics.viewmodel.StatisticsViewModel
import com.jp.widgetenglish.features.profile.statistics.viewmodel.StatisticsViewModelFactory
import com.jp.widgetenglish.ai.ui.AiChatScreen

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

    val estadisticasFirestoreDataSource = EstadisticasFirestoreDataSource(
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

    val streakRepository = StreakRepository(
        actividadDiariaDao = database.actividadDiariaDao(),
        usuarioDao = database.usuarioDao(),
        progresoDao = database.progresoDao(),
        estadisticasFirestoreDataSource = estadisticasFirestoreDataSource
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = authRepository,
            usuarioDao = database.usuarioDao(),
            actividadDiariaDao = database.actividadDiariaDao(),
            usuarioFirestoreDataSource = usuarioFirestoreDataSource,
            estadisticasFirestoreDataSource = estadisticasFirestoreDataSource,
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

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(
            authRepository = authRepository,
            usuarioDao = database.usuarioDao(),
            actividadDiariaDao = database.actividadDiariaDao(),
            vocabularioRepository = vocabularioRepository
        )
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            repository = vocabularioRepository,
            authRepository = authRepository,
            usuarioDao = database.usuarioDao(),
            actividadDiariaDao = database.actividadDiariaDao()
        )
    )

    val vocabularyViewModel: VocabularyViewModel = viewModel(
        factory = VocabularyViewModelFactory(
            repository = vocabularioRepository,
            authRepository = authRepository,
            usuarioFirestoreDataSource = usuarioFirestoreDataSource,
            streakRepository = streakRepository
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
            authRepository = authRepository,
            usuarioDao = database.usuarioDao(),
            streakRepository = streakRepository
        )
    )

    val studyViewModel: StudyViewModel = viewModel(
        factory = StudyViewModelFactory(
            vocabularioRepository = vocabularioRepository,
            authRepository = authRepository,
            usuarioDao = database.usuarioDao()
        )
    )
    val cardsViewModel: CardsViewModel = viewModel(
        factory = CardsViewModelFactory(
            vocabularioRepository = vocabularioRepository,
            authRepository = authRepository,
            streakRepository = streakRepository
        )
    )

    val authUiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(
        authUiState.autenticado,
        authUiState.rolUsuario
    ) {
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
                onVocabularioClick = {
                    navegar(Screen.Vocabulario.route)
                },
                onLotesClick = {
                    navegar(Screen.Lotes.route)
                },
                onEstudioClick = {
                    navegar(Screen.Estudio.route)
                },
                onIaClick = {
                    navegar(Screen.Ia.route)
                },
                onPerfilClick = {
                    navegar(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Vocabulario.route) {
            VocabularyScreen(
                viewModel = vocabularyViewModel,
                onBackClick = {
                    navegar(Screen.Home.route)
                },
                onVocabularioClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Vocabulario.route)
                },
                onLotesClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Lotes.route)
                },
                onEstudioClick = {
                    navegar(Screen.Estudio.route)
                },
                onIaClick = {
                    navegar(Screen.Ia.route)
                },
                onPerfilClick = {
                    navegar(Screen.Profile.route)
                },
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
            val isVerbo = backStackEntry.arguments
                ?.getString("isVerbo")
                ?.toBoolean() ?: false

            VocabularyDetailScreen(
                itemId = itemId,
                isVerbo = isVerbo,
                viewModel = vocabularyViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Lotes.route) {
            LotesScreen(
                viewModel = lotesViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onInicioClick = {
                    navegar(Screen.Home.route)
                },
                onVocabularioClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Vocabulario.route)
                },
                onLotesClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Lotes.route)
                },
                onEstudioClick = {
                    navegar(Screen.Estudio.route)
                },
                onIaClick = {
                    navegar(Screen.Ia.route)
                },
                onPerfilClick = {
                    navegar(Screen.Profile.route)
                },
                onVerContenido = { loteId ->
                    navController.navigate(
                        Screen.LoteDetail.createRoute(loteId)
                    )
                }
            )
        }

        composable(Screen.LoteDetail.route) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getString("loteId") ?: ""

            LoteDetailScreen(
                loteId = loteId,
                viewModel = lotesViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onItemClick = { itemId, isVerbo ->
                    navController.navigate(
                        Screen.VocabularyDetail.createRoute(
                            itemId,
                            isVerbo
                        )
                    )
                },
                onEstudiarClick = { id ->
                    navController.navigate(
                        Screen.Quiz.createRoute(
                            loteId = id,
                            repasarFalladas = false,
                            limite = 10,
                            failedIds = emptyList()
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Quiz.route) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getString("loteId") ?: ""

            val repasarFalladas = backStackEntry.arguments
                ?.getString("repasarFalladas")
                .toBoolean()

            val limite = backStackEntry.arguments
                ?.getString("limite")
                ?.toIntOrNull()
                ?: 10

            val failedIdsText = backStackEntry.arguments
                ?.getString("failedIds")
                .orEmpty()

            val failedIds = if (failedIdsText.isBlank() || failedIdsText == "-") {
                emptyList()
            } else {
                failedIdsText
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            QuizScreen(
                loteId = loteId,
                repasarFalladas = repasarFalladas,
                failedIds = failedIds,
                limite = limite,
                viewModel = quizViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onFinish = { _, _, _ ->
                    navController.navigate(Screen.QuizResult.route) {
                        popUpTo(Screen.Quiz.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.QuizResult.route) {
            val quizState by quizViewModel.uiState.collectAsState()

            QuizResultScreen(
                score = quizState.score,
                total = quizState.preguntas.size,
                failedWords = quizState.respuestasFalladas,
                onRepasarFalladas = {
                    val idsFalladas = quizState.respuestasFalladas
                        .map { it.id }
                        .distinct()

                    android.util.Log.d(
                        "QuizDebug",
                        "Repasar falladas desde resultado. Cantidad=${idsFalladas.size}, ids=$idsFalladas"
                    )

                    navController.navigate(
                        Screen.Quiz.createRoute(
                            loteId = quizState.loteId,
                            repasarFalladas = true,
                            limite = idsFalladas.size,
                            failedIds = idsFalladas
                        )
                    ) {
                        popUpTo(Screen.QuizResult.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRepetirQuiz = {
                    navController.navigate(
                        Screen.Quiz.createRoute(
                            loteId = quizState.loteId,
                            repasarFalladas = false,
                            limite = quizState.preguntas.size,
                            failedIds = emptyList()
                        )
                    ) {
                        popUpTo(Screen.QuizResult.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onVolverInicio = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Estudio.route) {
            StudyModeScreen(
                viewModel = studyViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onOpenQuizConfig = {
                    navController.navigate(Screen.Study.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onOpenCards = {
                    navController.navigate(Screen.CardsPlan.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onInicioClick = {
                    navegar(Screen.Home.route)
                },
                onVocabularioClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Vocabulario.route)
                },
                onLotesClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Lotes.route)
                },
                onEstudioClick = {
                    navegar(Screen.Estudio.route)
                },
                onIaClick = {
                    navegar(Screen.Ia.route)
                },
                onPerfilClick = {
                    navegar(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Study.route) {
            StudyDashboardScreen(
                viewModel = studyViewModel,
                onBack = {
                    navController.navigate(Screen.Estudio.route) {
                        popUpTo(Screen.Study.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onStartQuiz = { loteId, limite ->
                    navController.navigate(
                        Screen.Quiz.createRoute(
                            loteId = loteId,
                            repasarFalladas = false,
                            limite = limite,
                            failedIds = emptyList()
                        )
                    ) {
                        popUpTo(Screen.Study.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        composable(Screen.CardsPlan.route) {
            CardsPlanScreen(
                viewModel = cardsViewModel,
                onBack = {
                    navController.navigate(Screen.Estudio.route) {
                        popUpTo(Screen.CardsPlan.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onStartSession = {
                    navController.navigate(Screen.CardsSession.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onOpenDetailedConfig = {
                    navController.navigate(Screen.CardsConfig.route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        composable(Screen.CardsConfig.route) {
            CardsConfigScreen(
                viewModel = cardsViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onSaveConfig = {
                    navController.navigate(Screen.CardsPlan.route) {
                        popUpTo(Screen.CardsConfig.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        composable(Screen.CardsSession.route) {
            CardsSessionScreen(
                viewModel = cardsViewModel,
                onBack = {
                    navController.navigate(Screen.CardsPlan.route) {
                        popUpTo(Screen.CardsSession.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onSessionFinished = {
                    navController.navigate(Screen.CardsResult.route) {
                        popUpTo(Screen.CardsSession.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        composable(Screen.CardsResult.route) {
            CardsResultScreen(
                viewModel = cardsViewModel,
                onBackToStudy = {
                    navController.navigate(Screen.Estudio.route) {
                        popUpTo(Screen.Estudio.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onRepeatSession = {
                    navController.navigate(Screen.CardsSession.route) {
                        popUpTo(Screen.CardsResult.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onNewConfig = {
                    navController.navigate(Screen.CardsPlan.route) {
                        popUpTo(Screen.CardsResult.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }


        composable(Screen.Ia.route) {
            AiChatScreen(
                onBack = {
                    navController.popBackStack()
                },
                onInicioClick = {
                    navegar(Screen.Home.route)
                },
                onVocabularioClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Vocabulario.route)
                },
                onLotesClick = {
                    vocabularyViewModel.establecerLote(null)
                    navegar(Screen.Lotes.route)
                },
                onEstudioClick = {
                    navegar(Screen.Estudio.route)
                },
                onIaClick = {
                    navegar(Screen.Ia.route)
                },
                onPerfilClick = {
                    navegar(Screen.Profile.route)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onInicioClick = {
                    navegar(Screen.Home.route)
                },
                onVocabularioClick = {
                    navegar(Screen.Vocabulario.route)
                },
                onLotesClick = {
                    navegar(Screen.Lotes.route)
                },
                onEstudioClick = {
                    navegar(Screen.Estudio.route)
                },
                onIaClick = {
                    navegar(Screen.Ia.route)
                },
                onPerfilClick = {
                    navegar(Screen.Profile.route)
                }
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