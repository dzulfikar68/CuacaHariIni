package io.github.dzulfikar68.cuarahariini

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class JsonParserLocation(private val context: Context) {

    private fun parsingFileToStringForCountry(): String? {
        return try {
            val `is` = context.assets.open("list_country.json")
            val buffer = ByteArray(`is`.available())
            `is`.read(buffer)
            `is`.close()
            String(buffer)

        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    fun getDataCountryDataSet(): List<Country> {
        val listCountry = mutableListOf<Country>()
        try {
            val listObject = JSONObject(parsingFileToStringForCountry().toString())
            val keys = listObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                listCountry.add(Country(key, listObject.get(key) as String))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return listCountry
    }

    fun getDataCityDataSet(): List<City> {
        val list = ArrayList<City>()
        try {
            val listArray = JSONArray(parsingFileToStringForLocation().toString())
//            val listArray = responseObject.getJSONArray("data")
            for (i in 0 until listArray.length()) {
                val course = listArray.getJSONObject(i)

                val kabko = course.getString("kabko")
                val lat = course.getDouble("lat")
                val long = course.getDouble("long")

                val city = City(0, kabko, "ID", lat, long)
                list.add(city)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return list
    }

    private fun parsingFileToStringForLocation(): String? {
        return try {
            val `is` = context.assets.open("list_city.json")
            val buffer = ByteArray(`is`.available())
            `is`.read(buffer)
            `is`.close()
            String(buffer)

        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}