package com.app.stepcounter.data.preferences

import android.content.Context
import com.app.stepcounter.data.local.PartyPreferences

object PartyPreferencesProvider {
    private var instance: PartyPreferences? = null

    fun getInstance(context: Context): PartyPreferences {
        if (instance == null) {
            instance = PartyPreferences(context.applicationContext)
        }
        return instance!!
    }
}
