package com.app.stepcounter.data.repository

import WebSocketUpdateListener
import com.app.stepcounter.database.dao.PartyDao
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import com.app.stepcounter.server.response.ServerResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

// Questa implementazione tiene i party in una lista in memoria
class PartyRepositoryImpl(private val partyDao: PartyDao) : PartyRepository, WebSocketUpdateListener {

    private val _parties = MutableStateFlow<List<PartyData>>(emptyList())
    private val _activePartyState = MutableStateFlow<PartyData?>(null)

    init {
        WebSocketManager.setListener(this)
        WebSocketManager.start()
    }

    override fun getAllParties(): Flow<List<PartyData>> {
        return partyDao.getAllParties()
    }

    override suspend fun addParty(party: PartyData) {
        partyDao.addParty(party)
    }

    override suspend fun removeParty(partyId: String) {
        partyDao.removeParty(partyId)
    }

    /**
     * Cerca nella lista corrente il primo party che ha l'ID corrispondente.
     * Restituisce null se non lo trova.
     */
    override suspend fun getParty(partyId: String): PartyData? {
        return partyDao.getPartyById(partyId)
    }

    /**
     * Chiama la funzione del DAO per aggiornare il party nel database.
     */
    override suspend fun updateParty(party: PartyData) {
        partyDao.updateParty(party)
    }

    override fun getPartyDetails(partyId: String): Flow<PartyData?> {
        // Restituisce il flow che il ViewModel osserverà.
        // Quando onWebSocketUpdate aggiornerà _activePartyState, la UI riceverà i dati.
        return _activePartyState
    }

    override suspend fun updateMySteps(partyId: String, userId: String, steps: Int) {
        val stepsMessage = """
            {
              "action": "update_steps",
              "payload": { "partyId": "$partyId", "userId": "$userId", "steps": $steps }
            }
        """.trimIndent()
        WebSocketManager.sendMessage(stepsMessage)
    }

    override suspend fun joinPartySession(partyId: String, user: Participant) {
        val joinMessage = """
            {
              "action": "join_party",
              "payload": { "partyId": "$partyId", "user": ${Json.encodeToString(user)} }
            }
        """.trimIndent()
        WebSocketManager.sendMessage(joinMessage)
    }


    override fun cleanUpPartyDetailListener() {
        // Quando usciamo dal dettaglio, resettiamo lo stato
        _activePartyState.value = null
        // Potremmo anche inviare un messaggio di "leave_party" al server
    }

    override suspend fun replaceAllParties(parties: List<PartyData>) {
        partyDao.replaceAllParties(parties)
    }

    override fun onWebSocketUpdate(message: String) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val serverResponse = json.decodeFromString<ServerResponse>(message)

            if (serverResponse.payload == null) return

            if (serverResponse.type == "party_state_update") {
                val partyData = json.decodeFromJsonElement<PartyData>(serverResponse.payload)
                // ...aggiorniamo il nostro flow interno.
                _activePartyState.value = partyData
            }

        } catch (e: Exception) {
            println("PartyRepository - Errore nel parsing: ${e.message}")
        }
    }

    override fun onWebSocketError(error: String) {
        println("PartyRepository - Errore WebSocket: $error")
    }
}