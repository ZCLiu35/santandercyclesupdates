package com.example.mysantanderupdates

// Import network utils
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysantanderupdates.utils.BikeStation
import com.example.mysantanderupdates.utils.fetchBikeStations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BikeStationsScreen()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeStationsScreen() {
    val bikeStations = remember { mutableStateListOf<BikeStation>() }

    suspend fun reloadData() {
        val stations = fetchBikeStations()
        bikeStations.clear()
        bikeStations.addAll(stations)
        Log.d("Reload", "Reloaded ${bikeStations.size} stations")
    }

    LaunchedEffect(Unit) { reloadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MySantanderUpdates") },
                actions = {
                    IconButton(onClick = { CoroutineScope(Dispatchers.IO).launch { reloadData() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload Data")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Ensure content doesn't overlap with the app bar
                .padding(16.dp)
        ) {
            if (bikeStations.isEmpty()) {
                Text("No bike stations found or failed to fetch data.")
            } else {
                LazyColumn {
                    items(bikeStations) { station ->
                        StationItem(station)
                    }
                }
            }
        }
    }
}


@Composable
fun StationItem(station: BikeStation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = "Station: ${station.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = "Standard Bikes: ${station.nbStandardBikes}")
        Text(text = "E-Bikes: ${station.nbEBikes}")
        Text(text = "Empty Docks: ${station.nbEmptyDocks}")
        Text(text = "Total Docks: ${station.nbDocks}")
    }
}