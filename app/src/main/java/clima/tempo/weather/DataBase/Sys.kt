package clima.tempo.weather.DataBase

import java.io.Serializable

data class Sys(
    val type: Int,
    val message: Double,
    val country: String,
    val sunrise: Long,
    val sunset: Long
) : Serializable