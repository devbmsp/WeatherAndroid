package clima.tempo.weather.Models

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

    object Constants {

        const val APP_ID: String = "d285e3db26867152cb3877abd3fa1ee5"
        const val BASE_URL: String = "https://api.openweathermap.org/data/"
        const val METRIC_UNIT: String = "metric"

        const val PREFERENCE_NAME = "WeatherAppPreference"
        const val WEATHER_RESPONSE_DATA = "weather_response_data"

        fun isNetworkAvailable(context: Context): Boolean {

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network      = connectivityManager.activeNetwork ?: return false
                val activeNetWork = connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    activeNetWork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    activeNetWork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    activeNetWork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnectedOrConnecting
            }
        }
    }