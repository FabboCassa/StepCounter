package com.app.stepcounter.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.stepcounter.data.preferences.StepPreferencesProvider
import com.app.stepcounter.data.repository.StepRepositoryImpl
import com.app.stepcounter.domain.model.StepData
import com.app.stepcounter.domain.repository.StepRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StepCountViewModel(application: Application) : AndroidViewModel(application) {

    // Inizializzazione manuale delle dipendenze
    private val stepPreferences = StepPreferencesProvider.getInstance(application)

    private val repository: StepRepository = StepRepositoryImpl(stepPreferences)

    val stepData: StateFlow<StepData> = repository.getStepData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StepData()
        )

    private val _uiState = MutableStateFlow(StepUiState())
    val uiState: StateFlow<StepUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            stepData.collect {
                Log.d("ViewModel", "Steps: ${it.steps}, Time: ${it.timeInMinutes}")
            }
        }
    }

    fun startTracking() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.startTracking()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun stopTracking() {
        viewModelScope.launch {
            try {
                repository.stopTracking()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            try {
                repository.resetData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class StepUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)