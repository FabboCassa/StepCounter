package com.app.stepcounter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.stepcounter.data.local.UserPreferences
import com.app.stepcounter.data.repository.PartyRepositoryImpl
import com.app.stepcounter.data.service.StepService
import com.app.stepcounter.database.AppDatabase
import com.app.stepcounter.presentation.ui.home.PartyDetailScreen
import com.app.stepcounter.presentation.ui.home.ProfileSetupScreen
import com.app.stepcounter.presentation.ui.home.StepHomeScreen
import com.app.stepcounter.presentation.ui.home.StepPartyListScreen
import com.app.stepcounter.presentation.ui.navigation.BottomNavigationBar
import com.app.stepcounter.presentation.ui.navigation.Screen
import com.app.stepcounter.presentation.viewmodel.PartyDetailViewModel
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
        val database = AppDatabase.getInstance(this)
        val partyRepository = PartyRepositoryImpl(database.partyDao())
        partyViewModel = PartyViewModel(partyRepository)

        setContent {
            val navController = rememberNavController()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val stepData by stepViewModel.stepData.collectAsState()
            val stepUiState by stepViewModel.uiState.collectAsState()
            val parties by partyViewModel.parties.collectAsState()
            val partyUiState by partyViewModel.uiState.collectAsState()

            val startDestination = if (UserPreferences.getUser() == null) {
                "profile_setup" // Se non c'è un utente, vai al setup
            } else {
                Screen.Home.route // Altrimenti, vai alla Home
            }

            StepCounterTheme {
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            currentRoute = currentRoute,
                            onScreenSelected = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Home.route) {
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

                        composable("profile_setup") {
                            ProfileSetupScreen(
                                onProfileCreated = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo("profile_setup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Parties.route) {
                            StepPartyListScreen(
                                parties = parties,
                                uiState = partyUiState,
                                onCreatePartyClick = { partyName ->
                                    partyViewModel.createParty(partyName)
                                },
                                onPartyClick = { party ->
                                    navController.navigate("party_detail/${party.id}")
                                },
                                onDeleteParty = { partyId ->
                                    partyViewModel.deleteParty(partyId)
                                }
                            )
                        }

                        composable("party_detail/{partyId}") {
                            val partyDetailViewModel: PartyDetailViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        // 1. Otteniamo il SavedStateHandle che contiene l'ID dalla rotta
                                        val savedStateHandle = createSavedStateHandle()

                                        // 2. Creiamo l'istanza del repository, proprio come facciamo
                                        //    all'inizio della MainActivity
                                        val database = AppDatabase.getInstance(application)
                                        val repository = PartyRepositoryImpl(database.partyDao())

                                        // 3. Restituiamo il nostro ViewModel con le dipendenze necessarie
                                        PartyDetailViewModel(repository, savedStateHandle)
                                    }
                                }
                            )

                            PartyDetailScreen(
                                viewModel = partyDetailViewModel,
                                currentSteps = stepData.steps
                            )
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    handleDeepLink(intent) { partyId ->
                        navController.navigate("party_detail/$partyId")
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    private fun handleDeepLink(intent: Intent?, navigateToParty: (String) -> Unit) {
        // Controlla se l'app è stata aperta da un link
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data
            // Estrai l'ID del party dall'URL (è l'ultimo segmento del percorso)
            val partyId = uri?.lastPathSegment
            if (partyId != null) {
                println("Deep Link ricevuto per il party ID: $partyId")
                navigateToParty(partyId)
                // Resetta l'intent per non ri-navigare al cambio di configurazione
                intent.data = null
            }
        }
    }
    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionsToRequest = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.FOREGROUND_SERVICE_HEALTH
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), 1001)
            }
        }
    }
}

