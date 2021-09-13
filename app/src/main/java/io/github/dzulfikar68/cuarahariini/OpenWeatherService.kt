package io.github.dzulfikar68.cuarahariini

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenWeatherService {
    @GET("data/2.5/onecall")
    fun listWeather(
        @Query("exclude") exclude: String = "minutely,hourly,alerts",
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("appid") appid: String = "164a6368bba755f68fe3107143be0e7f"
    ): Call<OpenWeatherResponse>

    @GET("static/data/country-cities/{code}/{code}.json")
    fun listCity(
        @Path("code") code: String
    ): Call<List<CityResponse>>

    @GET("name/{country}")
    fun getCountryByName(
        @Header("x-rapidapi-host") host: String = "restcountries-v1.p.rapidapi.com",
        @Header("x-rapidapi-key") key: String = "06f2f0a5cbmsh1ea5e65e3796f8cp1bb3adjsn6cf46db330dd",
        @Path("country") country: String
    ): Call<List<Country>>
}