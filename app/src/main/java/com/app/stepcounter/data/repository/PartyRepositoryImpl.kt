package com.app.stepcounter.data.repository

import com.app.stepcounter.WebSocketManager
import com.app.stepcounter.database.dao.PartyDao
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.model.toDto
import com.app.stepcounter.domain.model.toEntity
import com.app.stepcounter.domain.repository.NavigationEvent
import com.app.stepcounter.domain.repository.PartyRepository
import com.app.stepcounter.server.response.ServerResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class PartyRepositoryImpl(private val partyDao: PartyDao, private val applicationScope: CoroutineScope) : PartyRepository {

    private val _activePartyState = MutableStateFlow<PartyData?>(null)
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        applicationScope.launch {
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

            if (serverResponse.type == "party_created_success" && serverResponse.payload != null) {
                val newPartyDto = json.decodeFromJsonElement<PartyData>(serverResponse.payload)
                partyDao.addParty(newPartyDto.toEntity()) // Corretto: converte DTO in Entity
                _navigationEvents.emit(NavigationEvent.ToPartyDetail(newPartyDto.id))
            } else if (serverResponse.type == "my_parties_list" && serverResponse.payload != null) { // Nota: ho usato 'my_parties_list' dal nostro server privato
                val serverParties = json.decodeFromJsonElement<List<PartyData>>(serverResponse.payload)
                partyDao.replaceAllParties(serverParties.map { it.toEntity() }) // Corretto: converte la lista
            } else if (serverResponse.type == "party_state_update" && serverResponse.payload != null) {
                val partyData = json.decodeFromJsonElement<PartyData>(serverResponse.payload)
                _activePartyState.value = partyData // Corretto: questo flow è già di tipo DTO
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAllParties(): Flow<List<PartyData>> {
        return partyDao.getAllParties().map { entityList ->
            entityList.map { it.toDto() }
        }
    }

    override suspend fun addParty(party: PartyData) {
        // Converte il DTO in Entity prima di passarlo al DAO
        partyDao.addParty(party.toEntity())
    }

    override suspend fun removeParty(partyId: String) {
        // partyId è una String, quindi non serve conversione
        partyDao.removeParty(partyId)
    }

    override suspend fun getParty(partyId: String): PartyData? {
        // Prende l'Entity dal DAO e lo converte in DTO prima di restituirlo
        return partyDao.getPartyById(partyId)?.toDto()
    }

    override suspend fun updateParty(party: PartyData) {
        // Converte il DTO in Entity prima di passarlo al DAO
        partyDao.updateParty(party.toEntity())
    }

    override suspend fun replaceAllParties(parties: List<PartyData>) {
        // Converte la lista di DTO in una lista di Entity prima di passarla al DAO
        partyDao.replaceAllParties(parties.map { it.toEntity() })
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

}