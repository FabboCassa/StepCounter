package com.app.stepcounter.presentation.viewmodel

// Nel file PartyDetailViewModel.kt
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.stepcounter.data.local.UserPreferences
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PartyDetailViewModel(
    private val repository: PartyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val partyId: String = checkNotNull(savedStateHandle["partyId"])

    // Il ViewModel ora espone semplicemente il Flow che arriva dal repository
    val partyState: StateFlow<PartyData?> = repository.getPartyDetails(partyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val currentUser = Participant(userId = "user123", name = "Io")

    init {
        UserPreferences.getUser()?.let { userProfile ->
            val currentUser = Participant(userId = userProfile.id, name = userProfile.name)
            viewModelScope.launch {
                repository.joinPartySession(partyId, currentUser)
            }
        }
    }

    fun updateMySteps(steps: Int) {
        UserPreferences.getUser()?.let { userProfile ->
            viewModelScope.launch {
                repository.updateMySteps(partyId, userProfile.id, steps)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Diciamo al repository che abbiamo finito con questa schermata
        repository.cleanUpPartyDetailListener()
    }

    fun shareInviteCode(context: Context) {
        val party = partyState.value ?: return
        val inviteCode = party.inviteCode ?: "Codice non disponibile"

        // Creiamo un Intent per condividere semplice testo
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Codice invito per il party '${party.name}'")
            putExtra(Intent.EXTRA_TEXT, "Ehi! Unisciti al mio party '${party.name}' su StepCounter. Il codice di invito è: $inviteCode")
        }

        val chooser = Intent.createChooser(intent, "Condividi codice con...")
        context.startActivity(chooser)
    }
}