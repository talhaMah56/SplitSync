// ============================================
// FILE 12: MapScreen.kt
// Location: app/src/main/java/com/example/splitsync/screens/MapScreen.kt
// ============================================
package com.example.splitsync.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf("Requesting location...") }
    var hasPermission by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    } else {
                        currentLocation = "Location unavailable"
                    }
                }
            } catch (e: SecurityException) {
                currentLocation = "Permission denied"
            }
        } else {
            currentLocation = "Permission denied"
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission = true
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                        } else {
                            currentLocation = "Location unavailable"
                        }
                    }
                } catch (e: SecurityException) {
                    currentLocation = "Permission denied"
                }
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Location",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Current Location")

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(currentLocation)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To view on Google Maps, open the location in your browser",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (currentLocation.contains("Lat:")) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val coords = currentLocation.replace("Lat: ", "").replace(" Lon: ", ",")
                        // In a real app, you would open Google Maps here
                    }
                ) {
                    Icon(Icons.Default.LocationOn, "Open Maps")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View on Google Maps")
                }
            }
        }
    }
}
