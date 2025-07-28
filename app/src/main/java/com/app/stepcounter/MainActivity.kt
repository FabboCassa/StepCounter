package com.app.stepcounter

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.app.stepcounter.data.local.StepPreferences
import com.app.stepcounter.data.service.StepService
import com.app.stepcounter.presentation.ui.home.StepHomeScreen
import com.app.stepcounter.presentation.viewmodel.StepCountViewModel
import com.app.stepcounter.ui.theme.StepCounterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StepCountViewModel by viewModels()

    // Istanza globale per il service
    private lateinit var stepPreferences: StepPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsIfNeeded()

        setContent {
            val stepData by viewModel.stepData.collectAsState()
            val uiState by viewModel.uiState.collectAsState()

            StepCounterTheme {
                StepHomeScreen(
                    stepData = stepData,
                    uiState = uiState,
                    onStartClick = {
                        startStepService()
                        viewModel.startTracking()
                    },
                    onStopClick = {
                        stopStepService()
                        viewModel.stopTracking()
                    },
                    onResetClick = {
                        stopStepService()
                        viewModel.resetData()
                    },
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
        }
    }

    private fun startStepService() {
        val intent = Intent(this, StepService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopStepService() {
        val intent = Intent(this, StepService::class.java)
        stopService(intent)
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionsToRequest = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.FOREGROUND_SERVICE_HEALTH
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(android.Manifest.permission.FOREGROUND_SERVICE_HEALTH)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), 1001)
            }
        }
    }
}

