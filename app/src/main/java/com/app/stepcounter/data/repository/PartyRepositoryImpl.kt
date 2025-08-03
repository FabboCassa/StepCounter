package com.app.stepcounter.data.repository

import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Questa implementazione tiene i party in una lista in memoria
class PartyRepositoryImpl : PartyRepository {

    private val _parties = MutableStateFlow<List<PartyData>>(emptyList())

    override fun getAllParties(): Flow<List<PartyData>> {
        return _parties.asStateFlow()
    }

    override suspend fun addParty(party: PartyData) {
        _parties.value = _parties.value + party
    }

    override suspend fun removeParty(partyId: String) {
        _parties.value = _parties.value.filterNot { it.id == partyId }
    }

    /**
     * Cerca nella lista corrente il primo party che ha l'ID corrispondente.
     * Restituisce null se non lo trova.
     */
    override suspend fun getParty(partyId: String): PartyData? {
        return _parties.value.find { it.id == partyId }
    }

    /**
     * Crea una nuova lista dove il vecchio party viene rimpiazzato con quello nuovo.
     * La funzione 'map' scorre la lista e per ogni elemento decide se tenerlo
     * o sostituirlo con quello aggiornato.
     */
    override suspend fun updateParty(party: PartyData) {
        _parties.value = _parties.value.map {
            if (it.id == party.id) {
                party // Sostituisci con il party aggiornato
            } else {
                it // Mantieni il party esistente
            }
        }
    }
}