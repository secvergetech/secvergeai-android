package com.secverge.ai.data.auth

data class UserSession(
    val token: String,
    val userId: String?,
    val name: String?,
    val email: String?,
    val role: String?
)

data class CurrentUser(
    val userId: String?,
    val name: String?,
    val email: String?,
    val role: String?
)
