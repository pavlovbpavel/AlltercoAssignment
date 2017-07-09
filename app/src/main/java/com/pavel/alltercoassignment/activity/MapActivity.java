package com.pavel.alltercoassignment.activity;

import android.Manifest;
import android.Manifest.permission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pavel.alltercoassignment.R;
import com.pavel.alltercoassignment.data_base.DBManager;
import com.pavel.alltercoassignment.data_base.DBManager.LocationsLoadedCallback;
import com.pavel.alltercoassignment.model.LocationsManager;
import com.pavel.alltercoassignment.model.MarkerLocation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static com.pavel.alltercoassignment.Constants.KEY_LOCATION_ID;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    protected LatLng currentLocationLatLon;
    protected SupportMapFragment mapFragment;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        buildGoogleApiClient();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationsManager.getInstance().clearLocations();
        mapFragment.getMapAsync(this);
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                DBManager.getInstance(getApplicationContext()).loadLocations(new LocationsLoadedCallback() {
                    @Override
                    public void onDatabaseLocationsLoaded() {
                        if (googleMap != null) {
                            for (MarkerLocation markerLocation : LocationsManager.getInstance().getLocations().values()) {
                                googleMap.addMarker(new MarkerOptions().
                                        position(new LatLng(markerLocation.getLat(), markerLocation.getLon())).
                                        title(markerLocation.getAddress()).
                                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))).
                                        setTag(markerLocation.getId());
                            }
                        }
                    }
                });
            }
        });

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerLocation markerLocation = getLocationInfo(latLng.latitude, latLng.longitude);
                markerLocation = DBManager.getInstance(MapActivity.this).addLocation(markerLocation);
                if (markerLocation != null) {
                    LocationsManager.getInstance().addLocation(markerLocation.getId(), markerLocation);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    googleMap.addMarker(markerOptions).setTag(markerLocation.getId());
                }
            }
        });

        googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() == null) return false;
                Intent intent = new Intent(MapActivity.this, LocationDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong(KEY_LOCATION_ID, (Long) marker.getTag());
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
    }

    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        } else {
            trackLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                trackLocation();
                if (map != null) {
                    onMapReady(map);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (SettingNotFoundException e) {
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void buildLocationAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location services are off");
        builder.setMessage("Please turn on location services to continue");
        builder.setCancelable(false);
        builder.setPositiveButton("Turn on location", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void trackLocation() {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            if (isLocationEnabled(this)) {
                currentLocationLatLon = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            } else {
                buildLocationAlertDialog();
            }
        } else {
            buildLocationAlertDialog();
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocationLatLon = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private MarkerLocation getLocationInfo(double latitude, double longitude) {
        MarkerLocation markerLocation = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                markerLocation = new MarkerLocation(
                        (returnedAddress.getLocality() + ", " + returnedAddress.getThoroughfare()),
                        returnedAddress.getCountryName(),
                        returnedAddress.getLongitude(),
                        returnedAddress.getLatitude()
                );
            }
        } catch (IOException ioe) {
            markerLocation = new MarkerLocation(longitude, latitude);
        } finally {
            return markerLocation;
        }
    }
}
