package com.app.stepcounter.domain.model

data class StepData(
    val steps: Int = 0,
    val timeInMinutes: Int = 0,
    val calories: Double = 0.0,
    val distance: Double = 0.0,
    val isTracking: Boolean = false
) {
    val caloriesBurned: Double
        get() = steps * 0.04

    val distanceKm: Double
        get() = steps * 0.0008
}