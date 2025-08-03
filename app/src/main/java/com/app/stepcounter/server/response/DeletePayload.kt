package com.app.stepcounter.server.response
import kotlinx.serialization.Serializable

@Serializable
data class DeletePayload(val partyId: String)