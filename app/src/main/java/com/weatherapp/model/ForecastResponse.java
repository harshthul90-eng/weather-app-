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

        public int getDt() { return dt; }
        public Main getMain() { return main; }
        public Weather getWeather() { return weatherArray != null && weatherArray.length > 0 ? weatherArray[0] : null; }
        public String getDtTxt() { return dtTxt; }
    }

    public static class Main {
        @SerializedName("temp_min")
        private double minTemp;
        @SerializedName("temp_max")
        private double maxTemp;

        public double getMinTemp() { return minTemp; }
        public double getMaxTemp() { return maxTemp; }
    }

    public static class Weather {
        @SerializedName("main")
        private String main;

        public String getMain() { return main; }
    }
}
