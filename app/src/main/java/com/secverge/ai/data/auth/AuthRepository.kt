package com.secverge.ai.data.auth

import com.secverge.ai.data.models.auth.LoginRequest
import com.secverge.ai.data.network.ApiClient
import com.secverge.ai.data.network.SECVergeApi

class AuthRepository(
    private val secureStore: SecureStore,
    private val api: SECVergeApi = ApiClient.create(
        tokenProvider = secureStore,
        isDebug = false
    )
) {

    suspend fun login(
        email: String,
        password: String
    ): Result<UserSession> {
        return runCatching {
            require(email.isNotBlank()) {
                "Email address is required."
            }

            require(password.isNotBlank()) {
                "Password is required."
            }

            val response = api.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            )

            val token = response.accessToken
                ?.takeIf { it.isNotBlank() }
                ?: response.token
                    ?.takeIf { it.isNotBlank() }
                ?: error(
                    response.message
                        ?: "The server did not return a session token."
                )

            val user = response.user

            secureStore.saveSession(
                token = token,
                userId = user?.id,
                name = user?.name,
                email = user?.email ?: email.trim(),
                role = user?.role
            )

            UserSession(
                token = token,
                userId = user?.id,
                name = user?.name,
                email = user?.email ?: email.trim(),
                role = user?.role
            )
        }
    }

    suspend fun refreshCurrentUser(): Result<CurrentUser> {
        return runCatching {
            check(isLoggedIn()) {
                "No authenticated session exists."
            }

            val response = api.me()
            val user = response.user
                ?: error("The server did not return the current user.")

            secureStore.updateUser(
                userId = user.id,
                name = user.name,
                email = user.email,
                role = user.role
            )

            CurrentUser(
                userId = user.id,
                name = user.name,
                email = user.email,
                role = user.role
            )
        }
    }

    fun logout() {
        secureStore.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return secureStore.isLoggedIn()
    }

    fun currentUser(): CurrentUser? {
        return secureStore.currentUser()
    }

    fun currentSession(): UserSession? {
        return secureStore.currentSession()
    }
}
