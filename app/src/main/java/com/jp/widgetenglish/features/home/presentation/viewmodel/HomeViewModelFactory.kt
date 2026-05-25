package com.jp.widgetenglish.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository

class HomeViewModelFactory(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val actividadDiariaDao: ActividadDiariaDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                repository = repository,
                authRepository = authRepository,
                usuarioDao = usuarioDao,
                actividadDiariaDao = actividadDiariaDao
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}