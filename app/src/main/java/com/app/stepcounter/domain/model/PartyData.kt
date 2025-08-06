package com.app.stepcounter.domain.model
import kotlinx.serialization.Serializable


@Serializable
data class PartyData(
    val id: String,
    val name: String,
    val participants: List<Participant>,
    val createdAt: Long,
    val inviteCode: String? = null,
    val password: String? = null
)