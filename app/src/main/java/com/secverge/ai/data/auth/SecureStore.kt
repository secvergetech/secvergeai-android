package com.secverge.ai.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.secverge.ai.data.network.AuthTokenProvider

class SecureStore(
    context: Context
) : AuthTokenProvider {

    private val appContext = context.applicationContext

    private val encryptedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            SECURE_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun getToken(): String? {
        return encryptedPreferences
            .getString(KEY_SESSION_TOKEN, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun saveSession(
        token: String,
        userId: String?,
        name: String?,
        email: String?,
        role: String?
    ) {
        encryptedPreferences.edit()
            .putString(KEY_SESSION_TOKEN, token)
            .putNullableString(KEY_USER_ID, userId)
            .putNullableString(KEY_USER_NAME, name)
            .putNullableString(KEY_USER_EMAIL, email)
            .putNullableString(KEY_USER_ROLE, role)
            .apply()
    }

    fun updateUser(
        userId: String?,
        name: String?,
        email: String?,
        role: String?
    ) {
        encryptedPreferences.edit()
            .putNullableString(KEY_USER_ID, userId)
            .putNullableString(KEY_USER_NAME, name)
            .putNullableString(KEY_USER_EMAIL, email)
            .putNullableString(KEY_USER_ROLE, role)
            .apply()
    }

    fun currentUser(): CurrentUser? {
        if (!isLoggedIn()) {
            return null
        }

        return CurrentUser(
            userId = encryptedPreferences.getString(KEY_USER_ID, null),
            name = encryptedPreferences.getString(KEY_USER_NAME, null),
            email = encryptedPreferences.getString(KEY_USER_EMAIL, null),
            role = encryptedPreferences.getString(KEY_USER_ROLE, null)
        )
    }

    fun currentSession(): UserSession? {
        val token = getToken() ?: return null

        return UserSession(
            token = token,
            userId = encryptedPreferences.getString(KEY_USER_ID, null),
            name = encryptedPreferences.getString(KEY_USER_NAME, null),
            email = encryptedPreferences.getString(KEY_USER_EMAIL, null),
            role = encryptedPreferences.getString(KEY_USER_ROLE, null)
        )
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun clearSession() {
        encryptedPreferences.edit().clear().apply()
    }

    private fun SharedPreferences.Editor.putNullableString(
        key: String,
        value: String?
    ): SharedPreferences.Editor {
        return if (value.isNullOrBlank()) {
            remove(key)
        } else {
            putString(key, value)
        }
    }

    private companion object {
        const val SECURE_FILE_NAME = "secverge_ai_secure_store"

        const val KEY_SESSION_TOKEN = "session_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_ROLE = "user_role"
    }
}
