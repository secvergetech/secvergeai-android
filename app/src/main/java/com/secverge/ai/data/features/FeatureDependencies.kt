package com.secverge.ai.data.features

import com.secverge.ai.data.auth.SecureStore
import com.secverge.ai.data.network.ApiClient
import com.secverge.ai.data.network.SECVergeApi

class FeatureDependencies(
    secureStore: SecureStore
) {
    val api: SECVergeApi by lazy {
        ApiClient.create(
            tokenProvider = secureStore,
            isDebug = false
        )
    }
}
