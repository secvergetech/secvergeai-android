package com.secverge.ai.ui.askhachy

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AskHachyHistoryStore(
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
            HISTORY_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Synchronized
    fun loadConversations(): List<AskHachySavedConversation> {
        return runCatching {
            val raw = encryptedPreferences
                .getString(KEY_HISTORY, null)
                ?.takeIf { it.isNotBlank() }
                ?: return@runCatching emptyList()

            json.decodeFromString<List<AskHachySavedConversation>>(raw)
                .sortedByDescending { it.updatedAtMillis }
                .take(MAX_HISTORY_ITEMS)
        }.getOrElse {
            emptyList()
        }
    }

    @Synchronized
    fun saveConversation(
        conversation: AskHachySavedConversation
    ) {
        if (conversation.messages.isEmpty()) {
            return
        }

        val updated = listOf(conversation) +
            loadConversations().filterNot {
                it.localId == conversation.localId
            }

        write(updated)
    }

    @Synchronized
    fun deleteConversation(localId: String) {
        val updated = loadConversations().filterNot {
            it.localId == localId
        }

        write(updated)
    }

    @Synchronized
    fun clearAll() {
        runCatching {
            encryptedPreferences.edit()
                .remove(KEY_HISTORY)
                .apply()
        }
    }

    private fun write(
        conversations: List<AskHachySavedConversation>
    ) {
        runCatching {
            encryptedPreferences.edit()
                .putString(
                    KEY_HISTORY,
                    json.encodeToString(
                        conversations
                            .sortedByDescending { it.updatedAtMillis }
                            .take(MAX_HISTORY_ITEMS)
                    )
                )
                .apply()
        }
    }

    private companion object {
        const val HISTORY_FILE_NAME =
            "secverge_ai_ask_hachy_history"

        const val KEY_HISTORY =
            "ask_hachy_saved_conversations_v1"

        const val MAX_HISTORY_ITEMS = 50
    }
}
