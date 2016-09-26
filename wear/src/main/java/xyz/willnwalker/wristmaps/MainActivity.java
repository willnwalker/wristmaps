package xyz.willnwalker.wristmaps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

public class MainActivity extends WearableActivity implements LocationListener{

    //Constants
    private String TAG = "xyz.willnwalker.wristmaps.wear";
    private Boolean locationEnabled = false;

    MapView mapView;
    LocationManager locationManager;
    Vibrator vibrator;
    ImageButton imageButton;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        imageButton = (ImageButton) findViewById(R.id.compass);
        imageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrator.vibrate(100);
                Toast.makeText(getApplicationContext(), "Up is North!", Toast.LENGTH_SHORT).show();
                mapView.setMapOrientation(0);
                return false;
            }
        });
        imageView = (ImageView) findViewById(R.id.myLocation);
        mapView = (MapView) findViewById(R.id.map);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(0);

        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(getApplicationContext(), mapView);
        rotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationGestureOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(getApplicationContext(), mapView);
        compassOverlay.enableCompass();
        compassOverlay.setEnabled(true);
        mapView.getOverlays().add(compassOverlay);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        if(locationEnabled){
            try{
                locationManager.removeUpdates(this);
            }
            catch(SecurityException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        if(locationEnabled){
            try{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            catch(SecurityException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        try{
            locationManager.removeUpdates(this);
        }
        catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location l){
        Log.i(TAG, "FINALLY GOT CALLED");
        Log.i(TAG, "LOCATION UPDATE: "+l.toString());
        Log.i(TAG, "Location found. Lat: "+l.getLatitude()+", Long: "+l.getLongitude()+".");
        //Insert draw pin code here
        mapView.setMapOrientation(0);
        mapView.getController().zoomTo(18);
        mapView.getController().animateTo(new GeoPoint(l));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, ""+provider+status+extras.toString());
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider);
    }

    public void handleClick(View v) {
        vibrator.vibrate(100);
        locationEnabled = !locationEnabled;
        if (locationEnabled) {
            Log.i(TAG, "Location enabled!");
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_searching));
            Toast.makeText(getApplicationContext(), "Location Enabled!", Toast.LENGTH_SHORT).show();
            try{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            catch(SecurityException e){
                e.printStackTrace();
            }
            Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (l != null){
                imageView.setVisibility(View.INVISIBLE);
                imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location));
                Toast.makeText(getApplicationContext(), "Location Locked!", Toast.LENGTH_SHORT).show();
                mapView.setMapOrientation(0);
                mapView.getController().zoomTo(18);
                mapView.getController().animateTo(new GeoPoint(l));
                imageView.setVisibility(View.VISIBLE);
                //Insert draw pin code here

            }
            else{
                Log.i(TAG, "Got a null response when querying for last known location.");
                imageView.setVisibility(View.INVISIBLE);
            }
        } else {
            imageView.setVisibility(View.INVISIBLE);
            Log.i(TAG, "Location disabled!");
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_disabled));
            Toast.makeText(getApplicationContext(), "Location Disabled!", Toast.LENGTH_SHORT).show();
            locationManager.removeUpdates(this);
        }
    }
}
