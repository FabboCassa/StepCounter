package com.app.stepcounter.server.response

import com.app.stepcounter.domain.model.PartyData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ServerResponse(
    val type: String,
    val payload: JsonElement? = null, // <-- Usa un JsonElement generico
    val message: String? = null
)