package com.app.stepcounter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Participant(
    val userId: String,
    val name: String,
    var steps: Int = 0
)