package com.app.stepcounter.domain.repository

import com.app.stepcounter.domain.model.PartyData
import kotlinx.coroutines.flow.Flow


interface PartyRepository {
    fun getAllParties(): Flow<List<PartyData>>
    suspend fun addParty(party: PartyData)
    suspend fun removeParty(partyId: String)
    suspend fun getParty(partyId: String): PartyData?
    suspend fun updateParty(party: PartyData)
}
