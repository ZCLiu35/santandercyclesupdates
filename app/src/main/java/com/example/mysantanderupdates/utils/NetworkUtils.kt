package com.example.mysantanderupdates.utils // Change the package name to match your app's package

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toLongOrDefault
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory


// Data model class should be here or imported
data class BikeStation(
    var id: String = "",
    var name: String = "",
    var terminalName: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var installed: Boolean = false,
    var locked: Boolean = false,
    var installDate: Long = 0L,
    var removalDate: Long? = null,
    var temporary: Boolean = false,
    var nbBikes: Int = 0,
    var nbStandardBikes: Int = 0,
    var nbEBikes: Int = 0,
    var nbEmptyDocks: Int = 0,
    var nbDocks: Int = 0
)

// Function to fetch the bike stations from the API
suspend fun fetchBikeStations(): List<BikeStation> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml")
        .build()

    return withContext(Dispatchers.IO) {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val stations = parseXml(response.body?.string())
            Log.d("BikeStation", "Stations fetched: ${stations.size}")
            return@withContext stations
        } else {
            Log.e("BikeStation", "Failed to fetch stations: ${response.code}")
            return@withContext emptyList<BikeStation>()
        }
    }
}

// Function to parse the XML response
fun parseXml(xml: String?): List<BikeStation> {
    val stations = mutableListOf<BikeStation>()
    if (xml.isNullOrEmpty()) return stations

    val parserFactory = XmlPullParserFactory.newInstance()
    val parser = parserFactory.newPullParser()
    parser.setInput(xml.reader())

    var eventType = parser.eventType
    var currentStation: BikeStation? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "station" -> currentStation = BikeStation() // Default values used
                    "id" -> currentStation?.id = parser.nextText()
                    "name" -> currentStation?.name = parser.nextText()
                    "terminalName" -> currentStation?.terminalName = parser.nextText()
                    "lat" -> currentStation?.lat = parser.nextText().toDouble()
                    "long" -> currentStation?.lon = parser.nextText().toDouble()
                    "installed" -> currentStation?.installed = parser.nextText().toBoolean()
                    "locked" -> currentStation?.locked = parser.nextText().toBoolean()
                    "installDate" -> currentStation?.installDate = parser.nextText().toLongOrDefault(0L)
                    "removalDate" -> currentStation?.removalDate = parser.nextText().takeIf { it.isNotEmpty() }?.toLong()
                    "temporary" -> currentStation?.temporary = parser.nextText().toBoolean()
                    "nbBikes" -> currentStation?.nbBikes = parser.nextText().toInt()
                    "nbStandardBikes" -> currentStation?.nbStandardBikes = parser.nextText().toInt()
                    "nbEBikes" -> currentStation?.nbEBikes = parser.nextText().toInt()
                    "nbEmptyDocks" -> currentStation?.nbEmptyDocks = parser.nextText().toInt()
                    "nbDocks" -> currentStation?.nbDocks = parser.nextText().toInt()
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "station" && currentStation != null) {
                    stations.add(currentStation)
                }
            }
        }
        eventType = parser.next()
    }

    return stations
}
