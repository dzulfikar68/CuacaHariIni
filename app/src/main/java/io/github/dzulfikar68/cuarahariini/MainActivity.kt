package io.github.dzulfikar68.cuarahariini

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import io.github.dzulfikar68.cuarahariini.Utils.capitalizeWords
import io.github.dzulfikar68.cuarahariini.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherAdapter: WeatherAdapter
    private var selectedCity: City? = null
    private var listCity: ArrayList<City> = arrayListOf()
    private var locationManager : LocationManager? = null
    private var isMyLocation = false
    private var myLocation: Location? = null
    private var isRefreshState: Boolean = false

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar?.setBackgroundDrawable(resources.getDrawable(R.color.purple_600))
        binding.rlInfo.visibility = View.VISIBLE

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        checkingGPS {}

        weatherAdapter = WeatherAdapter()
        with(binding.rvForecast) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = weatherAdapter
        }
        emptyFillAndData()

        initSpinner()
        binding.msCity.setOnItemSelectedListener { view, position, id, item ->
            binding.rlInfo.visibility = View.GONE
            binding.tvForecast.visibility = View.VISIBLE
            binding.llContent.visibility = View.VISIBLE
            val selected = binding.msCity.getItems<City>().get(position)
            if (selected.lat != 0.0 && selected.long != 0.0) {
                selectedCity = selected
                getListWeather()
            } else if (selected.id == 9L) {
                isMyLocation = false
                gettingGPS()
            } else {
                binding.ivInfo.setImageDrawable(getDrawable(R.drawable.ic_up_weather))
                binding.rlInfo.visibility = View.VISIBLE
                isRefreshState = false
                emptyFillAndData()
            }
        }

        binding.btnAdd.setOnClickListener {
            val dialog = SearchDialogFragment.newInstance(object :
                    SearchDialogFragment.SearchCallback {
                override fun onClick(city: City) {
                    var isSame = false
                    listCity.forEach {
                        if (it.name.equals(city.name, ignoreCase = true)) {
                            isSame = true
                            return@forEach
                        }
                    }
                    if (!isSame) {
                        CityPreference.setCities(this@MainActivity, city)
                        Snackbar.make(
                                binding.root,
                                "Kota Berhasil Ditambah (Add Success)",
                                Snackbar.LENGTH_LONG
                        )
                                .show()
                        restartActivity()
                    } else {
                        Toast.makeText(
                                this@MainActivity,
                                "Nama Kota Sudah Ada (Already Exist)",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            val bundle = Bundle()
//            "lat":-6.2146,"long":106.8451
            bundle.putDouble("lat", myLocation?.latitude ?: -6.2146)
            bundle.putDouble("long", myLocation?.longitude ?: 106.8451)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, SearchDialogFragment::class.java.simpleName)
        }

        binding.btnRefresh.setOnClickListener {
            CityPreference.delCities(this@MainActivity)
            Snackbar.make(
                    binding.root,
                    "Semua Data Tersimpan Sudah Dihapus (Cleared)",
                    Snackbar.LENGTH_LONG
            )
                    .show()
            restartActivity()
        }

        binding.swipeRefresh.setOnRefreshListener {
            getListWeather()
        }

        binding.ivInfo.setOnClickListener {
            if (isRefreshState) {
                getListWeather()
            } else {
                binding.msCity.expand()
            }
        }
    }

    private fun restartActivity(){
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                finish()
                startActivity(intent)
            }
        }.start()
    }

    private fun initSpinner() {
        listCity.add(City(0, "--Pilih Kota (Choose City)--", "-", 0.0, 0.0))
        val savedCities = CityPreference.getCities(this@MainActivity).reversed()
        listCity.addAll(savedCities)
        listCity.add(City(9, "Posisi GPS saat ini (Now Location)", "-", 0.0, 0.0))
        val citiesIndonesia = JsonParserLocation(this).getDataCityDataSet().sortedBy { it.name }
        listCity.addAll(citiesIndonesia)
        binding.msCity.setItems(listCity)
    }

    private fun gettingGPS() {
        try {
            locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
            )
        } catch (ex: SecurityException) {
            checkingGPS {
                gettingGPS()
            }
        }
    }

    private fun checkingGPS(callback: () -> Unit) {
        try {
            if (ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        101
                )
            } else {
                callback()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val myCity = listCity.firstOrNull { it.id == 9L }
            myCity?.lat = location.latitude
            myCity?.long = location.longitude
            myLocation = location
            if (!isMyLocation) {
                selectedCity = myCity
                getListWeather()
                isMyLocation = true
            }
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun emptyFillAndData() {
        weatherAdapter.setList(emptyList())
        binding.tvWeather.text = "-"
        binding.tvDate.text = "-"
        binding.tvLocation.text = "-"
        binding.tvHumidity.text = "-"
        binding.tvPressure.text = "-"
        binding.tvPercent.text = "-"
        binding.tvForecast.visibility = View.GONE
        binding.llContent.visibility = View.GONE
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getListWeather() {
        binding.rlInfo.visibility = View.GONE

        val lat = selectedCity?.lat?.toString() ?: "0.0"
        val lon = selectedCity?.long?.toString() ?: "0.0"
        if(lat=="0.0" && lon=="0.0") {
            binding.swipeRefresh.isRefreshing = false
            binding.ivInfo.setImageDrawable(getDrawable(R.drawable.ic_up_weather))
            binding.rlInfo.visibility = View.VISIBLE
            isRefreshState = false
            Toast.makeText(this@MainActivity, "Silahkan pilih cuaca kota yang dituju {Please Choose City)", Toast.LENGTH_SHORT)
                    .show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Sedang Menunggu (Loading)")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(OpenWeatherService::class.java)
        service.listWeather(lat = lat, lon = lon).enqueue(object : Callback<OpenWeatherResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                    call: Call<OpenWeatherResponse>,
                    response: Response<OpenWeatherResponse>
            ) {
                binding.swipeRefresh.isRefreshing = false
                val data = response.body()
                val listWeather = data?.daily ?: emptyList()
                weatherAdapter.setList(listWeather)
                val weatherName =
                        data?.current?.weather?.get(0)?.description?.capitalizeWords() ?: "-"
                binding.tvWeather.text = weatherName

                binding.tvDate.text = Utils.timestampToDate(data?.current?.dt ?: 0L)
                binding.tvLocation.text = data?.timezone?.replace("/", ", ") ?: "-"

                val pressure = data?.current?.pressure?.toString()
                binding.tvPressure.text = "${pressure} hPa"

                val humidity = data?.current?.wind_deg ?: 0
                binding.tvHumidity.text = "${humidity}%"
//                        .div(100)

                val celcius = data?.current?.temp ?: 0.0
                val result = celcius.minus(273.15).roundToInt()
                binding.tvPercent.text = "$result C"

                Glide.with(this@MainActivity)
                        .load(Utils.weatherToImage(data?.current?.weather?.get(0)?.main))
                        .into(binding.ivPicture)

                progressDialog.dismiss()

                if (data == null) {
                    Toast.makeText(this@MainActivity, "Data Tidak Ditemukan", Toast.LENGTH_SHORT)
                            .show()
                } else {
                    Snackbar.make(
                            binding.root,
                            "Cuaca $selectedCity saat ini: $weatherName",
                            Snackbar.LENGTH_LONG
                    )
                            .show()
                }
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onFailure(call: Call<OpenWeatherResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                binding.ivInfo.setImageDrawable(getDrawable(R.drawable.ic_refresh_weather))
                binding.rlInfo.visibility = View.VISIBLE
                isRefreshState = true
                emptyFillAndData()
                progressDialog.dismiss()
                Toast.makeText(
                        this@MainActivity,
                        "Terjadi Kesalahan Jaringan (Network Error)",
                        Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menus, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.getItemId()
        return if (id == R.id.setting) {
            //TODO
            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            true
        } else super.onOptionsItemSelected(item)
    }
}