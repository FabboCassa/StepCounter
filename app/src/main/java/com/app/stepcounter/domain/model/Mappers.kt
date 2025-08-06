package com.app.stepcounter.domain.model

import kotlinx.serialization.json.Json

fun PartyData.toEntity(): PartyEntity {
    return PartyEntity(
        id = this.id,
        name = this.name,
        participants = Json.encodeToString(this.participants),
        createdAt = this.createdAt,
        inviteCode = this.inviteCode
    )
}

fun PartyEntity.toDto(): PartyData {
    return PartyData(
        id = this.id,
        name = this.name,
        participants = Json.decodeFromString(this.participants),
        createdAt = this.createdAt,
        inviteCode = this.inviteCode,
        password = null
    )
}