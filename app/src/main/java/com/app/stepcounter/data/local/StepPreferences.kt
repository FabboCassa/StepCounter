package com.app.stepcounter.data.local

import android.content.Context
import android.content.SharedPreferences
import com.app.stepcounter.domain.model.StepData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    private val _stepData = MutableStateFlow(
        StepData(
            steps = prefs.getInt("steps", 0),
            timeInMinutes = prefs.getInt("time", 0),
            isTracking = prefs.getBoolean("is_tracking", false)
        )
    )

    val stepData: Flow<StepData> = _stepData.asStateFlow()

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "steps", "time", "is_tracking" -> {
                _stepData.value = StepData(
                    steps = prefs.getInt("steps", 0),
                    timeInMinutes = prefs.getInt("time", 0),
                    isTracking = prefs.getBoolean("is_tracking", false)
                )
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    fun updateSteps(steps: Int) {
        prefs.edit().putInt("steps", steps).apply()
    }

    fun updateTime(timeInMinutes: Int) {
        prefs.edit().putInt("time", timeInMinutes).apply()
    }

    fun setTracking(isTracking: Boolean) {
        prefs.edit().putBoolean("is_tracking", isTracking).apply()
    }

    fun getStartTime(): Long = prefs.getLong("start_time", 0L)

    fun setStartTime(time: Long) {
        prefs.edit().putLong("start_time", time).apply()
    }

    fun reset() {
        prefs.edit()
            .putInt("steps", 0)
            .putInt("time", 0)
            .putBoolean("is_tracking", false)
            .remove("start_time")
            .apply()
    }
}