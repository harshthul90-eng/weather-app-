package com.weatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.model.WeatherResponse
import com.weatherapp.repository.WeatherRepository
import com.weatherapp.state.WeatherState
import com.weatherapp.state.WeatherUiState
import com.weatherapp.state.HourlyForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val repository = WeatherRepository.getInstance()
    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchSuggestions = emptyList()) }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(500) // Debounce for 500ms
            repository.getLocationSuggestions(query).enqueue(object : Callback<List<com.weatherapp.model.LocationResult>> {
                override fun onResponse(call: Call<List<com.weatherapp.model.LocationResult>>, response: Response<List<com.weatherapp.model.LocationResult>>) {
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.update { it.copy(searchSuggestions = response.body()!!) }
                    }
                }
                override fun onFailure(call: Call<List<com.weatherapp.model.LocationResult>>, t: Throwable) {
                    _uiState.update { it.copy(searchSuggestions = emptyList()) }
                }
            })
        }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
    }

    fun selectForecastItem(item: HourlyForecast?) {
        _uiState.update { it.copy(selectedForecast = item) }
    }

    fun fetchWeather(city: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, cityName = city, selectedForecast = null) }
        
        // Fetch Current Weather
        repository.getCurrentWeather(city).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val weatherResponse = response.body()!!
                    
                    val temp = Math.round(weatherResponse.main.temperature).toString() + "°C"
                    val humidity = weatherResponse.humidity.toString() + "%"
                    val windSpeed = Math.round(weatherResponse.wind.speed * 3.6).toString() + " km/h" // m/s to km/h
                    val description = weatherResponse.weather?.description?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    } ?: "Unknown"
                    val mainCondition = weatherResponse.weather?.main ?: "Clear"
                    
                    val sys = weatherResponse.sys
                    val isNight = weatherResponse.dt < sys?.sunrise ?: 0 || weatherResponse.dt > sys?.sunset ?: Long.MAX_VALUE
                    
                    val parsedState = mapConditionToState(mainCondition, isNight)
                    
                    _uiState.update {
                        it.copy(
                            weatherState = parsedState,
                            temperature = temp,
                            humidity = humidity,
                            wind = windSpeed,
                            cityName = weatherResponse.name,
                            description = description,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load weather: ${response.code()}",
                            temperature = "--°C"
                        ) 
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${t.localizedMessage}",
                        temperature = "--°C"
                    ) 
                }
            }
        })

        // Fetch Forecast
        repository.getForecast(city).enqueue(object : Callback<com.weatherapp.model.ForecastResponse> {
            override fun onResponse(call: Call<com.weatherapp.model.ForecastResponse>, response: Response<com.weatherapp.model.ForecastResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val forecastList = response.body()!!.list
                    val hourlyForecasts = forecastList.take(8).map { item ->
                        val time = formatTime(item.dtTxt)
                        val temp = Math.round(item.main.temperature).toString() + "°C"
                        val humidity = item.main.humidity.toString() + "%"
                        val wind = Math.round(item.wind.speed * 3.6).toString() + " km/h"
                        val mainCondition = item.weather?.main ?: "Clear"
                        val description = item.weather?.description?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        } ?: "Unknown"
                        
                        // Simple night check for forecast: if hour is late or very early
                        val hour = try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val date = sdf.parse(item.dtTxt)
                            val cal = Calendar.getInstance()
                            if (date != null) cal.time = date
                            cal.get(Calendar.HOUR_OF_DAY)
                        } catch (e: Exception) { 12 }
                        val isNight = hour < 6 || hour > 19

                        com.weatherapp.state.HourlyForecast(
                            time = time,
                            temperature = temp,
                            description = description,
                            mainCondition = mainCondition,
                            humidity = humidity,
                            wind = wind,
                            weatherState = mapConditionToState(mainCondition, isNight)
                        )
                    }
                    _uiState.update { it.copy(hourlyForecast = hourlyForecasts) }
                }
            }
            override fun onFailure(call: Call<com.weatherapp.model.ForecastResponse>, t: Throwable) {
                // Silently fail forecast for now
            }
        })
    }

    private fun formatTime(dtTxt: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
            val date = inputFormat.parse(dtTxt)
            if (date != null) outputFormat.format(date) else dtTxt
        } catch (e: Exception) {
            dtTxt
        }
    }

    private fun mapConditionToState(condition: String, isNight: Boolean): WeatherState {
        return when (condition.lowercase()) {
            "clear" -> if (isNight) WeatherState.Night else WeatherState.Sunny
            "clouds" -> WeatherState.Cloudy
            "rain", "drizzle" -> WeatherState.Rain
            "snow" -> WeatherState.Snow
            "thunderstorm" -> WeatherState.Thunderstorm
            "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> WeatherState.Fog
            else -> if (isNight) WeatherState.Night else WeatherState.Sunny
        }
    }
    
}
