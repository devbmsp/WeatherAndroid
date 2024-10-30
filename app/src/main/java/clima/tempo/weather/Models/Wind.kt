package clima.tempo.weather.Models

import java.io.Serializable

data class Wind(
    val speed: Double,
    val deg: Int
) : Serializable