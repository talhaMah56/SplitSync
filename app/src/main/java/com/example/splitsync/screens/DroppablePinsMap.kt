package com.example.splitsync.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import com.example.splitsync.data.DroppedPin
import com.example.splitsync.data.PinStore
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.collections.removeAll

@Composable
fun DroppablePinsMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    hasPermission: Boolean,
    onMapLoaded: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pins = remember { mutableStateListOf<DroppedPin>() }

    // Load pins (keeps in sync with DataStore)
    LaunchedEffect(Unit) {
        PinStore.pinsFlow(context).collect { stored ->
            pins.clear()
            pins.addAll(stored)
        }
    }

    // Debounced save helper
    var saveJob by remember { mutableStateOf<Job?>(null) }
    fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(250)
            PinStore.savePins(context, pins.toList())
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasPermission),
        uiSettings = MapUiSettings(myLocationButtonEnabled = false),
        onMapLoaded = onMapLoaded,

        // ✅ long-press drops a pin
        onMapLongClick = { latLng ->
            pins.add(DroppedPin(lat = latLng.latitude, lng = latLng.longitude))
            scheduleSave()
        },
    ) {
        // IMPORTANT: Only map content here (Marker/Polyline/etc). No UI composables.
        pins.forEach { pin ->
            Marker(
                state = MarkerState(position = LatLng(pin.lat, pin.lng)),
                title = pin.title ?: "Dropped pin",
                snippet = pin.note ?: "${pin.lat}, ${pin.lng}",

                // Optional: tap marker to delete it
                onClick = {
                    pins.removeAll { it.id == pin.id }
                    scheduleSave()
                    true
                }
            )
        }
    }
}
