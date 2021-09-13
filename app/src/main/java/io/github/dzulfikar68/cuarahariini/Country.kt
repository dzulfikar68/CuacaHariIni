package io.github.dzulfikar68.cuarahariini

import android.os.Parcelable
import io.github.dzulfikar68.cuarahariini.Utils.capitalizeWords
import kotlinx.parcelize.Parcelize

@Parcelize
data class Country(
    val code: String,
    val name: String?,
    val capital: String? = null,
    val population: Long? = null,
    val callingCodes: List<String>? = null,
    val latlng: List<Double>? = null
): Parcelable {
    override fun toString(): String {
        return name?.capitalizeWords() ?: "-"
    }
}