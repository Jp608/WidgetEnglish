package com.widgetenglish.app.ui

sealed class Screen(val route: String) {
    object Login          : Screen("login")
    object Register       : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home           : Screen("home")
    object Vocabulary     : Screen("vocabulary")
    object Lots           : Screen("lots")
    object Study          : Screen("study")
    object AIChat         : Screen("ai_chat")
    object Profile        : Screen("profile")
    object VerifyResetCode : Screen("verify_reset_code")
    object NewPassword     : Screen("new_password")
    object Vocabulario : Screen("vocabulario")
    object Lotes : Screen("lotes")
    object Estudio : Screen("estudio")
    object Ia : Screen("ia")
}