package com.weatherapp.state

sealed class WeatherState {
    data object Sunny : WeatherState()
    data object Cloudy : WeatherState()
    data object Rain : WeatherState()
    data object Snow : WeatherState()
    data object Thunderstorm : WeatherState()
    data object Fog : WeatherState()
    data object Night : WeatherState()
}

data class WeatherUiState(
    val weatherState: WeatherState = WeatherState.Sunny,
    val temperature: String = "--°C",
    val humidity: String = "--%",
    val wind: String = "-- km/h",
    val cityName: String = "Loading...",
    val description: String = "Loading...",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchSuggestions: List<com.weatherapp.model.LocationResult> = emptyList()
)
