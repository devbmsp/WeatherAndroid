package clima.tempo.weather.Models
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import clima.tempo.weather.DataBase.WeatherServices
import com.google.android.gms.location.*
import com.google.gson.Gson
import retrofit.Call
import retrofit.Callback
import retrofit.GsonConverterFactory
import retrofit.Response
import retrofit.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView
import clima.tempo.weather.Network.Constants
import clima.tempo.weather.DataBase.WeatherResponse
import clima.tempo.weather.R
import clima.tempo.weather.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private lateinit var mSharedPreferences : SharedPreferences
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDeslogar.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val  backLogin = Intent(this,LoginActivity::class.java)
            startActivity(backLogin)

        }

    }

    override fun onStart() {
        super.onStart()
        val userAtual= FirebaseAuth.getInstance().currentUser
        if (userAtual != null) {
            MainActivity()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)
        setupUI()

        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Sua localização está desligada, favor ligue para prosseguir.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        else
        {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {

                            requestLocationData()

                        }
                        if (report.isAnyPermissionPermanentlyDenied) {

                            Toast.makeText(

                                this@MainActivity,
                                "Por favor, ligue sua localização para usar o app",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        setupUI()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check() }

    }
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //mFusedLocationClient = LocationServ ices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            val mLastLocation: Location = locationResult.lastLocation
            val mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            val mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")
            getLocationWeatherDetails(mLatitude, mLongitude)

/*
                              //ler o bd :
                              db.collection("Usuários").document("bruno2")
                                  .addSnapshotListener{documento,error ->
                                      print(documento?.getString("nickName"))

            */

        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {

        if (Constants.isNetworkAvailable(this)) {

            val retrofit: Retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
            val service : WeatherServices =
                retrofit.create(WeatherServices::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.APP_ID, Constants.METRIC_UNIT
            )
            showCustomProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse>{
                override fun onResponse(response: Response<WeatherResponse>?, retrofit: Retrofit?) {
                    val requestUrl = response?.raw()?.request()?.urlString() // Obter a URL completa
                    Log.d("URL Used", "URL da requisição: $requestUrl")
                    if(response!!.isSuccess){
                        hideProgressDialog()
                        val weatherList: WeatherResponse = response.body()
                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

                    }else{
                        val rc = response.code()
                        when(rc){
                            400 -> {
                                Log.e("Erro 400", "Bad connection")
                            }
                            404 -> {
                                Log.e("Erro 404", "Not found")
                            } else -> {
                                Log.e("Erro", "Erro genérico")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    hideProgressDialog()
                    Log.e("Erro", t!!.message.toString())
                }
            })

        } else {
            Toast.makeText(
                this@MainActivity,
                "Ops! Sem internet.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Parece que você desativou as permissões. Isso pode ser ajustado nas Configurações")
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
            .setNegativeButton("Cancelar") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {

        // This provides access to the system location services.
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh -> {
                requestLocationData()
                true
            }else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }
    private fun setupUI(){
        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")
        if(!weatherResponseJsonString.isNullOrEmpty()){
            val weatherList = Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)
            for(i in weatherList.weather.indices){
                Log.i("Weather Name", weatherList.weather.toString())
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
                tv_temp.text = "Temperatura: " + weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                tv_sunrise_time.text = unixTime(weatherList.sys.sunrise.toLong())
                tv_sunset_time.text = unixTime(weatherList.sys.sunset.toLong())
                tv_humidity.text = weatherList.main.humidity.toString() + " per cent"
                tv_min.text = weatherList.main.temp_min.toString() + " min"
                tv_max.text = weatherList.main.temp_max.toString() + " max"
                tv_speed.text = "Velocidade do vento:"
                tv_speed_unit.text = weatherList.wind.speed.toString() + "Km/H"
                tv_name.text = weatherList.name
                tv_country.text = weatherList.sys.country
                // Não tá importando os dados da activity_main.xml

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
    }
    private fun getUnit(value: String): String?{
        var value = "°C"
        if("US" == value || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }
    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm")
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}


