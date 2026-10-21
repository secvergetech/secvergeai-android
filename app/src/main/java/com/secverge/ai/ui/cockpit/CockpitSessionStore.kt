package com.secverge.ai.ui.cockpit

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CockpitSessionStore(
    context: Context
) {
    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val encryptedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            STORE_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Synchronized
    fun loadSessions(): List<CockpitSavedSession> {
        return runCatching {
            val raw = encryptedPreferences
                .getString(KEY_SESSIONS, null)
                ?.takeIf { it.isNotBlank() }
                ?: return@runCatching emptyList()

            json.decodeFromString<List<CockpitSavedSession>>(raw)
                .sortedByDescending { it.updatedAtMillis }
                .take(MAX_SESSIONS)
        }.getOrElse {
            emptyList()
        }
    }

    @Synchronized
    fun saveSession(session: CockpitSavedSession) {
        if (session.entries.isEmpty()) {
            return
        }

        val updated = listOf(session) +
            loadSessions().filterNot {
                it.localId == session.localId
            }

        write(updated)
    }

    @Synchronized
    fun deleteSession(localId: String) {
        write(
            loadSessions().filterNot {
                it.localId == localId
            }
        )
    }

    private fun write(sessions: List<CockpitSavedSession>) {
        runCatching {
            encryptedPreferences.edit()
                .putString(
                    KEY_SESSIONS,
                    json.encodeToString(
                        sessions
                            .sortedByDescending {
                                it.updatedAtMillis
                            }
                            .take(MAX_SESSIONS)
                    )
                )
                .apply()
        }
    }

    private companion object {
        const val STORE_FILE_NAME =
            "secverge_ai_ucf_sessions"

        const val KEY_SESSIONS =
            "ucf_saved_sessions_v1"

        const val MAX_SESSIONS = 40
    }
}
