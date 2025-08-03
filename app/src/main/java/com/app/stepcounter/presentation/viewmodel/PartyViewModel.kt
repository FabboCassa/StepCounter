package com.app.stepcounter.presentation.viewmodel

import WebSocketUpdateListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import com.app.stepcounter.server.response.ServerResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID // <-- Importa per generare ID unici

/**
 * Il ViewModel ora implementa WebSocketUpdateListener per reagire
 * ai messaggi del server in tempo reale.
 */
class PartyViewModel(
    private val repository: PartyRepository
) : ViewModel(), WebSocketUpdateListener {

    // Questo StateFlow reagisce ai cambiamenti del database locale.
    // Quando il server ci notificherà un cambiamento, aggiorneremo il database
    // e questa variabile aggiornerà automaticamente la UI.
    val parties: StateFlow<List<PartyData>> = repository.getAllParties()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    init {
        // All'avvio del ViewModel, ci connettiamo al WebSocket.
        // Il ViewModel stesso farà da "ascoltatore" per i messaggi.
        WebSocketManager.setListener(this)
        WebSocketManager.start()
    }

    /**
     * Crea un nuovo party inviando un messaggio al server WebSocket.
     * Non scrive più direttamente nel repository locale.
     */
    fun createParty(name: String) {
        // L'ID dell'utente andrebbe recuperato da un sistema di autenticazione.
        val userId = "user_test_123"
        val partyId = UUID.randomUUID().toString()

        // Creiamo il messaggio JSON da inviare.
        val message = """
            {
               "action": "create_party",
               "payload": {
                    "id": "$partyId",  
                    "name": "$name",    
                    "participants": [],
                    "createdAt": ${System.currentTimeMillis()}
                }
            }
        """.trimIndent()

        // Inviamo il messaggio.
        WebSocketManager.sendMessage(message)
    }

    /**
     * Elimina un party inviando un messaggio al server WebSocket.
     */
    fun deleteParty(partyId: String) {
        val message = """
            {
              "action": "delete_party",
              "payload": {
                "partyId": "$partyId"
              }
            }
        """.trimIndent()
        WebSocketManager.sendMessage(message)
    }

    /**
     * Questo metodo viene chiamato dal WebSocketManager quando arriva un messaggio dal server.
     */
    override fun onWebSocketUpdate(message: String) {
        viewModelScope.launch {
            println("Messaggio WebSocket ricevuto nel ViewModel: $message")

            try {
                val serverResponse = Json.decodeFromString<ServerResponse>(message)

                if (serverResponse.type == "party_created" && serverResponse.payload != null) {
                    // Salva il party ricevuto dal server nel tuo repository.
                    repository.addParty(serverResponse.payload)
                }

            } catch (e: Exception) {
                println("Errore nel parsing del JSON dal server: ${e.message}")
            }
        }
    }

    /**
     * Questo metodo viene chiamato in caso di errore di connessione.
     */
    override fun onWebSocketError(error: String) {
        _uiState.value = _uiState.value.copy(errorMessage = error)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * È FONDAMENTALE pulire il listener quando il ViewModel non è più in uso
     * per evitare memory leak e crash dell'app.
     */
    override fun onCleared() {
        super.onCleared()
        WebSocketManager.removeListener()
        // Decidi se chiudere la connessione quando si esce da questa schermata
        // WebSocketManager.stop()
    }
}

data class PartyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)