package com.example.jetpackroom.meteorologia.viewModel



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.jetpackroom.meteorologia.model.forecast.ForecastResult
import com.example.jetpackroom.meteorologia.model.weather.WeatherResult
import androidx.lifecycle.viewModelScope
import com.example.jetpackroom.meteorologia.db.RetrofitClient
import com.example.jetpackroom.meteorologia.model.MyLatLng
import kotlinx.coroutines.launch

enum class STATE{
    LOADING,
    SUCCESS,
    FAILED
}
class MainViewModel: ViewModel() {
    var state by mutableStateOf(STATE.LOADING)
    // Resultado del tiempo actual
    var weatherResponse: WeatherResult by mutableStateOf(WeatherResult())

    // Resultado del pronóstico
    var forecastResponse: ForecastResult by mutableStateOf(ForecastResult())

    var errorMessage: String = mutableStateOf("").toString()

    fun getWeatherByLocation(latLng: MyLatLng){
        viewModelScope.launch {
            state= STATE.LOADING
            val apiService = RetrofitClient.getIntances()
            try{
                val apiResponse = apiService.getWeather(latLng.lat,latLng.lng)
                weatherResponse = apiResponse
                state = STATE.SUCCESS

            }catch (e:Exception){
                errorMessage = e.message!!.toString()
                state = STATE.FAILED
            }

        }
    }

    fun getForecastByLocation(latLng: MyLatLng){
        viewModelScope.launch {
            state= STATE.LOADING
            val apiService = RetrofitClient.getIntances()
            try{
                val apiResponse = apiService.getForecast(latLng.lat,latLng.lng)
                forecastResponse = apiResponse
                state = STATE.SUCCESS

            }catch (e:Exception){
                errorMessage = e.message!!.toString()
                state = STATE.FAILED
            }

        }
    }

}