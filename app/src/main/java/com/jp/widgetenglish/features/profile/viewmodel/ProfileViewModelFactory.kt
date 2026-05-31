package com.jp.widgetenglish.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.repository.StreakRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val streakRepository: StreakRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                authRepository = authRepository,
                usuarioDao = usuarioDao,
                streakRepository = streakRepository
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
