package com.example.mysantanderupdates

// Location services

// Utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mysantanderupdates.utils.BikeStation
import com.example.mysantanderupdates.utils.calculateDistance
import com.example.mysantanderupdates.utils.fetchBikeStations
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permission when the activity is created
        requestLocationPermission()

        setContent {
            BikeStationsScreen()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLastKnownLocation()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted ->
        if (granted) {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    // Pass the location to Composables via a state variable or ViewModel
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BikeStationsScreen() {
    val bikeStations = remember { mutableStateListOf<BikeStation>() }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var shouldRefresh by remember { mutableStateOf(true) }
    var shouldUpdateLocation by remember {mutableStateOf(true) }

    // Accessing the context in a composable scope
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Check for permissions
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Request location updates instead of just lastLocation
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation.let {
                    currentLocation = it
                    Log.d("LocationUpdate", "Location received: ${it.latitude}, ${it.longitude}")
                }
            }
        }
    }

    // Effect to handle location requests
    LaunchedEffect(locationPermissionState.status, shouldUpdateLocation) {
        if (shouldUpdateLocation) {
            when {
                locationPermissionState.status.isGranted -> {
                    // Check if location is enabled
                    val locationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val isLocationEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    if (isLocationEnabled) {
                        try {
                            // Request location updates
                            val locationRequest = LocationRequest.create().apply {
                                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                                numUpdates = 2
                                interval = 10000 // 10 seconds
                                fastestInterval = 1000 // 5 seconds
                            }

                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper()
                            )

                            // Also try to get last location as a fallback
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location: Location? ->
                                    if (location != null) {
                                        currentLocation = location
                                        Log.d(
                                            "LastLocation",
                                            "Location retrieved: ${location.latitude}, ${location.longitude}"
                                        )
                                    } else {
                                        Log.d("LastLocation", "Last location is null")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "LastLocation",
                                        "Error getting last location: ${e.message}"
                                    )
                                }
                            shouldUpdateLocation = false
                        } catch (e: SecurityException) {
                            Log.e("Location", "Security exception: ${e.message}")
                        }
                    } else {
                        // Show dialog to enable location
                        Toast.makeText(
                            context,
                            "Please enable location services",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                !locationPermissionState.status.isGranted -> {
                    // Request permission
                    locationPermissionState.launchPermissionRequest()
                }
            }
            shouldUpdateLocation = false
        }
    }

    // Cleanup location updates when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // LaunchedEffect to fetch bike stations
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            isLoading = true
            try {
                val stations = fetchBikeStations()
                bikeStations.clear()
                bikeStations.addAll(stations)
                Log.d("BikeStations", "Displayed ${stations.size} stations")
            } catch (e: Exception) {
                Log.e("BikeStations", "Error fetching bike stations: ${e.message}")
                Toast.makeText(context, "Failed to fetch bike stations", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                shouldRefresh = false
            }
        }
    }

    // Function to trigger refresh
    val refreshBikeStations: () -> Unit = {
        shouldRefresh = true
        shouldUpdateLocation = true
    }

    // Sorting the bike stations based on distance from current location
    val sortedStations = remember(currentLocation, bikeStations) {
        if (currentLocation != null) {
            bikeStations.sortedBy { station ->
                calculateDistance(
                    currentLocation!!.latitude, currentLocation!!.longitude,
                    station.lat, station.lon
                )
            }
        } else {
            bikeStations
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header with reload button and location status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bike Stations", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Location status indicator
            if (currentLocation == null) {
                Text(
                    "Location unavailable",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Text(
                    "Location: ${currentLocation!!.latitude}, ${currentLocation!!.longitude}",
                    color = Color.Green,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            IconButton(onClick = { refreshBikeStations() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload")
            }
        }

        // Search functionality
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                containerColor = Color.Transparent
            )
        )

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Display the sorted bike stations
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sortedStations.filter { it.name.contains(searchQuery, ignoreCase = true) }) { station ->
                    StationCard(station, currentLocation)
                }
            }
        }
    }
}

@Composable
fun StationCard(station: BikeStation, currentLocation: Location?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Station: ${station.name}", fontWeight = FontWeight.Bold)
            Text("Standard Bikes: ${station.nbStandardBikes}")
            Text("EBikes: ${station.nbEBikes}")
            Text("Empty Docks: ${station.nbEmptyDocks}")
            Text("Total Docks: ${station.nbDocks}")

            // Add distance if location is available
            currentLocation?.let {
                val distance = calculateDistance(
                    it.latitude, it.longitude,
                    station.lat, station.lon  // Changed from long to lon
                )
                Text(
                    "Distance: ${String.format("%.0f", distance * 1000)} m",
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}