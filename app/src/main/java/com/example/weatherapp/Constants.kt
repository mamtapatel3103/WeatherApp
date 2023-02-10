package com.example.weatherapp

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object Constants {
    var baseUrl_Weather: String =
        "https://api.openweathermap.org/data/2.5/weather?units=metric&appid="
    var api_key: String = "9b5ae1a801024b50acc4d3434ef4fb08"
    fun unixTimestampToTimeString(time: Int, timezone: Int?): String {
        val outputDateFormat = SimpleDateFormat("hh:mm a")

        val calndr = Calendar.getInstance()
        calndr.timeInMillis = ((time + timezone!!) * 1000).toLong()
        return outputDateFormat.format(calndr.time)
    }

    fun convertMeterspersecToMilesperhour(Meterspersec: Double): Int {
        return (Meterspersec * 2.23694).roundToInt()
    }

    fun metersToMiles(meters: Int): Int {
        return (meters * 0.00062137119).roundToInt()
    }
}
