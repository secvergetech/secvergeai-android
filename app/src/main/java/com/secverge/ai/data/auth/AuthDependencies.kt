package com.secverge.ai.data.auth

import android.content.Context

class AuthDependencies(
    context: Context
) {
    val secureStore: SecureStore by lazy {
        SecureStore(context.applicationContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(secureStore)
    }
}
