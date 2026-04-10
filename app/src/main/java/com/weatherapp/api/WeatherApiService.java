package com.weatherapp.api;

import com.weatherapp.model.ForecastResponse;
import com.weatherapp.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String language,
            @Query("appid") String apiKey2 
    );

    @GET("forecast")
    Call<ForecastResponse> getForecast(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("https://api.openweathermap.org/geo/1.0/direct")
    Call<java.util.List<com.weatherapp.model.LocationResult>> getLocations(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}
