// ============================================
// FILE 4: DataManager.kt
// Location: app/src/main/java/com/example/splitsync/data/DataManager.kt
// ============================================
package com.example.splitsync.data

import android.content.Context
import java.io.File

class DataManager {
    var currentUserEmail: String = ""
    private var users = mutableListOf<User>()
    private var trips = mutableListOf<Trip>()
    private var expenses = mutableListOf<Expense>()

    fun saveUser(user: User, context: Context) {
        users.add(user)
        saveUsersToCSV(context)
    }

    fun getUser(email: String, password: String): User? {
        return users.firstOrNull { it.email == email && it.password == password }
    }

    fun userExists(email: String): Boolean {
        return users.any { it.email == email }
    }

    fun saveTrip(trip: Trip, context: Context) {
        trips.add(trip)
        saveTripsToCSV(context)
    }

    fun getTrips(): List<Trip> = trips.filter { it.participants.contains(currentUserEmail) }

    fun getTrip(tripId: String): Trip? = trips.firstOrNull { it.id == tripId }

    fun saveExpense(expense: Expense, context: Context) {
        expenses.add(expense)
        saveExpensesToCSV(context)
    }

    fun getExpenses(tripId: String): List<Expense> {
        return expenses.filter { it.tripId == tripId }
    }

    fun calculateBalances(tripId: String): List<Balance> {
        val tripExpenses = getExpenses(tripId)
        val balances = mutableMapOf<Pair<String, String>, Double>()

        tripExpenses.forEach { expense ->
            val perPersonAmount = expense.totalAmount / expense.sharedBy.size
            expense.sharedBy.forEach { person ->
                if (person != expense.paidBy) {
                    val key = Pair(person, expense.paidBy)
                    balances[key] = (balances[key] ?: 0.0) + perPersonAmount
                }
            }
        }

        return balances.map { Balance(it.key.first, it.key.second, it.value) }
            .filter { it.amount > 0.01 }
    }

    private fun saveUsersToCSV(context: Context) {
        val file = File(context.filesDir, "users.csv")
        file.writeText("email,username,password\n")
        users.forEach { user ->
            file.appendText("${user.email},${user.username},${user.password}\n")
        }
    }

    private fun saveTripsToCSV(context: Context) {
        val file = File(context.filesDir, "trips.csv")
        file.writeText("id,name,location,description,participants,createdBy,createdAt\n")
        trips.forEach { trip ->
            val participants = trip.participants.joinToString(";")
            file.appendText("${trip.id},${trip.name},${trip.location},${trip.description},$participants,${trip.createdBy},${trip.createdAt}\n")
        }
    }

    private fun saveExpensesToCSV(context: Context) {
        val file = File(context.filesDir, "expenses.csv")
        file.writeText("id,tripId,itemName,quantity,totalAmount,description,tags,sharedBy,paidBy,createdBy,location,imageUri,createdAt\n")
        expenses.forEach { expense ->
            val tags = expense.tags.joinToString(";")
            val sharedBy = expense.sharedBy.joinToString(";")
            file.appendText("${expense.id},${expense.tripId},${expense.itemName},${expense.quantity},${expense.totalAmount},${expense.description},$tags,$sharedBy,${expense.paidBy},${expense.createdBy},${expense.location},${expense.imageUri ?: ""},${expense.createdAt}\n")
        }
    }

    fun loadData(context: Context) {
        loadUsersFromCSV(context)
        loadTripsFromCSV(context)
        loadExpensesFromCSV(context)
    }

    private fun loadUsersFromCSV(context: Context) {
        val file = File(context.filesDir, "users.csv")
        if (file.exists()) {
            users.clear()
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 3) {
                    users.add(User(parts[0], parts[1], parts[2]))
                }
            }
        }
    }

    private fun loadTripsFromCSV(context: Context) {
        val file = File(context.filesDir, "trips.csv")
        if (file.exists()) {
            trips.clear()
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 7) {
                    val participants = parts[4].split(";")
                    trips.add(Trip(parts[0], parts[1], parts[2], parts[3], participants, parts[5], parts[6].toLongOrNull() ?: 0))
                }
            }
        }
    }

    private fun loadExpensesFromCSV(context: Context) {
        val file = File(context.filesDir, "expenses.csv")
        if (file.exists()) {
            expenses.clear()
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 13) {
                    val tags = if (parts[6].isNotEmpty()) parts[6].split(";") else emptyList()
                    val sharedBy = parts[7].split(";")
                    expenses.add(
                        Expense(
                            parts[0], parts[1], parts[2], parts[3].toIntOrNull() ?: 1,
                            parts[4].toDoubleOrNull() ?: 0.0, parts[5], tags, sharedBy,
                            parts[8], parts[9], parts[10], parts[11].ifEmpty { null },
                            parts[12].toLongOrNull() ?: 0
                        )
                    )
                }
            }
        }
    }
}