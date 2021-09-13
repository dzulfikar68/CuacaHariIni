package io.github.dzulfikar68.cuarahariini

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    @SuppressLint("SimpleDateFormat")
    fun timestampToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("EEE, d MMM yyyy\nh:mm a")
        return format.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun timestampToDay(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("EEE, d MMM")
        return format.format(date)
    }

    fun String.capitalizeWords(): String = split(" ").map { it.capitalize(Locale.getDefault()) }.joinToString(" ")

    fun weatherToImage(string: String?): String {
        return when (string) {
            "Clouds" -> "https://cdn-icons-png.flaticon.com/512/1163/1163661.png"
            "Rain" -> "https://cdn-icons-png.flaticon.com/512/1163/1163657.png"
            "Clear" -> "https://cdn-icons-png.flaticon.com/512/869/869869.png"
            else -> "https://www.nicepng.com/png/full/123-1236627_haze-icon-png-haze-weather-icon.png"
        }
    }
}