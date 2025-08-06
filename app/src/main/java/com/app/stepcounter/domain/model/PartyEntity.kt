package com.app.stepcounter.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "party_table")
data class PartyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val participants: String,
    val createdAt: Long,
    val inviteCode: String? = null
)
