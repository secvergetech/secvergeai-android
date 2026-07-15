package com.secverge.ai.data.network

interface AuthTokenProvider {
    fun getToken(): String?
}
