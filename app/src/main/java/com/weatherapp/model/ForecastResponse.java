package com.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    private List<ForecastItem> list;

    public List<ForecastItem> getList() { return list; }

    public static class ForecastItem {
        @SerializedName("dt")
        private int dt;
        @SerializedName("main")
        private Main main;
        @SerializedName("weather")
        private Weather[] weatherArray;
        @SerializedName("dt_txt")
        private String dtTxt;

        @SerializedName("wind")
        private Wind wind;

        public int getDt() { return dt; }
        public Main getMain() { return main; }
        public Weather getWeather() { return weatherArray != null && weatherArray.length > 0 ? weatherArray[0] : null; }
        public String getDtTxt() { return dtTxt; }
        public Wind getWind() { return wind; }
    }

    public static class Main {
        @SerializedName("temp")
        private double temperature;
        @SerializedName("temp_min")
        private double minTemp;
        @SerializedName("temp_max")
        private double maxTemp;
        @SerializedName("humidity")
        private int humidity;

        public double getTemperature() { return temperature; }
        public double getMinTemp() { return minTemp; }
        public double getMaxTemp() { return maxTemp; }
        public int getHumidity() { return humidity; }
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
