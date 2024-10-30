// MainActivity.kt
package clima.tempo.weather.View

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import clima.tempo.weather.Models.WeatherResponse
import clima.tempo.weather.R
import clima.tempo.weather.ViewModel.MainViewModel
import clima.tempo.weather.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.ImageView
import android.widget.TextView
import clima.tempo.weather.Models.Constants
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa a ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MainViewModel::class.java)

        binding.btnDeslogar.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val backLogin = Intent(this, LoginActivity::class.java)
            startActivity(backLogin)
            finish()
        }

        setupObservers()
    }

    override fun onStart() {
        super.onStart()

        val userAtual = FirebaseAuth.getInstance().currentUser
        if (userAtual == null) {
            // Se não estiver logado, volta para a tela de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Carrega dados do SharedPreferences
        viewModel.loadWeatherDataFromPrefs()

        // Busca novos dados
        // Coordenadas de Curitiba
        val latitude = -25.4284
        val longitude = -49.2733
        viewModel.fetchWeatherData(latitude, longitude)
    }

    private fun setupObservers() {
        viewModel.weatherData.observe(this, Observer { weatherResponse ->
            if (weatherResponse != null) {
                setupUI(weatherResponse)
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showCustomProgressDialog()
            } else {
                hideProgressDialog()
            }
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh -> {
                // Atualiza os dados
                val latitude = -25.4284
                val longitude = -49.2733
                viewModel.fetchWeatherData(latitude, longitude)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI(weatherList: WeatherResponse) {
        for (i in weatherList.weather.indices) {
            val tv_main = findViewById<TextView>(R.id.tv_main)
            val tv_main_description = findViewById<TextView>(R.id.tv_main_description)
            val tv_temp = findViewById<TextView>(R.id.tv_temp)
            val tv_sunrise_time = findViewById<TextView>(R.id.tv_sunrise_time)
            val tv_sunset_time = findViewById<TextView>(R.id.tv_sunset_time)
            val tv_humidity = findViewById<TextView>(R.id.tv_humidity)
            val tv_min = findViewById<TextView>(R.id.tv_min)
            val tv_max = findViewById<TextView>(R.id.tv_max)
            val tv_speed = findViewById<TextView>(R.id.tv_speed)
            val tv_speed_unit = findViewById<TextView>(R.id.tv_speed_unit)
            val tv_name = findViewById<TextView>(R.id.tv_name)
            val tv_country = findViewById<TextView>(R.id.tv_country)

            tv_main.text = weatherList.weather[i].main
            tv_main_description.text = weatherList.weather[i].description
            tv_temp.text = "Temperatura: " + weatherList.main.temp.toString() + getUnit()
            tv_sunrise_time.text = unixTime(weatherList.sys.sunrise.toLong())
            tv_sunset_time.text = unixTime(weatherList.sys.sunset.toLong())
            tv_humidity.text = weatherList.main.humidity.toString() + "%"
            tv_min.text = weatherList.main.temp_min.toString() + " min"
            tv_max.text = weatherList.main.temp_max.toString() + " max"
            tv_speed.text = "Velocidade do Vento:"
            tv_speed_unit.text = weatherList.wind.speed.toString() + " Km/H"
            tv_name.text = weatherList.name
            tv_country.text = weatherList.sys.country

            val iv_min_max: ImageView = findViewById(R.id.iv_min_max)
            iv_min_max.setImageResource(R.drawable.temperature)

            val iv_humidity: ImageView = findViewById(R.id.iv_humidity)
            iv_humidity.setImageResource(R.drawable.humidity)

            val iv_wind: ImageView = findViewById(R.id.iv_wind)
            iv_wind.setImageResource(R.drawable.wind)

            val iv_location: ImageView = findViewById(R.id.iv_location)
            iv_location.setImageResource(R.drawable.location)

            val iv_sunrise: ImageView = findViewById(R.id.iv_sunrise)
            iv_sunrise.setImageResource(R.drawable.sunrise)

            val iv_sunset: ImageView = findViewById(R.id.iv_sunset)
            iv_sunset.setImageResource(R.drawable.sunset)

            val iv_main: ImageView = findViewById(R.id.iv_main)
            when(weatherList.weather[i].icon){
                "01d" -> iv_main.setImageResource(R.drawable.sunny)
                "02d" -> iv_main.setImageResource(R.drawable.cloud)
                "03d" -> iv_main.setImageResource(R.drawable.cloud)
                "04d" -> iv_main.setImageResource(R.drawable.cloud)
                "04n" -> iv_main.setImageResource(R.drawable.cloud)
                "10d" -> iv_main.setImageResource(R.drawable.rain)
                "11d" -> iv_main.setImageResource(R.drawable.storm)
                "13d" -> iv_main.setImageResource(R.drawable.snowflake)
                "01n" -> iv_main.setImageResource(R.drawable.cloud)
                "02n" -> iv_main.setImageResource(R.drawable.cloud)
                "03n" -> iv_main.setImageResource(R.drawable.cloud)
                "10n" -> iv_main.setImageResource(R.drawable.cloud)
                "11n" -> iv_main.setImageResource(R.drawable.rain)
                "13n" -> iv_main.setImageResource(R.drawable.snowflake)
            }
        }
    }

    private fun getUnit(): String {
        return "°C"
    }

    private fun unixTime(timex: Long): String {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }
}
