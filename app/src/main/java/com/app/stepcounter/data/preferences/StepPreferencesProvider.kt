package com.app.stepcounter.data.preferences

import android.content.Context
import com.app.stepcounter.data.local.StepPreferences

object StepPreferencesProvider {
    private var instance: StepPreferences? = null

    fun getInstance(context: Context): StepPreferences {
        if (instance == null) {
            instance = StepPreferences(context.applicationContext)
        }
        return instance!!
    }
}
