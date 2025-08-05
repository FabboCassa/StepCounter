package com.app.stepcounter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.stepcounter.WebSocketManager
import com.app.stepcounter.data.local.UserPreferences
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import java.util.UUID
class PartyViewModel(
    private val repository: PartyRepository
) : ViewModel() {

    // Questo non cambia. La UI osserva i dati forniti dal repository.
    val parties: StateFlow<List<PartyData>> = repository.getAllParties()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    // L'init block è ora molto più semplice, non deve più gestire il listener.
    init {
        // Possiamo assicurarci che il WebSocket sia attivo, sebbene lo faccia già il repository.
        WebSocketManager.start()
    }

    fun createParty(name: String) {
        // Prende l'utente salvato per includerlo come primo partecipante
        UserPreferences.getUser()?.let { user ->
            val newParty = PartyData(
                id = UUID.randomUUID().toString(),
                name = name,
                participants = listOf(Participant(userId = user.id, name = user.name, steps = 0)),
                createdAt = System.currentTimeMillis()
            )

            // Il ViewModel costruisce il messaggio...
            val message = """
                {
                  "action": "create_party",
                  "payload": ${Json.encodeToString(newParty)}
                }
            """.trimIndent()

            // ...e lo invia. Non si preoccupa della risposta, quella la gestirà il Repository.
            WebSocketManager.sendMessage(message)
        }
    }

    fun deleteParty(partyId: String) {
        val message = """
            {
              "action": "delete_party",
              "payload": { "partyId": "$partyId" }
            }
        """.trimIndent()
        WebSocketManager.sendMessage(message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}

data class PartyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)