package com.example.splitsync.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlin.let


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var permissionRequestInFlight by remember { mutableStateOf(false) }

    var isMapLoaded by remember { mutableStateOf(false) }
    var showTimeout by remember { mutableStateOf(false) }

    // Default to Philadelphia
    val defaultPosition = LatLng(39.9526, -75.1652)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 12f)
    }

    var locationFetched by remember { mutableStateOf(false) }

    fun fetchAndSetLocation() {
        if (!hasPermission) return
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        currentLocation = latLng
                        if (!locationFetched) {
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(latLng, 15f)
                            locationFetched = true
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MapScreen", "Failed to get location", e)
                }
        } catch (e: SecurityException) {
            Log.e("MapScreen", "Security exception", e)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionRequestInFlight = false
        hasPermission = isGranted
        if (isGranted) fetchAndSetLocation()
    }

    // Initial permission check (don’t auto-launch dialog here)
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) fetchAndSetLocation()
    }

    // Timeout guard that doesn’t run while permission dialog is active
    LaunchedEffect(permissionRequestInFlight) {
        showTimeout = false
        delay(12000)
        if (!isMapLoaded && !permissionRequestInFlight) showTimeout = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasPermission && currentLocation != null && isMapLoaded) {
                FloatingActionButton(
                    onClick = {
                        currentLocation?.let {
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(it, 15f)
                        } ?: fetchAndSetLocation()
                    }
                ) {
                    Icon(Icons.Default.Place, contentDescription = "My Location")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ Map is in normal UI tree (NOT inside another GoogleMap content lambda)
            DroppablePinsMap(
                cameraPositionState = cameraPositionState,
                hasPermission = hasPermission,
                onMapLoaded = {
                    isMapLoaded = true
                    showTimeout = false
                    if (hasPermission && !locationFetched) fetchAndSetLocation()
                }
            )

            // ✅ Timeout overlay (UI composables are OUTSIDE map)
            if (showTimeout && !isMapLoaded) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Map is taking too long to load",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Check API key / Play Services / network. Then retry.",
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = {
                            // just reset flags; map stays mounted
                            showTimeout = false
                            isMapLoaded = false
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }

            // ✅ Permission overlay (UI composables are OUTSIDE map)
            if (!hasPermission) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Location Permission Required", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Long-press to drop pins. To show your current location, grant permission.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            enabled = !permissionRequestInFlight,
                            onClick = {
                                permissionRequestInFlight = true
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        ) {
                            Text(if (permissionRequestInFlight) "Requesting..." else "Grant Permission")
                        }
                    }
                }
            }
        }
    }
}
