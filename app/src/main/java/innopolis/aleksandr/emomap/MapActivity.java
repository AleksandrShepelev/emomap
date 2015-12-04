package innopolis.aleksandr.emomap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, EmojiChooserFragment.NoticeDialogListener {

    private static final String TAG = "DBG";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location currentLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if (!enabled) {
//            Log.d(TAG, "onCreate: gps disabled");
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//        }


        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EmojiChooserFragment().show(getSupportFragmentManager(), "myTag");
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void FindCurrentLocation() {
        currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (currentLoc == null) {
            Log.d(TAG, "onMapReady: gps null");
            currentLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "onMapReady: " + currentLoc.toString());
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        FindCurrentLocation();
        if (currentLoc != null) {
            double myLat = currentLoc.getLatitude();
            double myLng = currentLoc.getLongitude();
            // Add a marker in Sydney and move the camera
            LatLng currentLoc = new LatLng(myLat, myLng);
            mMap.addMarker(new MarkerOptions().position(currentLoc).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
            Log.d(TAG, "onMapReady: network not null");
        }
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String userComment) {
        FindCurrentLocation();
        Log.d(TAG, "onClick: click!!");
        double myLat = currentLoc.getLatitude();
        double myLng = currentLoc.getLongitude();
        LatLng currentLoc = new LatLng(myLat, myLng);
        mMap.addMarker(new MarkerOptions().position(currentLoc).title(userComment));
        Location myLocation = mMap.getMyLocation();
        LatLng ml = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(ml).title(userComment));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(MapActivity.this, "No", Toast.LENGTH_SHORT).show();
    }

}
