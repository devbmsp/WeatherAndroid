package clima.tempo.weather.DataBase

import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface WeatherServices {
    @GET("2.5/weather") // Mudar conforme a versão da API
    fun getWeather (
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appID: String?,
        @Query("units") units: String?,
    ) : Call<WeatherResponse>
}