package com.app.stepcounter.data.local

import android.content.Context
import com.app.stepcounter.domain.model.PartyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json


class PartyPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("party_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _parties = MutableStateFlow<List<PartyData>>(loadParties())
    val parties: StateFlow<List<PartyData>> = _parties.asStateFlow()

    private fun loadParties(): List<PartyData> {
        val jsonString = prefs.getString("party_list", "[]") ?: "[]"
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveParties(parties: List<PartyData>) {
        try {
            prefs.edit().putString("party_list", json.encodeToString(parties)).apply()
        } catch (e: Exception) {
            // Log error
        }
    }

    fun addParty(party: PartyData) {
        val updated = _parties.value + party
        _parties.value = updated
        saveParties(updated)
    }

    fun removeParty(partyId: String) {
        val updated = _parties.value.filterNot { it.id == partyId }
        _parties.value = updated
        saveParties(updated)
    }

    fun getParty(id: String): PartyData? {
        return _parties.value.find { it.id == id }
    }

    fun updateParty(updatedParty: PartyData) {
        val updated = _parties.value.map {
            if (it.id == updatedParty.id) updatedParty else it
        }
        _parties.value = updated
        saveParties(updated)
    }
}
