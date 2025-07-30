package com.app.stepcounter.domain.model
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PartyData(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val participants: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)