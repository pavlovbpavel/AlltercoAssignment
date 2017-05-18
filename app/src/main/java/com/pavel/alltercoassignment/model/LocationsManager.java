package com.pavel.alltercoassignment.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

public class LocationsManager {

    private static LocationsManager ourInstance;
    private Map<Integer, Location> locations;

    public static LocationsManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new LocationsManager();
        }
        return ourInstance;
    }

    private LocationsManager() {
        locations = new HashMap<>();
    }

    public Map<Integer, Location> getLocations() {
        return locations;
    }
}
