package com.example.mysantanderupdates.utils

import kotlin.math.*

/**
 * Calculates the distance between two coordinates on Earth using the Haversine formula.
 * @param lat1 Latitude of the first point (in decimal degrees).
 * @param lon1 Longitude of the first point (in decimal degrees).
 * @param lat2 Latitude of the second point (in decimal degrees).
 * @param lon2 Longitude of the second point (in decimal degrees).
 * @return The distance between the two points in kilometers.
 */
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val radius = 6371.0 // Radius of Earth in kilometers

    // Convert degrees to radians
    val lat1Rad = Math.toRadians(lat1)
    val lon1Rad = Math.toRadians(lon1)
    val lat2Rad = Math.toRadians(lat2)
    val lon2Rad = Math.toRadians(lon2)

    // Difference in coordinates
    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    // Haversine formula
    val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    // Distance in kilometers
    return radius * c
}