package com.weatherapp.model;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private Weather[] weatherArray;

    @SerializedName("description")
    private String description;

    @SerializedName("humidity")
    private int humidity;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("dt")
    private int dt;

    @SerializedName("sys")
    private Sys sys;

    public String getName() { return name; }
    public Main getMain() { return main; }
    public Weather getWeather() { return weatherArray != null && weatherArray.length > 0 ? weatherArray[0] : null; }
    public String getDescription() { return description; }
    public int getHumidity() { return humidity; }
    public Wind getWind() { return wind; }
    public int getDt() { return dt; }
    public Sys getSys() { return sys; }

    public static class Sys {
        @SerializedName("sunrise")
        private long sunrise;
        @SerializedName("sunset")
        private long sunset;

        public long getSunrise() { return sunrise; }
        public long getSunset() { return sunset; }
    }

    public static class Main {
        @SerializedName("temp")
        private double temperature;
        @SerializedName("feels_like")
        private double feelsLike;
        @SerializedName("temp_min")
        private double minTemp;
        @SerializedName("temp_max")
        private double maxTemp;

        public double getTemperature() { return temperature; }
        public double getFeelsLike() { return feelsLike; }
        public double getMinTemp() { return minTemp; }
        public double getMaxTemp() { return maxTemp; }
    }

    public static class Weather {
        @SerializedName("main")
        private String main;
        @SerializedName("description")
        private String description;

        public String getMain() { return main; }
        public String getDescription() { return description; }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;

        public double getSpeed() { return speed; }
    }
}
