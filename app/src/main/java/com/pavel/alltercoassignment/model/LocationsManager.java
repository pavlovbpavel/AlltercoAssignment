package com.pavel.alltercoassignment.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

public class LocationsManager {

    private static LocationsManager ourInstance;
    private Map<Long, MarkerLocation> locations;

    public static LocationsManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new LocationsManager();
        }
        return ourInstance;
    }

    private LocationsManager() {
        locations = new HashMap<>();
    }

    public void addLocation(long id, MarkerLocation markerLocation){

        this.locations.put(id, markerLocation);
    }

    public Map<Long, MarkerLocation> getLocations() {
        return locations;
    }

    public void clearLocations(){
        locations.clear();
    }

    public MarkerLocation getLocation(Long id){

        return this.locations.get(id);
    }
}
