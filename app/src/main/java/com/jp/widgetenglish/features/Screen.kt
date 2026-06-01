package com.widgetenglish.app.ui

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object VerifyResetCode : Screen("verify_reset_code")
    object NewPassword : Screen("new_password")

    object Home : Screen("home")
    object Profile : Screen("profile")
    object Statistics : Screen("statistics")


    object Vocabulary : Screen("vocabulary")
    object Vocabulario : Screen("vocabulario")

    object Lots : Screen("lots")
    object Lotes : Screen("lotes")

    object Study : Screen("study")
    object Estudio : Screen("estudio")

    object AIChat : Screen("ai_chat")
    object Ia : Screen("ia")

    object LoteDetail : Screen("lote_detail/{loteId}") {
        fun createRoute(loteId: String): String {
            return "lote_detail/$loteId"
        }
    }

    object VocabularyDetail : Screen("vocabulary_detail/{itemId}/{isVerbo}") {
        fun createRoute(
            itemId: String,
            isVerbo: Boolean
        ): String {
            return "vocabulary_detail/$itemId/$isVerbo"
        }
    }

    object Quiz : Screen("quiz/{loteId}/{repasarFalladas}/{limite}/{failedIds}") {
        fun createRoute(
            loteId: String,
            repasarFalladas: Boolean = false,
            limite: Int = 10,
            failedIds: List<String> = emptyList()
        ): String {
            val failedIdsText = if (failedIds.isEmpty()) {
                "-"
            } else {
                failedIds.joinToString(",")
            }

            return "quiz/$loteId/$repasarFalladas/$limite/$failedIdsText"
        }
    }

    object QuizResult : Screen("quiz_result")

    object CardsPlan : Screen("cards_plan")

    object CardsConfig : Screen("cards_config")

    object CardsSession : Screen("cards_session")

    object CardsResult : Screen("cards_result")

    object AdminDashboard : Screen("admin_dashboard")
    object AdminRanking : Screen("admin_ranking")
    object AdminActivity : Screen("admin_activity")
    object AdminProfile : Screen("admin_profile")
    object AdminCategorias : Screen("admin_categorias")
    object AdminErrores : Screen("admin_errores")
}
