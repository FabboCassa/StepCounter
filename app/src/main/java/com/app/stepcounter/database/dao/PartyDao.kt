package com.app.stepcounter.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.stepcounter.domain.model.PartyData
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM party_table ORDER BY createdAt DESC")
    fun getAllParties(): Flow<List<PartyData>>

    // ✅ AGGIUNGI QUESTO METODO per trovare un party tramite ID
    @Query("SELECT * FROM party_table WHERE id = :partyId")
    suspend fun getPartyById(partyId: String): PartyData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addParty(party: PartyData)

    // ✅ AGGIUNGI QUESTO METODO per aggiornare un party esistente
    @Update
    suspend fun updateParty(party: PartyData)

    @Query("DELETE FROM party_table WHERE id = :partyId")
    suspend fun removeParty(partyId: String)
}