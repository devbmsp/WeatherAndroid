package clima.tempo.weather.DataBase

import java.io.Serializable

data class Wind(
    val speed: Double,
    val deg: Int
) : Serializable