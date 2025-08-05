package com.app.stepcounter.data.local

import android.content.Context
import android.content.SharedPreferences
import com.app.stepcounter.domain.model.UserProfile
import java.util.UUID

object UserPreferences {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    private var sharedPreferences: SharedPreferences? = null

    // Questo metodo deve essere chiamato una sola volta, all'avvio dell'app
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveUser(name: String) {
        val userId = UUID.randomUUID().toString()
        sharedPreferences?.edit()?.apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }

    fun getUser(): UserProfile? {
        val userId = sharedPreferences?.getString(KEY_USER_ID, null)
        val userName = sharedPreferences?.getString(KEY_USER_NAME, null)

        return if (userId != null && userName != null) {
            UserProfile(id = userId, name = userName)
        } else {
            null
        }
    }
}