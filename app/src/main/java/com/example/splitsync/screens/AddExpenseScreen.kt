// ============================================
// FILE 9: AddExpenseScreen.kt
// Location: app/src/main/java/com/example/splitsync/screens/AddExpenseScreen.kt
// ============================================
package com.example.splitsync.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.splitsync.data.DataManager
import com.example.splitsync.data.Expense
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    tripId: String,
    dataManager: DataManager,
    onExpenseAdded: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val trip = dataManager.getTrip(tripId)

    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var totalAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedPaidBy by remember { mutableStateOf(dataManager.currentUserEmail) }
    var selectedSharedBy by remember { mutableStateOf(setOf<String>()) }
    var errorMessage by remember { mutableStateOf("") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        location = "${loc.latitude}, ${loc.longitude}"
                    }
                }
            } catch (e: SecurityException) {
                location = "Location unavailable"
            }
        }
    }

    LaunchedEffect(trip) {
        if (trip != null) {
            selectedSharedBy = trip.participants.toSet()
        }
    }

    if (trip == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Trip not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = {
                    itemName = it
                    errorMessage = ""
                },
                label = { Text("Item Name") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, "Item") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        errorMessage = ""
                    },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = {
                        totalAmount = it
                        errorMessage = ""
                    },
                    label = { Text("Total Amount") },
                    leadingIcon = { Text("$") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Info, "Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                leadingIcon = { Icon(Icons.Default.Star, "Tags") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Paid By",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            trip.participants.forEach { participant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaidBy == participant,
                        onClick = { selectedPaidBy = participant }
                    )
                    Text(participant)
                }
            }

            Text(
                text = "Shared By",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            trip.participants.forEach { participant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedSharedBy.contains(participant),
                        onCheckedChange = { checked ->
                            selectedSharedBy = if (checked) {
                                selectedSharedBy + participant
                            } else {
                                selectedSharedBy - participant
                            }
                        }
                    )
                    Text(participant)
                }
            }

            Button(
                onClick = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        location = "${loc.latitude}, ${loc.longitude}"
                                    } else {
                                        location = "Location unavailable"
                                    }
                                }
                            } catch (e: SecurityException) {
                                location = "Location unavailable"
                            }
                        }
                        else -> {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Place, "Location")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (location.isEmpty()) "Add Current Location" else "Location Added")
            }

            if (location.isNotEmpty()) {
                Text(
                    text = "Location: $location",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (itemName.isEmpty() || totalAmount.isEmpty()) {
                        errorMessage = "Please fill item name and amount"
                        return@Button
                    }

                    val amount = totalAmount.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }

                    if (selectedSharedBy.isEmpty()) {
                        errorMessage = "Please select at least one person to share with"
                        return@Button
                    }

                    val tagsList = if (tags.isNotEmpty()) {
                        tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else {
                        emptyList()
                    }

                    val expense = Expense(
                        tripId = tripId,
                        itemName = itemName,
                        quantity = quantity.toIntOrNull() ?: 1,
                        totalAmount = amount,
                        description = description,
                        tags = tagsList,
                        sharedBy = selectedSharedBy.toList(),
                        paidBy = selectedPaidBy,
                        createdBy = dataManager.currentUserEmail,
                        location = location
                    )

                    dataManager.saveExpense(expense, context)
                    onExpenseAdded()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save Expense", fontSize = 16.sp)
            }
        }
    }
}

