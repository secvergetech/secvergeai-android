package com.secverge.ai.data.network

class NoOpAuthTokenProvider : AuthTokenProvider {
    override fun getToken(): String? = null
}
