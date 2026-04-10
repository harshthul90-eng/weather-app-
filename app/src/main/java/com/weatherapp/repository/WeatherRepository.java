package com.weatherapp.repository;

import com.weatherapp.api.WeatherApiService;
import com.weatherapp.model.ForecastResponse;
import com.weatherapp.model.WeatherResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRepository {

    private static final String API_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "8f5cb364ff723780b0d683c35aee4c1f";
    private static WeatherRepository instance;
    private WeatherApiService apiService;

    private WeatherRepository() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(WeatherApiService.class);
    }

    public static WeatherRepository getInstance() {
        if (instance == null) {
            instance = new WeatherRepository();
        }
        return instance;
    }

    public Call<WeatherResponse> getCurrentWeather(String city) {
        return apiService.getCurrentWeather(city, API_KEY, "metric", "en", API_KEY);
    }

    public Call<ForecastResponse> getForecast(String city) {
        return apiService.getForecast(city, API_KEY, "metric");
    }

    public Call<java.util.List<com.weatherapp.model.LocationResult>> getLocationSuggestions(String query) {
        return apiService.getLocations(query, 5, API_KEY);
    }
}
