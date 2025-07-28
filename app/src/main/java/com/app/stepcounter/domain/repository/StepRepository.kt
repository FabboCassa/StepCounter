package com.app.stepcounter.domain.repository

import com.app.stepcounter.domain.model.StepData
import kotlinx.coroutines.flow.Flow

interface StepRepository {
    fun getStepData(): Flow<StepData>
    suspend fun updateSteps(steps: Int)
    suspend fun updateTime(timeInMinutes: Int)
    suspend fun startTracking()
    suspend fun stopTracking()
    suspend fun resetData()
}