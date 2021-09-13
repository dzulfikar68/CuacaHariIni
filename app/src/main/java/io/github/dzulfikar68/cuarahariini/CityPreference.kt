package io.github.dzulfikar68.cuarahariini

import android.app.Activity
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CityPreference {
    fun setCities(activity: Activity?, city: City) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val list = getCities(activity)
        list.add(city)
        val gson = Gson()
        val json = gson.toJson(list)
        if (json != null) {
            with(sharedPref.edit()) {
                putString("cities", json)
                commit()
            }
        }
    }
    fun getCities(activity: Activity?): MutableList<City> {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return mutableListOf()
        var arrayItems: MutableList<City> = mutableListOf()
        val serializedObject = sharedPref.getString("cities", null)
        if (serializedObject != null) {
            val gson = Gson()
            val type = object : TypeToken<List<City?>?>(){}.type
            arrayItems = gson.fromJson(serializedObject, type)
        }
        return arrayItems
    }
    fun delCities(activity: Activity?) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        sharedPref.edit().clear().apply()
    }
}