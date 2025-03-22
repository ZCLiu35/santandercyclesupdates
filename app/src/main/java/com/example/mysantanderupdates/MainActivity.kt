package com.example.mysantanderupdates

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf

// Import network utils
import com.example.mysantanderupdates.utils.fetchBikeStations
import com.example.mysantanderupdates.utils.BikeStation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BikeStationsScreen()
        }
    }
}

@Composable
fun BikeStationsScreen() {
    val bikeStations = remember { mutableStateListOf<BikeStation>() }

    LaunchedEffect(Unit) {
        // Fetch the bike stations using the coroutine
        val stations = fetchBikeStations()

        bikeStations.clear()
        bikeStations.addAll(stations)
        Log.d("LaunchEffect", "Added ${bikeStations.size} stations")
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Log.d("DisplayBikeStations","Number of stations: ${bikeStations.size}")
        if (bikeStations.isEmpty()) {
            Text("No bike stations found or failed to fetch data.")
        } else {
            bikeStations.forEach { station ->
                Text(text = "Station: ${station.name}", modifier = Modifier.padding(4.dp))
                Text(text = "Standard Bikes: ${station.nbStandardBikes}", modifier = Modifier.padding(4.dp))
                Text(text = "EBikes: ${station.nbEBikes}", modifier = Modifier.padding(4.dp))
                Text(text = "Empty Docks: ${station.nbEmptyDocks}", modifier = Modifier.padding(4.dp))
                Text(text = "Total Docks: ${station.nbDocks}", modifier = Modifier.padding(4.dp))
            }
        }
    }
}