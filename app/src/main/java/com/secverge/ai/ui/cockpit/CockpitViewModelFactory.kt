package com.secverge.ai.ui.cockpit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CockpitViewModelFactory(
    private val repository: CockpitRepository,
    private val sessionStore: CockpitSessionStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(CockpitViewModel::class.java)) {
            return CockpitViewModel(
                repository = repository,
                sessionStore = sessionStore
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
