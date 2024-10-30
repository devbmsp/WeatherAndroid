// MainActivity.kt
package clima.tempo.weather.View

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import clima.tempo.weather.Models.WeatherResponse
import clima.tempo.weather.R
import clima.tempo.weather.ViewModel.MainViewModel
import clima.tempo.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var mProgressDialog: Dialog? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MainViewModel::class.java)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        viewModel.loadWeatherDataFromPrefs()

        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Sua localização está desligada, favor ligue para prosseguir.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        requestLocationData()
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        showRationalDialogForPermissions()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationalDialogForPermissions()
                }
            }).check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Parece que você desativou as permissões necessárias. Elas podem ser ativadas nas configurações.")
            .setPositiveButton(
                "Vá para as Configurações"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            android.os.Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude

            Log.i("Current Latitude", "$mLatitude")
            Log.i("Current Longitude", "$mLongitude")

            viewModel.fetchWeatherData(mLatitude, mLongitude)
        }
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
                // Atualiza os dados com as coordenadas atuais
                viewModel.fetchWeatherData(mLatitude, mLongitude)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI(weatherList: WeatherResponse) {
        binding.apply {
            val weather = weatherList.weather[0]
            tvMain.text = weather.main
            tvMainDescription.text = weather.description
            tvTemp.text = "Temperatura: ${weatherList.main.temp}°C"
            tvSunriseTime.text = unixTime(weatherList.sys.sunrise.toLong())
            tvSunsetTime.text = unixTime(weatherList.sys.sunset.toLong())
            tvHumidity.text = "${weatherList.main.humidity}%"
            tvMin.text = "${weatherList.main.temp_min} min"
            tvMax.text = "${weatherList.main.temp_max} max"
            tvSpeed.text = "Velocidade do Vento:"
            tvSpeedUnit.text = "${weatherList.wind.speed} Km/H"
            tvName.text = weatherList.name
            tvCountry.text = weatherList.sys.country

            ivMinMax.setImageResource(R.drawable.temperature)
            ivHumidity.setImageResource(R.drawable.humidity)
            ivWind.setImageResource(R.drawable.wind)
            ivLocation.setImageResource(R.drawable.location)
            ivSunrise.setImageResource(R.drawable.sunrise)
            ivSunset.setImageResource(R.drawable.sunset)

            when(weather.icon){
                "01d" -> ivMain.setImageResource(R.drawable.sunny)
                "02d", "03d", "04d", "04n" -> ivMain.setImageResource(R.drawable.cloud)
                "10d" -> ivMain.setImageResource(R.drawable.rain)
                "11d" -> ivMain.setImageResource(R.drawable.storm)
                "13d" -> ivMain.setImageResource(R.drawable.snowflake)
                "01n" -> ivMain.setImageResource(R.drawable.cloud)
                "02n", "03n" -> ivMain.setImageResource(R.drawable.cloud)
                "10n" -> ivMain.setImageResource(R.drawable.cloud)
                "11n" -> ivMain.setImageResource(R.drawable.rain)
                "13n" -> ivMain.setImageResource(R.drawable.snowflake)
            }
        }
    }

    private fun unixTime(timex: Long): String {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    private fun showCustomProgressDialog(){
        if (mProgressDialog == null) {
            mProgressDialog = Dialog(this)
            mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog!!.setCancelable(false)
        }
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
}
