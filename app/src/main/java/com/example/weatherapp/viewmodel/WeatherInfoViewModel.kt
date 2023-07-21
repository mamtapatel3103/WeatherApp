package com.example.weatherapp.viewmodel

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.Constants.api_key
import com.example.weatherapp.Constants.baseUrl_Weather
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData
import com.google.gson.Gson

class WeatherInfoViewModel : ViewModel(), Observable {

    lateinit var contextParent: Activity
    val weatherInfoLiveData = MutableLiveData<WeatherData>()
    var currentWeatherData: WeatherData? = null
    val progress = ObservableBoolean(false)

    private var notificationManager: NotificationManager? = null
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"


    private val propertyChangeRegistry = PropertyChangeRegistry()
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        propertyChangeRegistry.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        propertyChangeRegistry.remove(callback)
    }

    fun init(context: Activity, currentLocation: Location) {
        contextParent = context
        val currentweatherAPI: String =
            baseUrl_Weather + api_key + "&lat=" + currentLocation.latitude + "&lon=" + currentLocation.longitude
        getcurrentWeatherInfo(currentweatherAPI)
    }

    fun getCityWeatherInfo(context: Activity, cityName: String) {
        contextParent = context
        val cityweatherAPI =
            "$baseUrl_Weather$api_key&q=$cityName"
        getcurrentWeatherInfo(cityweatherAPI)

    }

    fun getcurrentWeatherInfo(weatherAPI: String) {
        Log.d("ASDFGH", " $weatherAPI")
        val objectRequest = object : JsonObjectRequest(
            Method.GET,
            weatherAPI,
            null,
            Response.Listener { response ->
                currentWeatherData = Gson().fromJson(response.toString(), WeatherData::class.java)
                Log.d("ASDFGH", currentWeatherData.toString())
                weatherInfoLiveData.postValue(currentWeatherData)
                progress.set(false)
            },
            Response.ErrorListener { volleyError ->
                Log.d("ASDFGH", volleyError.toString())

                progress.set(false)

            }) {}
        val queue = Volley.newRequestQueue(contextParent)
        queue.add(objectRequest)
    }

    fun sendNotification(weatherCondition: String) {
        notificationManager =
            contextParent.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
        val intent = Intent(contextParent, MainActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(contextParent, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, "description", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager?.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(contextParent, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(weatherCondition) //set title of notification
                .setChannelId(channelId)
                .setContentIntent(pendingIntent)


        } else {

            builder = Notification.Builder(contextParent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(weatherCondition) //set title of notification
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        }
        notificationManager?.notify(1234, builder.build())
    }

}