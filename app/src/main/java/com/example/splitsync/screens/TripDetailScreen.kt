package com.example.splitsync.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    var showPhotoOptions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dataManager.loadData(context)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            dataManager.updateTripPhoto(tripId, it.toString(), context)
        }
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
            // Trip Photo Header (NEW)
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box {
                        if (trip.photoUri != null) {
                            AsyncImage(
                                model = Uri.parse(trip.photoUri),
                                contentDescription = "Trip photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Add a trip photo",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Floating add/edit photo button
                        FloatingActionButton(
                            onClick = { showPhotoOptions = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                if (trip.photoUri != null) Icons.Default.Edit else Icons.Default.AddAPhoto,
                                contentDescription = if (trip.photoUri != null) "Change photo" else "Add photo"
                            )
                        }
                    }
                }
            }

            // Trip Detail Card
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

            // Balances Section
            if (showBalances) {
                item {
                    Text(
                        text = "Breakdown by Person",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(trip.participants) { person ->
                    ParticipantBalanceCard(
                        person = person,
                        allExpenses = expenses,
                        settlements = balances
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Expenses Section
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

    // Photo options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Trip Photo") },
            text = { Text("Choose a memorable photo for this trip") },
            confirmButton = {
                TextButton(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                        showPhotoOptions = false
                    }
                ) {
                    Text("Select Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ParticipantBalanceCard(
    person: String,
    allExpenses: List<Expense>,
    settlements: List<Balance>
) {
    var expanded by remember { mutableStateOf(false) }

    val sharedExpenses = allExpenses.filter { it.sharedBy.contains(person) }
    val myDebts = settlements.filter { it.from == person }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = person,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSettled) {
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

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

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