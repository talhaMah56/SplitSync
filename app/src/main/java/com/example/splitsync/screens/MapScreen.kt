package com.example.splitsync.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var mapLoadError by remember { mutableStateOf<String?>(null) }
    var showTimeout by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationFetched by remember { mutableStateOf(false) }

    // Default to Philadelphia (fallback)
    val defaultPosition = LatLng(39.9526, -75.1652)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 12f)
    }

    // Function to get and set location
    fun fetchAndSetLocation() {
        if (!hasPermission) return
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLocation = latLng
                    if (!locationFetched) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                        locationFetched = true
                        Log.d("MapScreen", "Location centered: $latLng")
                    }
                } ?: run {
                    Log.d("MapScreen", "Location is null, using default")
                }
            }.addOnFailureListener { e ->
                Log.e("MapScreen", "Failed to get location", e)
            }
        } catch (e: SecurityException) {
            Log.e("MapScreen", "Security exception", e)
        }
    }

    // Timeout checker
    LaunchedEffect(Unit) {
        delay(5000) // 5 second timeout
        if (!isMapLoaded) {
            showTimeout = true
            Log.e("MapScreen", "Map failed to load within timeout")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            fetchAndSetLocation()
        }
    }

    LaunchedEffect(Unit) {
        Log.d("MapScreen", "Initializing map...")
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission = true
                Log.d("MapScreen", "Permission already granted, fetching location")
                fetchAndSetLocation()
            }
            else -> {
                Log.d("MapScreen", "Requesting permission")
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Also fetch location when map is loaded
    LaunchedEffect(isMapLoaded) {
        if (isMapLoaded && hasPermission && !locationFetched) {
            Log.d("MapScreen", "Map loaded, fetching location")
            fetchAndSetLocation()
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
                },
                actions = {
                    if (!isMapLoaded && !showTimeout) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasPermission && currentLocation != null && isMapLoaded) {
                FloatingActionButton(
                    onClick = {
                        currentLocation?.let {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                        } ?: run {
                            // Try fetching location again
                            fetchAndSetLocation()
                        }
                    }
                ) {
                    Icon(Icons.Default.Place, "My Location")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showTimeout && !isMapLoaded) {
                // Timeout error view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Map Failed to Load",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Possible causes:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "• Missing or invalid Google Maps API key\n" +
                                        "• Maps SDK for Android not enabled\n" +
                                        "• Network connectivity issues\n" +
                                        "• Emulator Google Play Services outdated",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = {
                            showTimeout = false
                            isMapLoaded = false
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Retry")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Check Logcat for detailed error messages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = hasPermission,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        compassEnabled = true,
                        mapToolbarEnabled = true,
                        zoomGesturesEnabled = true,
                        scrollGesturesEnabled = true,
                        tiltGesturesEnabled = true,
                        rotationGesturesEnabled = true
                    ),
                    onMapLoaded = {
                        isMapLoaded = true
                        showTimeout = false
                        Log.d("MapScreen", "✅ Map loaded successfully!")
                    }
                ) {
                    currentLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "You are here",
                            snippet = "Current location"
                        )
                    }
                }

                // Permission overlay
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
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location",
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                "Location Permission Required",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "To show your location on the map, please grant location permission.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            ) {
                                Icon(Icons.Default.LocationOn, "Grant Permission")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }
        }
    }
}