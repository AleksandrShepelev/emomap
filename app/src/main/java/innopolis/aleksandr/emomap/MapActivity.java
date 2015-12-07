package innopolis.aleksandr.emomap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, EmojiChooserFragment.NoticeDialogListener {

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

        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();

        myTimer.schedule(myTask, 1000 * 60, 1000 * 60);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            // Call the Parse log out method
            ParseUser.logOut();
            // Start and intent for the dispatch activity
            Intent intent = new Intent(MapActivity.this, DispatchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
       //     mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f));

            //     mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(newLat, newLng)).title(ParseUser.getCurrentUser().getUsername()).snippet(userComment + "\n" + date.toString()).icon(getIcon(mood)));
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
        gameScore.put("user", ParseUser.getCurrentUser().getUsername());
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


    class MyTimerTask extends TimerTask {
        public void run() {

            generateNotification(getApplicationContext(), "I need your mood");
        }
    }

    private void generateNotification(Context context, String message) {

        int icon = R.drawable.happy_icon;
        long when = System.currentTimeMillis();
        String appname = context.getResources().getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        Notification notification;
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MapActivity.class), 0);

        // To support 2.3 os, we use "Notification" class and 3.0+ os will use
        // "NotificationCompat.Builder" class.
        if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
            notification = new Notification(icon, message, 0);
            notification.setLatestEventInfo(context, appname, message,
                    contentIntent);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify((int) when, notification);

        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context);
            notification = builder.setContentIntent(contentIntent)
                    .setSmallIcon(icon).setTicker(appname).setWhen(0)
                    .setAutoCancel(true).setContentTitle(appname)
                    .setContentText(message).build();

            notificationManager.notify((int) when, notification);

        }
    }

}
