package com.app.stepcounter.data.repository

import com.app.stepcounter.data.local.PartyPreferences
import com.app.stepcounter.domain.model.PartyData
import com.app.stepcounter.domain.repository.PartyRepository
import kotlinx.coroutines.flow.Flow

class PartyRepositoryImpl(
    private val preferences: PartyPreferences
) : PartyRepository {

    override fun getAllParties(): Flow<List<PartyData>> = preferences.parties

    override suspend fun addParty(party: PartyData) {
        preferences.addParty(party)
    }

    override suspend fun removeParty(partyId: String) {
        preferences.removeParty(partyId)
    }

    override suspend fun getParty(partyId: String): PartyData? {
        return preferences.getParty(partyId)
    }

    override suspend fun updateParty(party: PartyData) {
        preferences.updateParty(party)
    }
}
