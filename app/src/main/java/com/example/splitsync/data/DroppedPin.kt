package com.example.splitsync.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DroppedPin(
    val id: String = UUID.randomUUID().toString(),
    val lat: Double,
    val lng: Double,
    val title: String? = null,
    val note: String? = null
)
