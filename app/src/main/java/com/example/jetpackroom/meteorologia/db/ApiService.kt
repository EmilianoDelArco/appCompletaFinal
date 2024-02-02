package com.example.jetpackroom.meteorologia.db



import com.example.jetpackroom.meteorologia.constant.Const.Companion.apikey
import com.example.jetpackroom.meteorologia.model.forecast.ForecastResult
import com.example.jetpackroom.meteorologia.model.weather.WeatherResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {


    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat:Double=0.0,
        @Query("lon") lng: Double=0.0,
        @Query("units") units:String = "metric",
        @Query("appid") appId:String = apikey
    ): WeatherResult

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat:Double=0.0,
        @Query("lon") lng: Double=0.0,
        @Query("units") units:String = "metric",
        @Query("appid") appId:String = apikey
    ): ForecastResult
}
