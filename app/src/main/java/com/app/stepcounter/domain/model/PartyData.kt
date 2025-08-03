package com.app.stepcounter.domain.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "party_table") // 1. Indica che questa è una tabella del database
data class PartyData(
    @PrimaryKey // 2. Indica che 'id' è la chiave primaria
    val id: String,
    val name: String,
    val participants: List<String>,
    val createdAt: Long
)