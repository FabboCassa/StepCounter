package com.app.stepcounter.server.response

import com.app.stepcounter.domain.model.PartyData
import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse(
    val type: String,
    val payload: PartyData? = null
)