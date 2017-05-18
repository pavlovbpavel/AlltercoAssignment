package com.pavel.alltercoassignment.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

class LocationManager {

    private static LocationManager ourInstance;
    private static Set<Location> locations;

    static LocationManager getInstance() {
        if(ourInstance == null){
            ourInstance = new LocationManager();
            locations = new HashSet<>();
        }
        return ourInstance;
    }

    private LocationManager() {
    }

    public static Set<Location> getLocations() {
        return Collections.unmodifiableSet(locations);
    }
}
