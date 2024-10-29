package clima.tempo.weather.DataBase

import java.io.Serializable

data class Coord(
    val lon: Double,
    val lat: Double
) : Serializable