package com.example.getloc;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;

    Location current_location;

    TextView loc, dis, sp;

    private final static int REQUEST_CHECK_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loc = findViewById(R.id.loc);
        dis = findViewById(R.id.distance);
        sp = findViewById(R.id.speed);

        requestPermissions();
        requestPermissions();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

    }

    /**
     *Requesting the User permission for access fine location
     */
    public void requestPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                //To check it's granted or not OPTIONAL
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

    }

    /**
     *Creating a Location Request Simultaniously
     */

    protected void createLocationRequest() {

        /**
         * Creating a Location request to change location default settings to set the priority. This will enable to access location multiple times
         */

        LocationRequest locationRequest = new LocationRequest.Builder(5000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000) //requesting the last location every 5 seconds
                .build();

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this); // changing the settings to according to the location request

        settingsClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<LocationSettingsResponse> task) {
                if (task.isSuccessful()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions();
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

                }else{
                    if (task.getException() instanceof ResolvableApiException){
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) task.getException();
                            resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.e("demo", String.valueOf(locationResult));

            Location location = locationResult.getLastLocation();
            if (location != null && current_location != null){
                loc.setText(location.getLatitude() + "," + location.getLongitude());

                /**
                 * Following code to calculate the distance and the speed
                 */
                float distance = location.distanceTo(current_location);
                Float distance_in_km = distance / 1000;
                Double speed = distance_in_km / 0.00138889;
                Log.e("distance", String.valueOf(speed));
                dis.setText(String.valueOf(distance_in_km));
                sp.setText(String.valueOf(speed));

            }
            current_location = location;

        }
    };


    /**
     * Get Last Location only One time
     */

    public void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }


        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
//                    current_location = new LatLng(location.getLatitude(), location.getLongitude());
                    current_location = location;
                    Log.e("Location: ", current_location.toString());
                    Toast.makeText(MainActivity.this, current_location.getLatitude()+","+current_location.getLongitude(), Toast.LENGTH_LONG).show();
                }else {
                    Log.e("Error: ", "Didn't get the current location");
                }
            }
        });
    }


}

