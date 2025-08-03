package com.app.stepcounter

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.app.stepcounter.data.preferences.PartyPreferencesProvider
import com.app.stepcounter.data.repository.PartyRepositoryImpl
import com.app.stepcounter.data.service.StepService
import com.app.stepcounter.presentation.ui.home.StepHomeScreen
import com.app.stepcounter.presentation.ui.home.StepPartyListScreen
import com.app.stepcounter.presentation.ui.navigation.BottomNavigationBar
import com.app.stepcounter.presentation.ui.navigation.Screen
import com.app.stepcounter.presentation.viewmodel.PartyViewModel
import com.app.stepcounter.presentation.viewmodel.StepCountViewModel
import com.app.stepcounter.ui.theme.StepCounterTheme

class MainActivity : ComponentActivity() {

    // ViewModels
    private val stepViewModel: StepCountViewModel by viewModels()
    private lateinit var partyViewModel: PartyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsIfNeeded()

        // Inizializza party dependencies
        val partyRepository = PartyRepositoryImpl()
        partyViewModel = PartyViewModel(partyRepository)

        setContent {
            var currentScreen by remember { mutableStateOf(Screen.Home) }

            // States
            val stepData by stepViewModel.stepData.collectAsState()
            val stepUiState by stepViewModel.uiState.collectAsState()
            val parties by partyViewModel.parties.collectAsState()
            val partyUiState by partyViewModel.uiState.collectAsState()

            StepCounterTheme {
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (currentScreen) {
                            Screen.Home -> {
                                StepHomeScreen(
                                    stepData = stepData,
                                    uiState = stepUiState,
                                    onStartClick = {
                                        startStepService()
                                        stepViewModel.startTracking()
                                    },
                                    onStopClick = {
                                        stopStepService()
                                        stepViewModel.stopTracking()
                                    },
                                    onResetClick = {
                                        stopStepService()
                                        stepViewModel.resetData()
                                    },
                                    onErrorDismiss = { stepViewModel.clearError() }
                                )
                            }
                            Screen.Parties -> {
                                StepPartyListScreen(
                                    parties = parties,
                                    uiState = partyUiState,
                                    // Modifica questa lambda per chiamare la nuova funzione
                                    onCreatePartyClick = {
                                        // Qui potresti mostrare un dialogo per chiedere il nome del party
                                        // Per ora, usiamo un nome di default.
                                        partyViewModel.createParty("Nuovo Super Party!")
                                    },
                                    onPartyClick = { party ->
                                        // Naviga al dettaglio party
                                    },
                                    onDeleteParty = { partyId ->
                                        partyViewModel.deleteParty(partyId)
                                    }
                                )
                            }
                        }
                    }
                }
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

