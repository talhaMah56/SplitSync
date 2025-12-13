package com.example.splitsync.data

import java.util.*

data class User(
    val email: String,
    val username: String,
    val password: String
)

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val location: String,
    val description: String,
    val participants: List<String>,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val photoUri: String? = null
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val itemName: String,
    val quantity: Int,
    val totalAmount: Double,
    val description: String,
    val tags: List<String>,
    val sharedBy: List<String>,
    val paidBy: String,
    val createdBy: String,
    val location: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Balance(
    val from: String,
    val to: String,
    val amount: Double
)