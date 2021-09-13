package io.github.dzulfikar68.cuarahariini

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenWeatherResponse (
    val lat: Double? = 0.0,
    val lon: Double? = 0.0,
    val timezone: String? = "-",
    val current: CurrentResponse? = null,
    val daily: List<DailyResponse>? = null
): Parcelable

@Parcelize
data class TemperatureResponse (
    val day: Double? = null
): Parcelable

@Parcelize
data class DailyResponse (
    val dt: Long? = null,
    val weather: List<WeatherResponse>? = null,
    val temp: TemperatureResponse? = null
): Parcelable

@Parcelize
data class CurrentResponse (
    val dt: Long? = null,
    val temp: Double? = null,
    val pressure: Int? = null,
    val humidity: Int? = null,
    val wind_deg: Int? = null,
    val weather: List<WeatherResponse>? = null
): Parcelable

@Parcelize
data class WeatherResponse (
    val id: Long? = null,
    val main: String? = null,
    val description: String? = null
): Parcelable

@Parcelize
data class CityResponse (
    val city: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val country: String? = null,
    val iso2: String? = null,
    val admin_name: String? = null,
    val capital: String? = null,
    val population: String? = null,
    val population_proper: String? = null
): Parcelable

