package com.example.splitsync.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.splitsync.data.DataManager
import com.example.splitsync.data.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dataManager: DataManager,
    onNavigateToTrip: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCreateTrip: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        dataManager.loadData(context)
    }

    val trips = dataManager.getTrips()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SplitSync") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToMap()
                    },
                    icon = { Icon(Icons.Default.Place, "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToHistory()
                    },
                    icon = { Icon(Icons.Default.DateRange, "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToSettings()
                    },
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateTrip) {
                Icon(Icons.Default.Add, "Create Trip")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Trips",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (trips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.List,
                            "No trips",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No trips yet. Create one!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trips) { trip ->
                        TripCard(
                            trip = trip,
                            dataManager = dataManager,
                            onClick = { onNavigateToTrip(trip.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    dataManager: DataManager,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var showPhotoOptions by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            dataManager.updateTripPhoto(trip.id, it.toString(), context)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Photo section (if exists)
            trip.photoUri?.let { photoUri ->
                AsyncImage(
                    model = Uri.parse(photoUri),
                    contentDescription = "Trip photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Trip info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip.location,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${trip.participants.size} participants",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Photo action button
                IconButton(
                    onClick = { showPhotoOptions = true }
                ) {
                    Icon(
                        if (trip.photoUri != null) Icons.Default.Edit else Icons.Default.AddAPhoto,
                        contentDescription = if (trip.photoUri != null) "Change photo" else "Add photo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "View trip"
                )
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