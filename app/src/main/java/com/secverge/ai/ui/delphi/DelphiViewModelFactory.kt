package com.secverge.ai.ui.delphi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DelphiViewModelFactory(
    private val repository: DelphiRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(DelphiViewModel::class.java)) {
            return DelphiViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
