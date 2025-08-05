package com.app.stepcounter.domain.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "party_table")
data class PartyData(
    @PrimaryKey
    val id: String,
    val name: String,
    val participants: List<Participant>,
    val createdAt: Long
)