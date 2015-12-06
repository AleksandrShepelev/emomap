package innopolis.aleksandr.emomap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, EmojiChooserFragment.NoticeDialogListener {

    private static final String TAG = "EMOJI";
    public static final String EMO_MARKER = "EmoMarker";
    private GoogleMap mMap;
    private LocationManager locationManager;
    double min = .999999;
    double max = 1.000001;
    private final List<ParseObject> addedMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeMap();
        initializeFab();

    }

    private void initializeFab() {
        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMarkers();
                new EmojiChooserFragment().show(getSupportFragmentManager(), TAG);
            }
        });
    }

    private void initializeMap() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            askToEnableGps();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void askToEnableGps() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("GPS is not enabled");
        dialog.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                //Do nothing
            }
        });
        dialog.show();
    }

    private Location findCurrentLocation() {
        Location currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (currentLoc == null) {
            currentLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return currentLoc;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        }
        loadMarkers();
    }

    private void loadMarkers() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EMO_MARKER);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> markers, ParseException e) {
                if (e == null) {
                    for (ParseObject marker : markers) {
                        if (!addedMarkers.contains(marker)) {
                            addedMarkers.add(marker);
                            putMarkersOnMap(marker);
                        }
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Can't load markers! Please try later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void putMarkersOnMap(ParseObject marker) {
        double lat = (double) marker.get("lat");
        double longt = (double) marker.get("long");
        String user = (String) marker.get("user");
        String comment = (String) marker.get("comment");
        int mood = (int) marker.get("mood");
        Date date = (Date) marker.get("date");
        putMarkerOnMap(lat, longt, comment, mood, date);
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String userComment, int mood) {
        LatLng currentLoc = getCurrentLocation();
        if (currentLoc != null) {
            Date date = new Date();
            saveLocallyAndRemotely(currentLoc, userComment, mood, date);
            putOnMapAndMoveTo(currentLoc, userComment, mood, date);
        }
    }

    private void putOnMapAndMoveTo(LatLng currentLoc, String userComment, int mood, Date date) {
        double newLat = currentLoc.latitude * (Math.random() * (max - min) + min);
        double newLng = currentLoc.longitude * (Math.random() * (max - min) + min);
        putMarkerOnMap(newLat, newLng, userComment, mood, date);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
    }

    private void putMarkerOnMap(double newLat, double newLng, String userComment, int mood, Date date) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(newLat, newLng)).title(userComment).snippet(userComment + "\n" + date.toString()).icon(getIcon(mood)));
    }

    private BitmapDescriptor getIcon(int mood) {
        BitmapDescriptor icon = null;
        switch (mood) {
            case Constants.SAD:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.sad_icon);
                break;
            case Constants.INDIFFERENT:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.indifferent_icon);
                break;
            case Constants.HAPPY:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.happy_icon);
                break;
        }
        return icon;
    }

    private void saveLocallyAndRemotely(LatLng currentLoc, String userComment, int mood, Date date) {
        ParseObject gameScore = new ParseObject(EMO_MARKER);
        gameScore.put("lat", currentLoc.latitude);
        gameScore.put("long", currentLoc.longitude);
        gameScore.put("user", userComment);
        gameScore.put("comment", userComment);
        gameScore.put("mood", mood);
        gameScore.put("date", date);
        gameScore.saveEventually();
    }

    @NonNull
    private LatLng getCurrentLocation() {
        Location currentLocation = findCurrentLocation();
        if (currentLocation != null) {
            double myLat = currentLocation.getLatitude();
            double myLng = currentLocation.getLongitude();
            return new LatLng(myLat, myLng);
        }
        return null;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Do nothing
    }

}
