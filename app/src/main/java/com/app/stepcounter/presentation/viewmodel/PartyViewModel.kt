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

    val navigationEvents = repository.navigationEvents
    val parties: StateFlow<List<PartyData>> = repository.getAllParties()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    init {
        UserPreferences.getUser()?.let { user ->
            val message = """{ "action": "get_my_parties", "payload": { "userId": "${user.id}" } }"""
            WebSocketManager.sendMessage(message)
        }
    }

    fun createParty(name: String, password: String) {
        UserPreferences.getUser()?.let { user ->
            val newParty = PartyData(
                id = UUID.randomUUID().toString(),
                name = name,
                participants = listOf(Participant(userId = user.id, name = user.name)),
                password = password.ifBlank { null },
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
    }

    fun deleteParty(partyId: String) {
        UserPreferences.getUser()?.let { user ->
            val message = """
            { "action":"delete_party", "payload":{ "partyId":"$partyId", "userId":"${user.id}" } }
        """
            WebSocketManager.sendMessage(message)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun joinPartyWithCode(code: String, password: String) {
        UserPreferences.getUser()?.let { user ->
            val userParticipant = Participant(userId = user.id, name = user.name)
            val message = """
            {
              "action": "join_party_with_code",
              "payload": {
                "inviteCode": "$code",
                "user": ${Json.encodeToString(userParticipant)},
                "password": "$password"
              }
            }
        """.trimIndent()
            WebSocketManager.sendMessage(message)
        }
    }

}

data class PartyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)