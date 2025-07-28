
package com.app.stepcounter

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCountViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    private val _steps = MutableStateFlow(prefs.getInt("steps", 0))
    private val _time = MutableStateFlow(prefs.getInt("time", 0))
    val steps: StateFlow<Int> = _steps
    val time: StateFlow<Int> = _time

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key == "steps") {
            _steps.value = sp.getInt(key, 0)
        }
        if (key == "time") {
            _time.value = sp.getInt(key, 0)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
}
