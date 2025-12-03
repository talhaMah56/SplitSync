// ============================================
// FILE: HistoryScreen.kt
// Location: app/src/main/java/com/example/splitsync/screens/HistoryScreen.kt
// ============================================
package com.example.splitsync.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitsync.data.DataManager
import com.example.splitsync.data.Trip
import com.example.splitsync.data.Expense

//private val Icons.Filled.History: Any

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    dataManager: DataManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        dataManager.loadData(context)
    }

    val trips = dataManager.getTrips()
    val currentUser = dataManager.currentUserEmail

    // Get all unique participants across all trips (excluding yourself)
    val allParticipants = trips.flatMap { it.participants }
        .filter { it != currentUser }
        .distinct()
        .sorted()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (allParticipants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Icon(
//                            Icons.Default.History,
//                            "No history",
//                            modifier = Modifier.size(64.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
                        // TO THIS (DateRange is available in Core):
                        Icon(
                            Icons.Default.DateRange,
                            "No history",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No history yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add friends to trips to see history here",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allParticipants) { participant ->
                        HistoryParticipantCard(
                            participant = participant,
                            currentUser = currentUser,
                            trips = trips,
                            dataManager = dataManager
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryParticipantCard(
    participant: String,
    currentUser: String,
    trips: List<Trip>,
    dataManager: DataManager
) {
    var expanded by remember { mutableStateOf(false) }

    // 1. Calculate the high-level totals (Net Balance)
    // We aggregate balances from all trips
    val allBalances = trips.flatMap { trip ->
        dataManager.calculateBalances(trip.id)
    }

    val amountTheyOweMe = allBalances
        .filter { it.from == participant && it.to == currentUser }
        .sumOf { it.amount }

    val amountIOweThem = allBalances
        .filter { it.from == currentUser && it.to == participant }
        .sumOf { it.amount }

    val netBalance = amountTheyOweMe - amountIOweThem

    // 2. Gather the specific Transaction History for the expanded view
    // We need to look at raw expenses where both users were involved
    data class TransactionItem(
        val tripName: String,
        val description: String,
        val amount: Double,
        val isTheyOweMe: Boolean // True if they owe me, False if I owe them
    )

    val historyItems = remember(trips) {
        val items = mutableListOf<TransactionItem>()

        trips.forEach { trip ->
            val tripExpenses = dataManager.getExpenses(trip.id)
            tripExpenses.forEach { expense ->

                val shareAmount = expense.totalAmount / expense.sharedBy.size

                // Case A: I paid, and they were included in the split
                if (expense.paidBy == currentUser && expense.sharedBy.contains(participant)) {
                    items.add(TransactionItem(
                        tripName = trip.name,
                        description = expense.itemName,
                        amount = shareAmount,
                        isTheyOweMe = true
                    ))
                }

                // Case B: They paid, and I was included in the split
                if (expense.paidBy == participant && expense.sharedBy.contains(currentUser)) {
                    items.add(TransactionItem(
                        tripName = trip.name,
                        description = expense.itemName,
                        amount = shareAmount,
                        isTheyOweMe = false
                    ))
                }
            }
        }
        items
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = participant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Net Balance Text
                    when {
                        netBalance > 0.01 -> {
                            Text(
                                text = "Owes you $${String.format("%.2f", netBalance)}",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50), // Green
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        netBalance < -0.01 -> {
                            Text(
                                text = "You owe $${String.format("%.2f", kotlin.math.abs(netBalance))}",
                                fontSize = 14.sp,
                                color = Color(0xFFE53935), // Red
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        else -> {
                            Text(
                                text = "Settled up",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "View details"
                )
            }

            // --- EXPANDED DETAILS ---
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (historyItems.isEmpty()) {
                    Text(
                        "No shared expenses found.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Expense History:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        historyItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left side: Description + Trip Name
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.description,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.tripName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Right side: Amount and indicator
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", item.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isTheyOweMe) Color(0xFF4CAF50) else Color(0xFFE53935)
                                    )
                                    Text(
                                        text = if (item.isTheyOweMe) "they owe" else "you owe",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}