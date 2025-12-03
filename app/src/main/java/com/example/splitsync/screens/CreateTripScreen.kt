// ============================================
// FILE: CreateTripScreen.kt
// Location: app/src/main/java/com/example/splitsync/screens/CreateTripScreen.kt
// ============================================
package com.example.splitsync.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitsync.data.DataManager
import com.example.splitsync.data.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    dataManager: DataManager,
    onTripCreated: () -> Unit,
    onBack: () -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var participantEmail by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf(listOf(dataManager.currentUserEmail)) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Trip") },
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
                value = tripName,
                onValueChange = {
                    tripName = it
                    errorMessage = ""
                },
                label = { Text("Trip Name") },
                leadingIcon = { Icon(Icons.Default.LocationOn, "Trip") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    errorMessage = ""
                },
                label = { Text("Location") },
                leadingIcon = { Icon(Icons.Default.Place, "Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    errorMessage = ""
                },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Info, "Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Text(
                text = "Add Participants",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = participantEmail,
                    onValueChange = { participantEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (participantEmail.isNotEmpty() && participantEmail.contains("@")) {
                            if (!participants.contains(participantEmail)) {
                                participants = participants + participantEmail
                                participantEmail = ""
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, "Add participant")
                }
            }

            participants.forEach { email ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Person, "Participant")
                            Text(email)
                        }
                        if (email != dataManager.currentUserEmail) {
                            IconButton(
                                onClick = {
                                    participants = participants.filter { it != email }
                                }
                            ) {
                                Icon(Icons.Default.Close, "Remove")
                            }
                        }
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (tripName.isEmpty() || location.isEmpty()) {
                        errorMessage = "Please fill trip name and location"
                        return@Button
                    }

                    val trip = Trip(
                        name = tripName,
                        location = location,
                        description = description,
                        participants = participants,
                        createdBy = dataManager.currentUserEmail
                    )

                    dataManager.saveTrip(trip, context)
                    onTripCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Create Trip", fontSize = 16.sp)
            }
        }
    }
}