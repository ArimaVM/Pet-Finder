package com.example.petfinder.components;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.petfinder.R;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.pages.pet.DisplayPetDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class Location extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "Location";
    private static final int FINE_PERMISSION_CODE = 1;
    private static final int ERROR_DIALOG_REQUEST = 2;

    private GoogleMap myMap;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList;
    private PendingIntent geofencePendingIntent;

    private float geofenceRadius = 50;
    private String geofenceId = "SOME_FENCE_ID";

    private boolean isConnected = false;
    private android.location.Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar myToolbar = findViewById(R.id.pet_toolbar);
        setSupportActionBar(myToolbar);

        // Set the custom back arrow as the navigation icon
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Set a click listener on the navigation icon
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        checkPlayServices();

        getLastLocation();

        isConnected = getIntent().getBooleanExtra("isConnected", false);

        ImageView emptyImageView = findViewById(R.id.empty);
        TextView disconnectedTextView = findViewById(R.id.disconnected);

        if (isConnected) {
            emptyImageView.setVisibility(View.GONE);
            disconnectedTextView.setVisibility(View.GONE);
        } else {
            emptyImageView.setVisibility(View.VISIBLE);
            disconnectedTextView.setVisibility(View.VISIBLE);
        }

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.nav_petProfile:
                        intent = new Intent(Location.this, DisplayPetDetails.class);
                        intent.putExtra("isConnected", isConnected);
                        startActivity(intent);
                        break;
                    case R.id.nav_location:
                        break;
                }
                return true;
            }
        });
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Dialog errorDialog = apiAvailability.getErrorDialog(this, resultCode, ERROR_DIALOG_REQUEST);
                errorDialog.setCancelable(false);
                errorDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                errorDialog.show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geofence:
                showCreateGeofenceDialog();
                return true;
            case R.id.clear:
                Toast.makeText(this, "Geofence Cleared", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<android.location.Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(Location.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        MarkerOptions options = new MarkerOptions().position(location).title("Current Location");
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        myMap.addMarker(options);

        getLastLocation();

        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);

        // Add existing geofence if available
        if (geofenceList != null && geofenceList.size() > 0) {
            myMap.clear();
            Geofence geofence = geofenceList.get(0);
//            double latitude = Double.parseDouble(geofence.getRequestId()); // Replace with latitude value
//            double longitude = geofence.getTransitionTypes(); // Replace with longitude value
            float radius = geofence.getRadius(); // Replace with radius value
//            LatLng geofenceLatLng = new LatLng(latitude, longitude);
            addGeofenceMarker(location);
            addGeofenceCircle(location, radius);
        }

        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        }

        // Add Geofence on long click
        myMap.setOnMapLongClickListener(this);
    }

    private void addGeofenceMarker(LatLng location) {
        LatLng latLng = new LatLng(location.latitude, location.longitude);
        MarkerOptions options = new MarkerOptions().position(latLng).title("Geofence");
        myMap.addMarker(options);
    }

    private void addGeofenceCircle(LatLng location, float perimeter) {
        LatLng latLng = new LatLng(location.latitude, location.longitude);
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(perimeter)
                .fillColor(Color.argb(70, 150, 50, 50))
                .strokeColor(Color.argb(100, 200, 100, 100))
                .strokeWidth(2);
        myMap.addCircle(circleOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permission Denied, Please Allow Access!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle long click on the map to create a geofence
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        showCreateGeofenceDialog();
    }

    private void showCreateGeofenceDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_geofence, null);
        dialogBuilder.setView(dialogView);

        EditText perimeterEditText = dialogView.findViewById(R.id.perimeterEditText);
        Button setButton = dialogView.findViewById(R.id.setButton);
        ImageView closeButton = dialogView.findViewById(R.id.closeButton);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String perimeterStr = perimeterEditText.getText().toString().trim();
                if (!perimeterStr.isEmpty()) {
                    float perimeter = Float.parseFloat(perimeterStr);
                    setGeofence(perimeter);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(Location.this, "Please enter a valid perimeter.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void setGeofence(float perimeter) {
        myMap.clear();
        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        addGeofenceMarker(location);
        addGeofenceCircle(location, perimeter);

        geofenceRadius = perimeter;

        geofenceList = new ArrayList<>();
        geofenceList.add(new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        perimeter
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());



        GeofencingRequest geofencingRequest = getGeofencingRequest(geofenceList);
        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Location.this, "Geofence created successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Location.this, "Failed to create geofence.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        return new GeofencingRequest.Builder()
                .addGeofences(geofenceList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        return geofencePendingIntent;
    }

    @Override
    public void onBackPressed() {
        PetFinder petFinder = PetFinder.getInstance();
        if (!petFinder.getBluetoothObject().isNull()) {
            petFinder.getBluetoothObject().getBluetoothGatt().disconnect();
            petFinder.deleteBluetoothObject();
        }
        startActivity(new Intent(Location.this, Dashboard.class));
    }
}
