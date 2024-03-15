package org.jom.supplier

import android.os.Build
import android.webkit.CookieManager
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Base64
import java.util.*
import java.util.Locale

class Methods {
    private val backendUrl = "http://10.0.2.2:8090/"
    private val artifact = "JOM_war_exploded"

    fun getBackendUrl(): String {
        return this.backendUrl
    }

    fun getArtifact(): String {
        return this.artifact
    }

    fun getAllCookies(cookieManager: CookieManager): List<Pair<String, String>> {
        // Get all cookies
        val allCookies = cookieManager.getCookie("10.0.2.2")

        // Initialize a list to store the parsed cookies
        val parsedCookies = mutableListOf<Pair<String, String>>()

        // Check if allCookies is not null or empty
        if (!allCookies.isNullOrBlank()) {
            // Split the cookies string into individual cookies
            val cookieArray = allCookies.split("; ")

            // Iterate through each cookie and parse it
            for (cookie in cookieArray) {
                // Split the cookie string into name and value
                val parts = cookie.split("=")
                if (parts.size == 2) {
                    // Add the parsed cookie to the list
                    parsedCookies.add(Pair(parts[0], parts[1]))
                }
            }
        }
        return parsedCookies
    }

    fun convertTime(inputTime: String): String {
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val date = inputFormat.parse(inputTime)
        return outputFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPayload(token: String): Map<String, Any> {
        val payload = token.split(".")[1]
        val decodedBytes = Base64.getDecoder().decode(payload)
        val decodedString = String(decodedBytes)
        return jsonToMap(decodedString)
    }

    fun jsonToMap(jsonString: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(jsonString, mapType)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGreetingTime(m: LocalTime?): String? {
        var g: String? = null //return g
        if (m == null) {
            return null
        }
        //if we can't find a valid or filled moment, we return.

        val splitAfternoon = 12 //24hr time to split the afternoon
        val splitEvening = 17 //24hr time to split the evening
        val currentHour = m.hour

        g = when {
            currentHour >= splitAfternoon && currentHour <= splitEvening -> "Good Afternoon"
            currentHour >= splitEvening -> "Good Evening"
            else -> "Good Morning"
        }
        return g
    }

    fun formatAmount(amount: Double): String {
        val locale = Locale("en", "US")
        val numberFormat = NumberFormat.getInstance(locale)
        return numberFormat.format(amount)
    }

    fun floatToInt(s: Any?): Int {
        return s.toString().toFloat().toInt()
    }

    fun checkDate(date: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = format.parse(date)
        val now = Calendar.getInstance()
        now.add(Calendar.DATE, -1)
        return selectedDate.after(now.time)
    }
}