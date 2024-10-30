// MainViewModel.kt
package clima.tempo.weather.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import clima.tempo.weather.Models.Constants
import clima.tempo.weather.Models.WeatherResponse
import clima.tempo.weather.Models.WeatherServices
import com.google.gson.Gson
import retrofit.*
import retrofit.GsonConverterFactory

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> get() = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val sharedPreferences = application.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

    // Método para obter os dados meteorológicos
    fun fetchWeatherData(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(getApplication())) {
            _isLoading.value = true

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherServices = retrofit.create(WeatherServices::class.java)
            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.APP_ID, Constants.METRIC_UNIT
            )

            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    response: Response<WeatherResponse>?,
                    retrofit: Retrofit?
                ) {
                    _isLoading.value = false
                    if (response!!.isSuccess) {
                        val weatherList: WeatherResponse = response.body()
                        _weatherData.value = weatherList

                        // Salva os dados no SharedPreferences
                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = sharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()
                    } else {
                        _errorMessage.value = "Erro ${response.code()}: ${response.message()}"
                    }
                }

                override fun onFailure(t: Throwable?) {
                    _isLoading.value = false
                    _errorMessage.value = t?.message
                }
            })
        } else {
            _errorMessage.value = "Sem conexão com a internet."
        }
    }

    // Método para carregar dados do SharedPreferences
    fun loadWeatherDataFromPrefs() {
        val weatherResponseJsonString = sharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")
        if (!weatherResponseJsonString.isNullOrEmpty()) {
            val weatherList = Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)
            _weatherData.value = weatherList
        }
    }
}
