package com.app.stepcounter.presentation.viewmodel

import WebSocketUpdateListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import com.app.stepcounter.server.response.DeletePayload
import com.app.stepcounter.server.response.ServerResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID

class PartyViewModel(
    private val repository: PartyRepository
) : ViewModel(), WebSocketUpdateListener {

    val parties: StateFlow<List<PartyData>> = repository.getAllParties()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    init {
        WebSocketManager.setListener(this)
        WebSocketManager.start()
        // Non dobbiamo più fare nulla qui, il server invierà la lista alla connessione.
    }

    /**
     * Crea un nuovo party inviando un messaggio al server WebSocket.
     * Non scrive più direttamente nel repository locale.
     */
    fun createParty(name: String) {
        val newParty = PartyData(
            id = UUID.randomUUID().toString(),
            name = name,
            participants = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        val message = """
            {
              "action": "create_party",
              "payload": ${Json.encodeToString(newParty)}
            }
        """.trimIndent()
        WebSocketManager.sendMessage(message)
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

    /**
     * Questo metodo viene chiamato dal WebSocketManager quando arriva un messaggio dal server.
     */
    override fun onWebSocketUpdate(message: String) {
        viewModelScope.launch {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val serverResponse = json.decodeFromString<ServerResponse>(message)

                // Se il server invia la lista completa dei party...
                if (serverResponse.type == "all_parties_list" && serverResponse.payload != null) {
                    // ...decodifichiamo la lista...
                    val serverParties = json.decodeFromJsonElement<List<PartyData>>(serverResponse.payload)
                    // ...e la usiamo per sostituire completamente i dati nel nostro database.
                    repository.replaceAllParties(serverParties)
                }
            } catch (e: Exception) {
                println("PartyViewModel - Errore nel parsing: ${e.message}")
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