package com.app.stepcounter.domain.repository

import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.domain.model.PartyData
import kotlinx.coroutines.flow.Flow


interface PartyRepository {
    fun getAllParties(): Flow<List<PartyData>>
    suspend fun addParty(party: PartyData)
    suspend fun removeParty(partyId: String)
    suspend fun getParty(partyId: String): PartyData?
    suspend fun updateParty(party: PartyData)
    fun getPartyDetails(partyId: String): Flow<PartyData?>

    suspend fun updateMySteps(partyId: String, userId: String, steps: Int)

    suspend fun joinPartySession(partyId: String, user: Participant)

    fun cleanUpPartyDetailListener()

    suspend fun replaceAllParties(parties: List<PartyData>)
}
