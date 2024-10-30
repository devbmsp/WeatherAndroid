package clima.tempo.weather.Models

import java.io.Serializable

data class Main(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double
) : Serializable