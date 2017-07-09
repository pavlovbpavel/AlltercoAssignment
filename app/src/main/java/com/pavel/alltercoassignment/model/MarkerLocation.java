package com.pavel.alltercoassignment.model;

import java.io.Serializable;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

public class MarkerLocation implements Serializable {

    private long id;
    private String address;
    private String country;
    private double lon;
    private double lat;

    public MarkerLocation(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public MarkerLocation(String address, String country, double lon, double lat) {
        this(lon, lat);
        this.address = address;
        this.country = country;
    }

    public MarkerLocation(long id, String address, String country, double lon, double lat) {
        this(address, country, lon, lat);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkerLocation markerLocation = (MarkerLocation) o;
        return id == markerLocation.id;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + "\n" +
                "Address: " + getAddress() + "\n" +
                "Country: " + getCountry() + "\n" +
                "Lon: " + getLon() + "\n" +
                "Lat: " + getLat();
    }

    public void setId(long id) {
        this.id = id;
    }
}
