package com.example.splitsync.data


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.splitsync.data.DroppedPin
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import java.util.UUID
import kotlin.getOrElse
import kotlin.runCatching

private val Context.dataStore by preferencesDataStore(name = "pins_store")

//@Serializable
//data class DroppedPin(
//    val id: String = UUID.randomUUID().toString(),
//    val lat: Double,
//    val lng: Double,
//    val title: String? = null,
//    val note: String? = null
//)

object PinStore {
    private val PINS_KEY = stringPreferencesKey("dropped_pins_json")
    private val json = Json { ignoreUnknownKeys = true }

    fun pinsFlow(context: Context): Flow<List<DroppedPin>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[PINS_KEY] ?: "[]"
            runCatching { json.decodeFromString<List<DroppedPin>>(raw) }.getOrElse { emptyList() }
        }

    suspend fun savePins(context: Context, pins: List<DroppedPin>) {
        val raw = json.encodeToString(pins)
        context.dataStore.edit { it[PINS_KEY] = raw }
    }
}
