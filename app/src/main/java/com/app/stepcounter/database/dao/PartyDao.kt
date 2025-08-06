package com.app.stepcounter.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.stepcounter.domain.model.PartyEntity // <-- Assicurati di importare PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM party_table ORDER BY createdAt DESC")
    fun getAllParties(): Flow<List<PartyEntity>> 

    @Query("SELECT * FROM party_table WHERE id = :partyId")
    suspend fun getPartyById(partyId: String): PartyEntity? 

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addParty(party: PartyEntity) 

    @Update
    suspend fun updateParty(party: PartyEntity) 

    @Query("DELETE FROM party_table WHERE id = :partyId")
    suspend fun removeParty(partyId: String)

    @Query("DELETE FROM party_table")
    suspend fun deleteAllParties()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parties: List<PartyEntity>) 

    @Transaction
    suspend fun replaceAllParties(parties: List<PartyEntity>) { 
        deleteAllParties()
        insertAll(parties)
    }
}