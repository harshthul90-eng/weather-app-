package com.weatherapp.model;

import com.google.gson.annotations.SerializedName;

public class LocationResult {
    @SerializedName("name")
    private String name;

    @SerializedName("state")
    private String state;

    @SerializedName("country")
    private String country;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    public String getName() { return name; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
}
