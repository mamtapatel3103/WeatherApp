package com.example.weatherapp

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.weatherapp.viewmodel.WeatherInfoViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock


class WeatherInfoViewModelTest {
    @get:Rule
    var rule = InstantTaskExecutorRule()

    var listViewModel =WeatherInfoViewModel()
    lateinit var instrumentationContext: Context


    @Test
    fun getcurrentWeatherInfoSuccess(){
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        listViewModel.contextParent = instrumentationContext as Activity

        val latitude = "22"
        val longitude = "72"
        val currentweatherAPI: String =
            Constants.baseUrl_Weather + Constants.api_key + "&lat=" + latitude + "&lon=" + longitude

        listViewModel.getcurrentWeatherInfo(currentweatherAPI)
        Assert.assertEquals(1, listViewModel.currentWeatherData?.weather?.size)
    }
    @Test
    fun getcurrentWeatherInfoFail(){
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        listViewModel.contextParent = instrumentationContext as Activity
        val latitude = "22"
        val longitude = "72"
        val currentweatherAPI: String =
            Constants.baseUrl_Weather + Constants.api_key + "&lat=" + latitude + "&lon=" + longitude

        listViewModel.getcurrentWeatherInfo(currentweatherAPI)
        Assert.assertEquals(null, listViewModel.currentWeatherData?.weather?.size)
    }

}