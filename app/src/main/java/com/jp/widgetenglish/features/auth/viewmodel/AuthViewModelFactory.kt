package com.jp.widgetenglish.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.auth.AuthRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val usuarioFirestoreDataSource: UsuarioFirestoreDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                authRepository = authRepository,
                usuarioDao = usuarioDao,
                usuarioFirestoreDataSource = usuarioFirestoreDataSource
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}