package com.app.stepcounter.data.repository

import com.app.stepcounter.database.dao.PartyDao
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Questa implementazione tiene i party in una lista in memoria
class PartyRepositoryImpl(private val partyDao: PartyDao) : PartyRepository {

    private val _parties = MutableStateFlow<List<PartyData>>(emptyList())

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
}