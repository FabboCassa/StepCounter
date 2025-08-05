package com.app.stepcounter.presentation.viewmodel

// Nel file PartyDetailViewModel.kt
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        // All'avvio, diciamo al repository di unirsi alla sessione del party
        viewModelScope.launch {
            repository.joinPartySession(partyId, currentUser)
        }
    }

    fun updateMySteps(steps: Int) {
        viewModelScope.launch {
            repository.updateMySteps(partyId, currentUser.userId, steps)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Diciamo al repository che abbiamo finito con questa schermata
        repository.cleanUpPartyDetailListener()
    }

    fun inviteToParty(context: Context) {
        // Usa il valore del 'partyState' pubblico, non del vecchio '_partyState'
        val party = partyState.value ?: return

        // Cambia il dominio per usare il nuovo schema
        val link = "https://stepcounter.app/join/${party.id}"


        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Unisciti al mio party su StepCounter!")
            putExtra(Intent.EXTRA_TEXT, "Ehi! Unisciti al mio party '${party.name}' su StepCounter usando questo link: $link")
        }

        val chooser = Intent.createChooser(intent, "Invita amici con...")
        context.startActivity(chooser)
    }
}