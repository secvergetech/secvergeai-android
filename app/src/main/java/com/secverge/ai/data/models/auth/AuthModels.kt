package com.secverge.ai.data.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    @SerialName("access_token") val accessToken: String? = null,
    val user: UserDto? = null,
    val message: String? = null
)

@Serializable
data class MeResponse(
    val user: UserDto? = null,
    val authenticated: Boolean = false
)

@Serializable
data class UserDto(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)
