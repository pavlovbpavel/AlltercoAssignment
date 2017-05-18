package com.pavel.alltercoassignment.model;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

public class Location {

    private int id;
    private String address;
    private String country;
    private float lon;
    private float lat;

    public Location(int id, String address, String country, float lon, float lat) {
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

    public float getLon() {
        return lon;
    }

    public float getLat() {
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
}
