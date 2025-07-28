package com.app.stepcounter.data.repository

import com.app.stepcounter.data.local.StepPreferences
import com.app.stepcounter.domain.repository.StepRepository
import com.app.stepcounter.domain.model.StepData
import kotlinx.coroutines.flow.Flow

class StepRepositoryImpl(
    private val stepPreferences: StepPreferences
) : StepRepository {

    override fun getStepData(): Flow<StepData> = stepPreferences.stepData

    override suspend fun updateSteps(steps: Int) {
        stepPreferences.updateSteps(steps)
    }

    override suspend fun updateTime(timeInMinutes: Int) {
        stepPreferences.updateTime(timeInMinutes)
    }

    override suspend fun startTracking() {
        stepPreferences.setTracking(true)
        if (stepPreferences.getStartTime() == 0L) {
            stepPreferences.setStartTime(System.currentTimeMillis())
        }
    }

    override suspend fun stopTracking() {
        stepPreferences.setTracking(false)
    }

    override suspend fun resetData() {
        stepPreferences.reset()
    }
}