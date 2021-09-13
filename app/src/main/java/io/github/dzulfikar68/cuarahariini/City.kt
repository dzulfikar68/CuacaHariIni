package io.github.dzulfikar68.cuarahariini

import android.os.Parcelable
import io.github.dzulfikar68.cuarahariini.Utils.capitalizeWords
import kotlinx.parcelize.Parcelize

@Parcelize
data class City (
    val id: Long,
    val name: String,
    val country: String,
    var lat: Double = 0.0,
    var long: Double = 0.0
): Parcelable {
    override fun toString(): String {
        return name.capitalizeWords()
    }
}