package com.jp.widgetenglish.features.profile.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository

class StatisticsViewModelFactory(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val actividadDiariaDao: ActividadDiariaDao,
    private val vocabularioRepository: VocabularioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(
                authRepository = authRepository,
                usuarioDao = usuarioDao,
                actividadDiariaDao = actividadDiariaDao,
                vocabularioRepository = vocabularioRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}