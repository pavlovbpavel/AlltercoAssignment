package com.pavel.alltercoassignment.data_base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.pavel.alltercoassignment.model.LocationsManager;
import com.pavel.alltercoassignment.model.MarkerLocation;

/**
 * Created by Pavel Pavlov on 5/18/2017.
 */

public class DBManager extends SQLiteOpenHelper {

    private static DBManager ourInstance;
    protected static Context context;

    private static final String DATABASE_NAME = "AllterkoAssignment.db";

    private static final String TABLE_LOCATIONS = "Locations";
    private static final String COL_LOCATION_ID = "LOCATION_ID";
    private static final String COL_ADDRESS = "ADDRESS";
    private static final String COL_COUNTRY = "COUNTRY";
    private static final String COL_LAT = "LAT";
    private static final String COL_LONG = "LONG";

    private static final String SQL_CREATE_LOCATIONS = "CREATE TABLE IF NOT EXISTS Locations (\n" +
            " LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            " ADDRESS text NOT NULL,\n" +
            " COUNTRY text NOT NULL, \n" +
            " LAT REAL NOT NULL,\n" +
            " LONG REAL NOT NULL\n" +
            ");";

    private DBManager(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static DBManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DBManager(context);
            DBManager.context = context;
        }
        return ourInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOCATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Nullable
    public MarkerLocation addLocation(MarkerLocation markerLocation) {
        if (markerLocation == null) return null;

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ADDRESS, markerLocation.getAddress());
        contentValues.put(COL_COUNTRY, markerLocation.getCountry());
        contentValues.put(COL_LAT, markerLocation.getLat());
        contentValues.put(COL_LONG, markerLocation.getLon());

        long id = getWritableDatabase().insert(TABLE_LOCATIONS, null, contentValues);
        markerLocation.setId((id));

        return markerLocation;
    }


    public void loadLocations(final LocationsLoadedCallback callback) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                LocationsManager.getInstance().getLocations().clear();
            }

            @Override
            protected Void doInBackground(Void... params) {

                Cursor cursor = ourInstance.getWritableDatabase().rawQuery("SELECT * FROM Locations;", null);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(COL_LOCATION_ID));
                    String address = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                    String country = cursor.getString(cursor.getColumnIndex(COL_COUNTRY));
                    double lat = cursor.getDouble(cursor.getColumnIndex(COL_LAT));
                    double lon = cursor.getDouble(cursor.getColumnIndex(COL_LONG));

                    MarkerLocation markerLocation = new MarkerLocation(id, address, country, lon, lat);
                    LocationsManager.getInstance().addLocation(id, markerLocation);
                }
                cursor.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (callback != null) {
                    callback.onDatabaseLocationsLoaded();
                }
            }
        }.execute();
    }

    public void updateLocation(MarkerLocation markerLocation) {

        String sql = "UPDATE Locations SET ADDRESS = '" + markerLocation.getAddress() + "'," +
                " COUNTRY = '" + markerLocation.getCountry() + "'," +
                " LAT = '" + markerLocation.getLat() + "', " +
                " LONG = '" + markerLocation.getLon() + "' WHERE LOCATION_ID = '" + markerLocation.getId() + "';";

        ourInstance.getWritableDatabase().execSQL(sql);
    }

    public interface LocationsLoadedCallback {
        void onDatabaseLocationsLoaded();
    }
}