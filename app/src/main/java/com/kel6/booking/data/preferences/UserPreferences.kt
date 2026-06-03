package com.kel6.booking.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property — buat DataStore instance sekali per Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_TOKEN    = stringPreferencesKey("jwt_token")
        val KEY_USER_ID  = longPreferencesKey("user_id")
        val KEY_NAME     = stringPreferencesKey("user_name")
        val KEY_EMAIL    = stringPreferencesKey("user_email")
        val KEY_PHONE    = stringPreferencesKey("user_phone")
        val KEY_ROLE     = stringPreferencesKey("user_role")
    }

    // Flow untuk dibaca dari mana saja
    val token: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val userId: Flow<Long?>  = context.dataStore.data.map { it[KEY_USER_ID] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val userPhone: Flow<String?> = context.dataStore.data.map { it[KEY_PHONE] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[KEY_ROLE] }
    val isLoggedIn: Flow<Boolean> = token.map { !it.isNullOrEmpty() }

    // Simpan data setelah login/register berhasil
    suspend fun saveUserSession(token: String, userId: Long, name: String,
                                email: String, phone: String?, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]   = token
            prefs[KEY_USER_ID] = userId
            prefs[KEY_NAME]    = name
            prefs[KEY_EMAIL]   = email
            prefs[KEY_PHONE]   = phone ?: ""
            prefs[KEY_ROLE]    = role
        }
    }

    // Hapus semua data saat logout
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}