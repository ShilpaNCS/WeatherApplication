package com.example.composeWeatherApp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("data/2.5/weather?")
    fun getWeatherDataOfLocation(
        @Query("q") city: String?,
        @Query("APPID") app_id: String?,
        @Query("units") metric: String?
    ): Call<WeatherResponse?>?
}