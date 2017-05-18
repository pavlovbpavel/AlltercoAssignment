package com.pavel.alltercoassignment.model;

import java.io.Serializable;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

public class Location implements Serializable{

    private int id;
    private String address;
    private String country;
    private double lon;
    private double lat;

    public Location(int id, String address, String country, double lon, double lat) {
        this.id = id;
        this.address = address;
        this.country = country;
        this.lon = lon;
        this.lat = lat;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getCountry() {
        return country;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return id == location.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + "\n" +
                "Address: " + getAddress() + "\n" +
                "Country: " + getCountry() + "\n" +
                "Lon: " + getLon() + "\n" +
                "Lat: " + getLat();
    }
}
