package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.weatherapp.Constants.convertMeterspersecToMilesperhour
import com.example.weatherapp.Constants.metersToMiles
import com.example.weatherapp.Constants.unixTimestampToTimeString
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.viewmodel.WeatherInfoViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    private val LOCATION_REQUEST_CODE = 101
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var viewModel: WeatherInfoViewModel
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this)[WeatherInfoViewModel::class.java]
        binding.viewModel = viewModel
        viewModel.weatherInfoLiveData.observe(this) { weatherData ->
            setWeatherInfo(weatherData)
        }

        binding.searchCity.setOnTouchListener(object : OnTouchListener {
            val DRAWABLE_RIGHT = 2

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                // TODO Auto-generated method stub
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.searchCity.right - binding.searchCity.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                    ) {
                        // your action here
                        viewModel.getCityWeatherInfo(
                            this@MainActivity,
                            binding.searchCity.text.toString()
                        )
                        binding.searchCity.text.clear()

                        return true
                    }
                }
                return false
            }
        })

        binding.searchCity.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                keyEvent == null ||
                keyEvent.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                viewModel.getCityWeatherInfo(this, binding.searchCity.text.toString())
                binding.searchCity.text.clear()
                true
            }
            false
        }
        getCurrentLocation()
    }

    private fun setWeatherInfo(weatherData: WeatherData?) {
        viewModel.progress.set(false)
        viewModel.currentWeatherData = weatherData

        if (weatherData?.main?.temp!! > 20) {
            viewModel.sendNotification("It's boiling!")
        }
        if (weatherData.main?.temp!! < 0) {
            viewModel.sendNotification("It's freezing!")
        }

        binding.apply {
            binding.searchCity.text.clear()
            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm").format(Date())
            layoutWeatherBasicInfo.tvCurrentTime.text = currentDate.toString()
            layoutWeatherBasicInfo.tvCityName.text = weatherData.name
            layoutWeatherBasicInfo.tvTemp.text =
                round(weatherData.main?.temp!!).toInt().toString()
            layoutWeatherBasicInfo.tvModeType.text = weatherData.weather[0].description
            layoutWeatherBasicInfo.tvTempMax.text =
                "H: " + round(weatherData.main?.tempMax!!).toInt()
                    .toString() + applicationContext.getString(R.string.temp_unit)
            layoutWeatherBasicInfo.tvTempLow.text =
                "L: " + round(weatherData.main?.tempMin!!).toInt()
                    .toString() + applicationContext.getString(R.string.temp_unit)
            layoutWeatherOtherInfo.tvSunrise.text =
                unixTimestampToTimeString(weatherData.sys?.sunrise!!, weatherData.timezone)
            layoutWeatherOtherInfo.tvSunset.text =
                unixTimestampToTimeString(weatherData.sys?.sunset!!, weatherData.timezone)
            layoutWeatherOtherInfo.tvHumidity.text = weatherData.main?.humidity.toString() + "%"
            layoutWeatherOtherInfo.tvVisibility.text =
                metersToMiles(weatherData.visibility!!).toString() + " mi"
            layoutWeatherOtherInfo.tvPressure.text = weatherData.main?.pressure.toString() + " hPa"
            layoutWeatherOtherInfo.tvWind.text =
                convertMeterspersecToMilesperhour(weatherData.wind?.speed!!).toString() + " mph"
        }
    }

    private fun getCurrentLocation() {
        viewModel.progress.set(true)
        if (checkpermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                } else {
                    fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
                    fusedLocationProvider.lastLocation.addOnSuccessListener(this) { location ->
                        if (location != null) {
                            currentLocation = location
                            viewModel.init(this, currentLocation)
                        }
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkpermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@MainActivity, "Permission Granted",
                    Toast.LENGTH_LONG
                ).show()
                getCurrentLocation()
            }
        }
    }
}