//// ============================================
//// FILE: TripDetailScreen.kt
//// Location: app/src/main/java/com/example/splitsync/screens/TripDetailScreen.kt
//// ============================================
//package com.example.splitsync.screens
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.splitsync.data.DataManager
//import java.text.SimpleDateFormat
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TripDetailScreen(
//    tripId: String,
//    dataManager: DataManager,
//    onBack: () -> Unit,
//    onNavigateToAddExpense: () -> Unit
//) {
//    val context = LocalContext.current
//    val trip = dataManager.getTrip(tripId)
//    val expenses = dataManager.getExpenses(tripId)
//    val balances = dataManager.calculateBalances(tripId)
//    var showBalances by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        dataManager.loadData(context)
//    }
//
//    if (trip == null) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Trip not found")
//        }
//        return
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(trip.name) },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { showBalances = !showBalances }) {
//                        Icon(Icons.Default.DateRange, "View balances")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = onNavigateToAddExpense) {
//                Icon(Icons.Default.Add, "Add expense")
//            }
//        }
//    ) { paddingValues ->
//        // 1. We removed the outer Column and started the LazyColumn immediately
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues),
//            // 2. Padding is applied to the content, not the container, for better scrolling
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            // 3. The Trip Detail Card is now the first 'item' in the list
//            item {
//                Card(modifier = Modifier.fillMaxWidth()) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Default.Place, "Location")
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(trip.location)
//                        }
//                        if (trip.description.isNotEmpty()) {
//                            Text(
//                                text = trip.description,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Default.Person, "Participants")
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("${trip.participants.size} participants")
//                        }
//                    }
//                }
//            }
//
//            // 4. Balances Logic wrapped in 'item' blocks
//            if (showBalances) {
//                item {
//                    Text(
//                        text = "Balances",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                if (balances.isEmpty()) {
//                    item {
//                        Text(
//                            text = "All settled up!",
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                } else {
//                    // We can map the balances to items here
//                    items(balances) { balance ->
//                        Card(
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(12.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Column {
//                                    Text(
//                                        text = "${balance.from}",
//                                        fontSize = 14.sp
//                                    )
//                                    Text(
//                                        text = "owes ${balance.to}",
//                                        fontSize = 12.sp,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
//                                Text(
//                                    text = "$${String.format("%.2f", balance.amount)}",
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = MaterialTheme.colorScheme.primary
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // 5. Expenses Header
//            item {
//                Text(
//                    text = "Expenses",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            // 6. Expenses List
//            if (expenses.isEmpty()) {
//                item {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp), // Give it some height so it looks centered
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Icon(
//                                Icons.Default.ShoppingCart,
//                                "No expenses",
//                                modifier = Modifier.size(64.dp),
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text(
//                                "No expenses yet",
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                }
//            } else {
//                items(expenses) { expense ->
//                    ExpenseCard(expense)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ParticipantBalanceCard(
//    person: String,
//    allExpenses: List<com.example.splitsync.data.Expense>,
//    settlements: List<com.example.splitsync.data.Balance>
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    // 1. Filter data
//    val sharedExpenses = allExpenses.filter { it.sharedBy.contains(person) }
//    val myDebts = settlements.filter { it.from == person }
//
//    // 2. Calculate status for the indicator
//    val totalOwed = myDebts.sumOf { it.amount }
//    val isSettled = totalOwed <= 0.0
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { expanded = !expanded },
//        colors = CardDefaults.cardColors(
//            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            // HEADER ROW
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Left Side: Icon + Name
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Default.Person, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = person,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                // Right Side: Status Indicator + Arrow
//                Row(verticalAlignment = Alignment.CenterVertically) {
//
//                    // --- STATUS INDICATOR LOGIC START ---
//                    if (isSettled) {
//                        // Green Dot
//                        Box(
//                            modifier = Modifier
//                                .size(10.dp)
//                                .padding(end = 4.dp)
//                                .background(androidx.compose.ui.graphics.Color(0xFF4CAF50), androidx.compose.foundation.shape.CircleShape)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            "Settled",
//                            fontSize = 12.sp,
//                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
//                        )
//                    } else {
//                        // Red Dot + Amount
//                        Box(
//                            modifier = Modifier
//                                .size(10.dp)
//                                .background(androidx.compose.ui.graphics.Color(0xFFE53935), androidx.compose.foundation.shape.CircleShape)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "-$${String.format("%.2f", totalOwed)}",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = androidx.compose.ui.graphics.Color(0xFFE53935)
//                        )
//                    }
//                    // --- STATUS INDICATOR LOGIC END ---
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Icon(
//                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                        "Toggle details",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            // EXPANDED CONTENT
//            if (expanded) {
//                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
//
//                if (sharedExpenses.isEmpty()) {
//                    Text(
//                        "No shared expenses.",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                } else {
//                    Text(
//                        "Expenses Share:",
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//
//                    sharedExpenses.forEach { expense ->
//                        val shareAmount = expense.totalAmount / expense.sharedBy.size
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 4.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = expense.itemName,
//                                fontSize = 14.sp,
//                                modifier = Modifier.weight(1f)
//                            )
//                            Text(
//                                text = "$${String.format("%.2f", shareAmount)}",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//
//                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
//
//                if (isSettled) {
//                    Text(
//                        text = "All settled up! No payments needed.",
//                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50), // Green
//                        fontWeight = FontWeight.Bold
//                    )
//                } else {
//                    Text(
//                        "Payments needed:",
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.padding(bottom = 4.dp)
//                    )
//                    myDebts.forEach { debt ->
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text("Pay to ${debt.to}")
//                            Text(
//                                text = "$${String.format("%.2f", debt.amount)}",
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ExpenseCard(expense: com.example.splitsync.data.Expense) {
//    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
//    var expanded by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { expanded = !expanded }
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = expense.itemName,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = "Paid by ${expense.paidBy}",
//                        fontSize = 12.sp,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//                Text(
//                    text = "$${String.format("%.2f", expense.totalAmount)}",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            if (expanded) {
//                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
//
//                Text(text = "Quantity: ${expense.quantity}")
//                if (expense.description.isNotEmpty()) {
//                    Text(text = "Description: ${expense.description}")
//                }
//                Text(text = "Shared by: ${expense.sharedBy.joinToString(", ")}")
//                if (expense.tags.isNotEmpty()) {
//                    Text(text = "Tags: ${expense.tags.joinToString(", ")}")
//                }
//                Text(
//                    text = "Created: ${dateFormat.format(Date(expense.createdAt))}",
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                if (expense.location.isNotEmpty()) {
//                    Text(
//                        text = "Location: ${expense.location}",
//                        fontSize = 12.sp,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        }
//    }
//}
// ============================================
// FILE: TripDetailScreen.kt
// Location: app/src/main/java/com/example/splitsync/screens/TripDetailScreen.kt
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitsync.data.DataManager
import com.example.splitsync.data.Expense
import com.example.splitsync.data.Balance
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    dataManager: DataManager,
    onBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit
) {
    val context = LocalContext.current
    val trip = dataManager.getTrip(tripId)
    val expenses = dataManager.getExpenses(tripId)
    val balances = dataManager.calculateBalances(tripId)
    var showBalances by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dataManager.loadData(context)
    }

    if (trip == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Trip not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBalances = !showBalances }) {
                        Icon(Icons.Default.DateRange, "View balances")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpense) {
                Icon(Icons.Default.Add, "Add expense")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Trip Detail Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, "Location")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(trip.location)
                        }
                        if (trip.description.isNotEmpty()) {
                            Text(
                                text = trip.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Participants")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${trip.participants.size} participants")
                        }
                    }
                }
            }

            // 2. Balances Section
            if (showBalances) {
                item {
                    Text(
                        text = "Breakdown by Person",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // CHANGED: We now iterate over participants, not the balance list
                items(trip.participants) { person ->
                    ParticipantBalanceCard(
                        person = person,
                        allExpenses = expenses,
                        settlements = balances
                    )
                }

                // Add a small spacer after the list
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // 3. Expenses Section
            item {
                Text(
                    text = "All Expenses",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                "No expenses",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No expenses yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(expenses) { expense ->
                    ExpenseCard(expense)
                }
            }
        }
    }
}

// ---------------------------------------------
// NEW COMPOSABLE: Participant Balance Card
// ---------------------------------------------
@Composable
fun ParticipantBalanceCard(
    person: String,
    allExpenses: List<Expense>,
    settlements: List<Balance>
) {
    var expanded by remember { mutableStateOf(false) }

    // 1. Filter data specific to this person
    val sharedExpenses = allExpenses.filter { it.sharedBy.contains(person) }
    val myDebts = settlements.filter { it.from == person }

    // 2. Calculate status
    val totalOwed = myDebts.sumOf { it.amount }
    val isSettled = totalOwed <= 0.0

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
            // HEADER ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name and Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = person,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Dots and Arrow
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSettled) {
                        // Green Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .padding(end = 4.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Settled",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    } else {
                        // Red Dot + Amount
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFE53935), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "-$${String.format("%.2f", totalOwed)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        "Toggle details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // EXPANDED DETAILS
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 1: Their Activity
                if (sharedExpenses.isEmpty()) {
                    Text(
                        "No shared expenses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Expenses Share:",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    sharedExpenses.forEach { expense ->
                        // Calculate simple equal split
                        val shareAmount = expense.totalAmount / expense.sharedBy.size

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = expense.itemName,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${String.format("%.2f", shareAmount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 2: Who they pay
                if (isSettled) {
                    Text(
                        text = "All settled up! No payments needed.",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "Payments needed:",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    myDebts.forEach { debt ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pay to ${debt.to}")
                            Text(
                                text = "$${String.format("%.2f", debt.amount)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------
// EXISTING COMPOSABLE: Expense Card
// ---------------------------------------------
@Composable
fun ExpenseCard(expense: Expense) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.itemName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Paid by ${expense.paidBy}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$${String.format("%.2f", expense.totalAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(text = "Quantity: ${expense.quantity}")
                if (expense.description.isNotEmpty()) {
                    Text(text = "Description: ${expense.description}")
                }
                Text(text = "Shared by: ${expense.sharedBy.joinToString(", ")}")
                if (expense.tags.isNotEmpty()) {
                    Text(text = "Tags: ${expense.tags.joinToString(", ")}")
                }
                Text(
                    text = "Created: ${dateFormat.format(Date(expense.createdAt))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.location.isNotEmpty()) {
                    Text(
                        text = "Location: ${expense.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}