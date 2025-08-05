package com.app.stepcounter.data.repository

import com.app.stepcounter.WebSocketManager
import com.app.stepcounter.database.dao.PartyDao
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import com.app.stepcounter.server.response.ServerResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

// ✅ 1. RIMUOVI WebSocketUpdateListener da qui
class PartyRepositoryImpl(private val partyDao: PartyDao) : PartyRepository {

    private val _activePartyState = MutableStateFlow<PartyData?>(null)

    init {
        // Il repository si mette in ascolto del nuovo Flow di messaggi
        CoroutineScope(Dispatchers.IO).launch {
            WebSocketManager.messages.collect { message ->
                handleWebSocketMessage(message)
            }
        }
        WebSocketManager.start()
    }

    private suspend fun handleWebSocketMessage(message: String) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val serverResponse = json.decodeFromString<ServerResponse>(message)

            if (serverResponse.type == "all_parties_list" && serverResponse.payload != null) {
                val serverParties = json.decodeFromJsonElement<List<PartyData>>(serverResponse.payload)
                replaceAllParties(serverParties)
            } else if (serverResponse.type == "party_state_update" && serverResponse.payload != null) {
                val partyData = json.decodeFromJsonElement<PartyData>(serverResponse.payload)
                _activePartyState.value = partyData
            } else if (serverResponse.type == "error") {
                println("Errore WebSocket ricevuto: ${serverResponse.message}")
            }
        } catch (e: Exception) {
            println("PartyRepository - Errore nel parsing: ${e.message}")
        }
    }

    // --- Qui sotto ci sono i tuoi metodi dell'interfaccia PartyRepository ---
    // --- Sono tutti corretti e non vanno toccati ---

    override fun getAllParties(): Flow<List<PartyData>> {
        return partyDao.getAllParties()
    }

    override suspend fun addParty(party: PartyData) {
        partyDao.addParty(party)
    }

    override suspend fun removeParty(partyId: String) {
        partyDao.removeParty(partyId)
    }

    override suspend fun getParty(partyId: String): PartyData? {
        return partyDao.getPartyById(partyId)
    }

    override suspend fun updateParty(party: PartyData) {
        partyDao.updateParty(party)
    }

    override suspend fun replaceAllParties(parties: List<PartyData>) {
        partyDao.replaceAllParties(parties)
    }

    override fun getPartyDetails(partyId: String): Flow<PartyData?> {
        return _activePartyState
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

    override suspend fun updateMySteps(partyId: String, userId: String, steps: Int) {
        val stepsMessage = """
            {
              "action": "update_steps",
              "payload": { "partyId": "$partyId", "userId": "$userId", "steps": $steps }
            }
        """.trimIndent()
        WebSocketManager.sendMessage(stepsMessage)
    }

    override fun cleanUpPartyDetailListener() {
        _activePartyState.value = null
    }

    // ✅ 2. CANCELLA COMPLETAMENTE i vecchi metodi onWebSocketUpdate e onWebSocketError
}