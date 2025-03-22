package com.example.mysantanderupdates

// Import network utils
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*

import androidx.compose.foundation.shape.RoundedCornerShape
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
    val filteredStations = remember { mutableStateListOf<BikeStation>() }
    var searchQuery by remember { mutableStateOf("") }  // Corrected here

    suspend fun reloadData() {
        val stations = fetchBikeStations()
        bikeStations.clear()
        bikeStations.addAll(stations)
        filteredStations.clear()
        filteredStations.addAll(stations)
        Log.d("Reload", "Reloaded ${bikeStations.size} stations")
    }

    LaunchedEffect(Unit) { reloadData() }

    // Update filtered list based on the search query
    val filteredList = filteredStations.filter { station ->
        station.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { CoroutineScope(Dispatchers.IO).launch { reloadData() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload Data")
                    }
                    // Search bar in the app bar
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search") },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                    )
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
            if (filteredList.isEmpty()) {
                Text("No bike stations found or failed to fetch data.")
            } else {
                LazyColumn {
                    items(filteredList) { station ->
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