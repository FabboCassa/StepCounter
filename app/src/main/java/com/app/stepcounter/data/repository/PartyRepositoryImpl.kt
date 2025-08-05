package com.app.stepcounter.data.repository

import com.app.stepcounter.WebSocketManager
import com.app.stepcounter.database.dao.PartyDao
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import com.app.stepcounter.server.response.ServerResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class PartyRepositoryImpl(private val partyDao: PartyDao, private val applicationScope: CoroutineScope) : PartyRepository {

    private val _activePartyState = MutableStateFlow<PartyData?>(null)

    init {
        // 2. Usa lo scope passato dal costruttore, non uno nuovo
        applicationScope.launch {
            WebSocketManager.messages.collect { message ->
                handleWebSocketMessage(message)
            }
        }
        WebSocketManager.start()
    }

    // In PartyRepositoryImpl.kt

    private suspend fun handleWebSocketMessage(message: String) {
        // Log di base per vedere se il metodo viene chiamato
        println("--- REPOSITORY: handleWebSocketMessage chiamato con: ---")
        println(message)

        try {
            val json = Json { ignoreUnknownKeys = true }

            println("--- REPOSITORY: Passo 1 - Provo a decodificare ServerResponse...")
            val serverResponse = json.decodeFromString<ServerResponse>(message)
            println("--- REPOSITORY: Passo 2 - Decodifica ServerResponse OK. Tipo: ${serverResponse.type}")

            if (serverResponse.type == "all_parties_list" && serverResponse.payload != null) {
                println("--- REPOSITORY: Passo 3 - Riconosciuto 'all_parties_list'. Provo a decodificare la lista dei party...")
                val serverParties = json.decodeFromJsonElement<List<PartyData>>(serverResponse.payload)
                println("--- REPOSITORY: Passo 4 - Decodifica lista OK. Trovati ${serverParties.size} party.")

                // Chiamata al DAO per aggiornare il database
                partyDao.replaceAllParties(serverParties)

                println("--- REPOSITORY: Passo 5 - Chiamata a replaceAllParties ESEGUITA. Il DB è aggiornato.")

            } else if (serverResponse.type == "party_state_update" && serverResponse.payload != null) {
                println("--- REPOSITORY: Riconosciuto 'party_state_update'.")
                val partyData = json.decodeFromJsonElement<PartyData>(serverResponse.payload)
                _activePartyState.value = partyData
                println("--- REPOSITORY: Stato del party attivo aggiornato.")
            }

        } catch (e: Exception) {
            println("--- !!! REPOSITORY: ERRORE CATTURATO NEL TRY-CATCH !!! ---")
            // Questo stamperà l'errore completo e dettagliato, non solo il messaggio
            e.printStackTrace()
        }
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