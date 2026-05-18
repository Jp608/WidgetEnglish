
package com.jp.widgetenglish.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.remote.firestore.AdminFirestoreDataSource

class AdminViewModelFactory(
    private val adminFirestoreDataSource: AdminFirestoreDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(
                adminFirestoreDataSource = adminFirestoreDataSource
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}