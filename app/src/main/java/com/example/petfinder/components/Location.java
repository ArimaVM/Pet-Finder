package com.example.petfinder.components;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.container.dataModel;
import com.example.petfinder.pages.pet.DisplayPetDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import yuku.ambilwarna.AmbilWarnaDialog;

public class Location extends AppCompatActivity implements PetFinder.GPS.GPSChangeCallback,
                                            BluetoothGattCallbackHandler.DescriptorWriteCallback,
                                            BluetoothGattCallbackHandler.ConnectionStateChangeCallback{

    private PetFinder petFinder;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private PetFinder.RepeatSend repeatSend;
    private PetFinder.GPS gpsObject;
    private Boolean isBluetoothConnected;
    private DatabaseHelper databaseHelper;
    BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        petFinder.deleteBluetoothObject();
                        setWarning("Bluetooth turned off. No data is being received by the" +
                                        " system.",
                                 "Please turn on your bluetooth device.");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (petFinder.getBluetoothObject().isNull()){
                            setWarning("Collar not found.",
                                    "Please make sure that the collar is turned on. Click here to" +
                                            " connect to device.");
                            dataUnavailableResolution.setOnClickListener(view -> connectToBluetooth());
                        } else removeWarning();
                        break;
                }
            }
        }
    };

    //TOOLS:
    private BottomNavigationView bottomNav;
    private LinearLayout geofencingView, preferencesView;
    //TOOLS: Geofencing
    private Button phoneLocation, collarLocation, saveDB;
    private SeekBar meterSlider;
    private TextView meterLabel;
    private int meters = 15;
    //TOOLS: Preferences
    private RadioGroup mapStyle;
    private ImageView geofenceVisibility, geofenceMarkerIcon, petMarkerIcon
                    , geofenceMarkerIcon1, geofenceMarkerIcon2, geofenceMarkerIcon3, geofenceMarkerIcon4, geofenceMarkerIcon5
                    , petMarkerIcon1, petMarkerIcon2, petMarkerIcon3, petMarkerIcon4, petMarkerIcon5
                    , geofenceColorDisplay, petColorDisplay, geofenceCustomizeColor, petCustomizeColor;
    private SeekBar geofenceMarkerSize, petMarkerSize;
    private TextView geofenceMarkerSizeLabel, petMarkerSizeLabel;
    private Button saveDBPreferences;
    private Boolean geofenceVisibilityStatus = true;
    private LinearLayout geofencePreference;
    //TOOLS: OFFLINE
    private TextView dataUnavailableReason, dataUnavailableResolution;
    private ImageView close_dataUnavailable;
    private LinearLayout offlineMode;

    //MAP:
    private MapView mapView;
    private GoogleMap googleMap;
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private Integer MAP_STYLE_CONSTANT;
    private Integer MAP_GEO_ICON_CONSTANT;
    private Integer MAP_PET_ICON_CONSTANT;
    private String  MAP_GEO_COLOR_CONSTANT;
    private String  MAP_PET_COLOR_CONSTANT;
    private Integer  MAP_GEO_SIZE_CONSTANT;
    private Integer  MAP_PET_SIZE_CONSTANT;
    private LatLng viewLatLng;
    private Boolean changedUserView = false;
    //MAP: Pet Location
    private LatLng petLatLng;
    private Marker petMarker;
    //MAP: Geofence Location
    private LatLng geofenceLatLng;
    private LatLng GEOFENCE_LATLNG_CONSTANT;
    private Integer GEOFENCE_RADIUS_CONSTANT;
    private Circle geofenceCircle;
    private Marker geofenceMarker;
    private boolean previousGeofenceState = false;

    //NOTIFICATION
    private NotificationCompat.Builder notif;
    private NotificationManagerCompat managerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        petFinder = PetFinder.getInstance();
        gpsObject = petFinder.getGps();
        gpsObject.setGPSChangeCallback(this);
        repeatSend = petFinder.getRepeatSend();
        geofenceLatLng = petFinder.getGeofenceData().getLatLng();
        databaseHelper = new DatabaseHelper(this);
        if (petFinder.getGeofenceData().getRadius()!=null)
            meters = petFinder.getGeofenceData().getRadius();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        //SET PREFERENCES FROM DATABASE
        initializePreferences();

        isBluetoothConnected = !petFinder.getBluetoothObject().isNull();
        if (isBluetoothConnected) {
            bluetoothGatt = petFinder.getBluetoothObject().getBluetoothGatt();
            characteristic = petFinder.getBluetoothObject().getCharacteristic();
        }

        LinearLayout linearLayout = findViewById(R.id.LocationLayoutView);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        linearLayout.setLayoutTransition(layoutTransition);

        //TOOLS:
        Toolbar myToolbar = findViewById(R.id.pet_toolbar);
        setSupportActionBar(myToolbar);

        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_petProfile:
                        startActivity(new Intent(Location.this, DisplayPetDetails.class));
                        break;
                    case R.id.nav_location:
                        break;
//                    case R.id.statistics:
//                        break;
                }
                return true;
            }
        });

        geofencingView = findViewById(R.id.geofencing);
        preferencesView = findViewById(R.id.preference);
        findViewById(R.id.close_geofencing).setOnClickListener(view -> {
            geofencingView.setVisibility(View.GONE);
            disableMapOnClick();
            if (geofenceMarker!=null) {
                geofenceMarker.setVisible(geofenceVisibilityStatus);
                geofenceCircle.setVisible(geofenceVisibilityStatus);
            }
            if (petFinder.getGeofenceData().getLatLng() != null) {
                geofenceLatLng = petFinder.getGeofenceData().getLatLng(); //GET GEOFENCE DATA FROM DATABASE.
                meters = petFinder.getGeofenceData().getRadius();
                onSetGeofence(geofenceLatLng); //SET IT TO WHAT'S SAVED FROM DATABASE.
            } else {
                geofenceVisibility.setImageResource(R.mipmap.invisible);
                if (geofenceMarker!=null) {
                    geofenceMarker.setVisible(false);
                    geofenceCircle.setVisible(false);
                }
            }
        });
        findViewById(R.id.close_preferences).setOnClickListener(view -> {
            preferencesView.setVisibility(View.GONE);
            initializePreferences();
        });

        //TOOLS: Geofencing
        phoneLocation = findViewById(R.id.phoneLocation);
        collarLocation = findViewById(R.id.collarLocation);
        saveDB = findViewById(R.id.saveDB);
        meterSlider = findViewById(R.id.meterSlider);
        meterLabel = findViewById(R.id.meterLabel);

        phoneLocation.setOnClickListener(view -> {
            LatLng tempGeofenceLatLng = getPhoneLocation();
            if (tempGeofenceLatLng != null) {
                geofenceLatLng = tempGeofenceLatLng;
                onSetGeofence(geofenceLatLng);
            }
        });
        collarLocation.setOnClickListener(view -> {
            if (Objects.equals(petFinder.getGps().getLocationStatus(), "DATA_AVAILABLE")) {
                geofenceLatLng = new LatLng(petFinder.getGps().getLatitude(), petFinder.getGps().getLongitude());
                onSetGeofence(geofenceLatLng);
            }
        });
        saveDB.setOnClickListener(view -> {
            if (geofenceLatLng==null) return;
            if (petFinder.getGeofenceData().getLatLng()==null) { //If no geofence data stored.
                databaseHelper.storeGeofence(petFinder.getCurrentMacAddress(), geofenceLatLng, meters);
                makeText("Geofence saved!");
            }
            else { //If geofence data has been stored.
                databaseHelper.updateGeofence(petFinder.getCurrentMacAddress(), geofenceLatLng, meters);
                makeText("Geofence updated!");
                petFinder.updateGeofenceData();
            }
            geofencingView.setVisibility(View.GONE);
            geofencePreference = findViewById(R.id.geofencePreference);
            if (geofencePreference.getVisibility()!=View.VISIBLE) {
                geofencePreference.setVisibility(View.VISIBLE);
                geofenceVisibility.setImageResource(R.mipmap.visible);
            }
            disableMapOnClick();
            geofenceMarker.setVisible(geofenceVisibilityStatus);
            geofenceCircle.setVisible(geofenceVisibilityStatus);
            GEOFENCE_LATLNG_CONSTANT = geofenceLatLng;
            GEOFENCE_RADIUS_CONSTANT = meters;
        });

        meterSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 1) {
                    meterLabel.setText("1 meter");
                } else {
                    meterLabel.setText(progress + " meters");
                }
                meters = progress;
                updateGeofence(meters);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                meterLabel.setTextColor(getResources().getColor(R.color.grey));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                meterLabel.setTextColor(getResources().getColor(R.color.black));
            }
        });
        meterSlider.setProgress(meters);
        //TOOLS: Preferences
        mapStyle = findViewById(R.id.mapStyle);
        mapStyle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.MAP_TYPE_NORMAL:
                        MAP_STYLE_CONSTANT = GoogleMap.MAP_TYPE_NORMAL;
                        break;
                    case R.id.MAP_TYPE_SATELLITE:
                        MAP_STYLE_CONSTANT = GoogleMap.MAP_TYPE_SATELLITE;
                        break;
                    case R.id.MAP_TYPE_HYBRID:
                        MAP_STYLE_CONSTANT = GoogleMap.MAP_TYPE_HYBRID;
                        break;
                }
                changeMapStyle();
            }
        });
        geofenceVisibility = findViewById(R.id.geofenceVisibility);
        geofenceVisibility.setOnClickListener(view -> {
            geofenceVisibilityStatus = !geofenceVisibilityStatus;
            if (geofenceVisibilityStatus) geofenceVisibility.setImageResource(R.mipmap.visible);
            else geofenceVisibility.setImageResource(R.mipmap.invisible);
            geofenceMarker.setVisible(geofenceVisibilityStatus);
            geofenceCircle.setVisible(geofenceVisibilityStatus);
        });

        geofenceMarkerIcon1 = findViewById(R.id.geofenceMarkerIcon1);
        geofenceMarkerIcon2 = findViewById(R.id.geofenceMarkerIcon2);
        geofenceMarkerIcon3 = findViewById(R.id.geofenceMarkerIcon3);
        geofenceMarkerIcon4 = findViewById(R.id.geofenceMarkerIcon4);
        geofenceMarkerIcon5 = findViewById(R.id.geofenceMarkerIcon5);
        geofenceMarkerIcon = geofenceMarkerIcon1;
        geofenceMarkerIcon.setBackgroundColor(Color.parseColor(MAP_GEO_COLOR_CONSTANT));
        View.OnClickListener geoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geofenceMarkerIcon.setBackgroundColor(getResources().getColor(R.color.transparent));
                geofenceMarkerIcon = findViewById(view.getId());
                geofenceMarkerIcon.setBackgroundColor(Color.parseColor(MAP_GEO_COLOR_CONSTANT));
                MAP_GEO_ICON_CONSTANT = getIconFromView(geofenceMarkerIcon.getId());
                if (petFinder.getGeofenceData().getLatLng()!=null)
                    onSetGeofence(geofenceLatLng);
            }
        };
        geofenceMarkerIcon1.setOnClickListener(geoClickListener);
        geofenceMarkerIcon2.setOnClickListener(geoClickListener);
        geofenceMarkerIcon3.setOnClickListener(geoClickListener);
        geofenceMarkerIcon4.setOnClickListener(geoClickListener);
        geofenceMarkerIcon5.setOnClickListener(geoClickListener);

        petMarkerIcon1 = findViewById(R.id.petMarkerIcon1);
        petMarkerIcon2 = findViewById(R.id.petMarkerIcon2);
        petMarkerIcon3 = findViewById(R.id.petMarkerIcon3);
        petMarkerIcon4 = findViewById(R.id.petMarkerIcon4);
        petMarkerIcon5 = findViewById(R.id.petMarkerIcon5);
        petMarkerIcon = petMarkerIcon1;
        petMarkerIcon.setBackgroundColor(Color.parseColor(MAP_PET_COLOR_CONSTANT));
        View.OnClickListener petClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petMarkerIcon.setBackgroundColor(getResources().getColor(R.color.transparent));
                petMarkerIcon = findViewById(view.getId());
                petMarkerIcon.setBackgroundColor(Color.parseColor(MAP_PET_COLOR_CONSTANT));
                MAP_PET_ICON_CONSTANT = getIconFromView(petMarkerIcon.getId());
                onPetCurrentLocation();
            }
        };
        petMarkerIcon1.setOnClickListener(petClickListener);
        petMarkerIcon2.setOnClickListener(petClickListener);
        petMarkerIcon3.setOnClickListener(petClickListener);
        petMarkerIcon4.setOnClickListener(petClickListener);
        petMarkerIcon5.setOnClickListener(petClickListener);

        geofenceColorDisplay = findViewById(R.id.geofenceColorDisplay);
        geofenceColorDisplay.setBackgroundColor(Color.parseColor(MAP_GEO_COLOR_CONSTANT));
        petColorDisplay = findViewById(R.id.petColorDisplay);
        petColorDisplay.setBackgroundColor(Color.parseColor(MAP_PET_COLOR_CONSTANT));

        geofenceCustomizeColor = findViewById(R.id.geofenceCustomizeColor);
        geofenceCustomizeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(Location.this, Color.parseColor(MAP_GEO_COLOR_CONSTANT), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {}

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        MAP_GEO_COLOR_CONSTANT = "#" + Integer.toHexString(color).substring(2).toUpperCase();
                        if (geofenceLatLng!=null) onSetGeofence(geofenceLatLng);
                        geofenceColorDisplay.setBackgroundColor(color);
                        geofenceMarkerIcon.setBackgroundColor(color);
                    }
                });
                ambilWarnaDialog.show();
            }
        });
        petCustomizeColor = findViewById(R.id.petCustomizeColor);
        petCustomizeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(Location.this, Color.parseColor(MAP_PET_COLOR_CONSTANT), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {}

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        MAP_PET_COLOR_CONSTANT = "#" + Integer.toHexString(color).substring(2).toUpperCase();
                        if (petLatLng!=null) onPetCurrentLocation();
                        petColorDisplay.setBackgroundColor(color);
                        petMarkerIcon.setBackgroundColor(color);
                    }
                });
                ambilWarnaDialog.show();
            }
        });
        geofenceMarkerSizeLabel = findViewById(R.id.geofenceMarkerSizeLabel);
        petMarkerSizeLabel = findViewById(R.id.petMarkerSizeLabel);

        geofenceMarkerSize = findViewById(R.id.geofenceMarkerSize);
        geofenceMarkerSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MAP_GEO_SIZE_CONSTANT = i;
                if (petFinder.getGeofenceData().getLatLng()!=null)
                    onSetGeofence(geofenceLatLng);
                geofenceMarkerSizeLabel.setText((i / 10) + "x");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                geofenceMarkerSizeLabel.setTextColor(getResources().getColor(R.color.grey));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                geofenceMarkerSizeLabel.setTextColor(getResources().getColor(R.color.black));
            }
        });
        petMarkerSize = findViewById(R.id.petMarkerSize);
        petMarkerSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MAP_PET_SIZE_CONSTANT = i;
                onPetCurrentLocation();
                petMarkerSizeLabel.setText((i / 10) + "x");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                petMarkerSizeLabel.setTextColor(getResources().getColor(R.color.grey));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                petMarkerSizeLabel.setTextColor(getResources().getColor(R.color.black));
            }
        });

        saveDBPreferences = findViewById(R.id.saveDBPreferences);
        saveDBPreferences.setOnClickListener(view -> {
            if (petFinder.getMapPreferences().getMapStyle()==null) {
                databaseHelper.storeMapPreferences(
                        petFinder.getCurrentMacAddress(), MAP_STYLE_CONSTANT,
                        MAP_GEO_ICON_CONSTANT, MAP_GEO_COLOR_CONSTANT, MAP_GEO_SIZE_CONSTANT,
                        MAP_PET_ICON_CONSTANT, MAP_PET_COLOR_CONSTANT, MAP_PET_SIZE_CONSTANT);
                makeText("Preferences saved!");
            } else {
                databaseHelper.updateMapPreferences(
                        petFinder.getCurrentMacAddress(), MAP_STYLE_CONSTANT,
                        MAP_GEO_ICON_CONSTANT, MAP_GEO_COLOR_CONSTANT, MAP_GEO_SIZE_CONSTANT,
                        MAP_PET_ICON_CONSTANT, MAP_PET_COLOR_CONSTANT, MAP_PET_SIZE_CONSTANT);
                makeText("Preferences updated!");
                petFinder.updateMapPreferences();
            }
            preferencesView.setVisibility(View.GONE);
            initializePreferences();
        });
        //TOOLS: WARNING
        dataUnavailableReason = findViewById(R.id.dataUnavailableReason);
        dataUnavailableResolution = findViewById(R.id.dataUnavailableResolution);
        close_dataUnavailable = findViewById(R.id.close_dataUnavailable);
        offlineMode = findViewById(R.id.offlineMode);
        close_dataUnavailable.setOnClickListener(view -> removeWarning());

        //MAP:
        ImageView focusPhone = findViewById(R.id.focusPhone);
        ImageView focusPet = findViewById(R.id.focusPet);
        ImageView focusGeofence = findViewById(R.id.focusGeofence);
        focusPhone.setOnClickListener(view -> {
            viewLatLng = getPhoneLocation();
            if (viewLatLng!=null) {
                onSetUserView(viewLatLng);
                Integer grey = getResources().getColor(R.color.grey);
                changeTint(focusPhone, null);
                changeTint(focusPet, grey);
                changeTint(focusGeofence, grey);
            }
        });
        focusPet.setOnClickListener(view -> {
            if (petLatLng!=null) {
                onSetUserView(petLatLng);
                Integer grey = getResources().getColor(R.color.grey);
                changeTint(focusPhone, grey);
                changeTint(focusPet, null);
                changeTint(focusGeofence, grey);
            } else {
                makeText("No pet location detected.");
            }
        });
        focusGeofence.setOnClickListener(view -> {
            if (geofenceLatLng!=null) {
                onSetUserView(geofenceLatLng);
                Integer grey = getResources().getColor(R.color.grey);
                changeTint(focusPhone, grey);
                changeTint(focusPet, grey);
                changeTint(focusGeofence, null);
            } else {
                makeText("No geofence set. Please set one first.");
            }
        });

        mapView = findViewById(R.id.GoogleMaps);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (MAP_STYLE_CONSTANT == null) MAP_STYLE_CONSTANT = GoogleMap.MAP_TYPE_NORMAL;
                Location.this.googleMap = googleMap;
                changeMapStyle();

                if (petFinder.getGeofenceData().getLatLng()!=null && geofenceLatLng != null) {
                    onSetGeofence(geofenceLatLng);
                    GEOFENCE_LATLNG_CONSTANT = petFinder.getGeofenceData().getLatLng();
                    GEOFENCE_RADIUS_CONSTANT = petFinder.getGeofenceData().getRadius();
                } else {
                    geofencePreference.setVisibility(View.GONE);
                }

                //SET WARNING
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled())
                    setWarning("Bluetooth turned off. No data is being received by the system.",
                            "Please turn on your bluetooth device.");
                else if (petFinder.getBluetoothObject().isNull()) {
                    setWarning("Collar not found.",
                            "Please make sure that the collar is turned on. Click here to" +
                                    " connect to device.");
                    dataUnavailableResolution.setOnClickListener(view -> connectToBluetooth());
                }

                //SET CALLBACKS
                if (!petFinder.getBluetoothObject().isNull()){
                    petFinder.getBluetoothObject()
                            .getHandlerInstance()
                            .setConnectionStateChangeCallback(Location.this);
                }

                //CHECK PERMISSION FOR PHONE'S LOCATION
                if (ContextCompat.checkSelfPermission(Location.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(Location.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request permissions if not granted
                    ActivityCompat.requestPermissions(Location.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                } else {
                    viewLatLng = getPhoneLocation();
                    if (viewLatLng!=null) onSetUserView(viewLatLng);
                }

                dataModel.GPSData gpsData = databaseHelper.getLatestGPS(petFinder.getCurrentMacAddress());
                if (gpsData.getLongitude()!=null && gpsData.getLatitude()!=null){
                    petLatLng = new LatLng(gpsData.getLatitude(), gpsData.getLongitude());
                    onPetCurrentLocation();
                }
            }
        });

        //NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("GEOFENCE_NOTIF",
                    "Geofence Notification", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothGattCallbackHandler =
                new BluetoothGattCallbackHandler(Location.this, new Handler());
        bluetoothGattCallbackHandler.setConnectionStateChangeCallback(this);
        bluetoothGattCallbackHandler.setDescriptorWriteCallback(this);
        if (bluetoothAdapter.isEnabled()) {
            Runnable connectToBT = () -> {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(petFinder.getCurrentMacAddress());
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);
                bluetoothGattCallbackHandler.setGatt(bluetoothGatt);
            };
            if (petFinder.getBluetoothObject().isNull()) {
                connectToBT.run();
            } else if (!Objects.equals(petFinder.getBluetoothObject().getBluetoothGatt().getDevice().getAddress(),
                    petFinder.getCurrentMacAddress())){
                connectToBT.run();
            }
        }
    }

    private void initializePreferences() {
        Integer mapStyleTemp = petFinder.getMapPreferences().getMapStyle();
        if (mapStyleTemp!=null) MAP_STYLE_CONSTANT = mapStyleTemp;
        else MAP_STYLE_CONSTANT = GoogleMap.MAP_TYPE_NORMAL; //default
        Integer mapGeoIconTemp = petFinder.getMapPreferences().getGeofenceIcon();
        if (mapGeoIconTemp!=null) MAP_GEO_ICON_CONSTANT = mapGeoIconTemp;
        else MAP_GEO_ICON_CONSTANT = R.drawable.house_marker; //default
        Integer mapPetIconTemp = petFinder.getMapPreferences().getPetIcon();
        if (mapPetIconTemp!=null) MAP_PET_ICON_CONSTANT = mapPetIconTemp;
        else MAP_PET_ICON_CONSTANT = R.drawable.pet_marker; //default
        String mapGeoColorTemp = petFinder.getMapPreferences().getGeofenceColor();
        if (mapGeoColorTemp!=null) MAP_GEO_COLOR_CONSTANT = mapGeoColorTemp;
        else MAP_GEO_COLOR_CONSTANT = "#E6A326"; //default
        String mapPetColorTemp = petFinder.getMapPreferences().getPetColor();
        if (mapPetColorTemp!=null) MAP_PET_COLOR_CONSTANT = mapPetColorTemp;
        else MAP_PET_COLOR_CONSTANT = "#9332D8"; //default
        Integer mapGeoSizeTemp = petFinder.getMapPreferences().getGeofenceSize();
        if (mapGeoSizeTemp!=null) MAP_GEO_SIZE_CONSTANT = mapGeoSizeTemp;
        else MAP_GEO_SIZE_CONSTANT = 30; //default
        Integer mapPetSizeTemp = petFinder.getMapPreferences().getPetSize();
        if (mapPetSizeTemp!=null) MAP_PET_SIZE_CONSTANT = mapPetSizeTemp;
        else MAP_PET_SIZE_CONSTANT = 15; //default
    }

    @SuppressLint("NonConstantResourceId")
    public Integer getIconFromView(int id) {
        switch (id){
            case R.id.petMarkerIcon1: return R.drawable.pet_marker;
            case R.id.petMarkerIcon2: return R.drawable.dog_marker;
            case R.id.petMarkerIcon3: return R.drawable.bone_marker;
            case R.id.petMarkerIcon4:
            case R.id.geofenceMarkerIcon4:
                return R.drawable.star_marker;
            case R.id.petMarkerIcon5:
            case R.id.geofenceMarkerIcon5:
                return R.drawable.circle_marker;
            case R.id.geofenceMarkerIcon1: return R.drawable.house_marker;
            case R.id.geofenceMarkerIcon2: return R.drawable.fence_marker;
            case R.id.geofenceMarkerIcon3: return R.drawable.flag_marker;
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.geofence:
                geofencingView.setVisibility(View.VISIBLE);
                preferencesView.setVisibility(View.GONE);
                enableMapOnClick();
                if (geofenceMarker!=null) {
                    geofenceMarker.setVisible(true);
                    geofenceCircle.setVisible(true);
                }
                initializePreferences();
                break;
            case R.id.preferences:
                preferencesView.setVisibility(View.VISIBLE);
                geofencingView.setVisibility(View.GONE);
                disableMapOnClick();
                if (petFinder.getGeofenceData().getLatLng() != null) {
                    geofenceLatLng = petFinder.getGeofenceData().getLatLng(); //GET GEOFENCE DATA FROM DATABASE.
                    meters = petFinder.getGeofenceData().getRadius();
                    onSetGeofence(geofenceLatLng); //SET IT TO WHAT'S SAVED FROM DATABASE.
                } else {
                    geofenceVisibility.setImageResource(R.mipmap.invisible);
                    if (geofenceMarker!=null) {
                        geofenceMarker.setVisible(false);
                        geofenceCircle.setVisible(false);
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableMapOnClick(){
        googleMap.setOnMapClickListener(this::onSetGeofence);
    }
    private void disableMapOnClick(){
        googleMap.setOnMapClickListener(null);
    }

    private void changeMapStyle(){
        googleMap.setMapType(MAP_STYLE_CONSTANT);
    }

    private void changeTint(ImageView imageView, @ColorInt Integer tint){
        Drawable drawable = imageView.getDrawable();

        Drawable tintedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(tintedDrawable, tint != null ? ColorStateList.valueOf(tint) : null);

        imageView.setImageDrawable(tintedDrawable);
    }

    private BitmapDescriptor BitmapFromVector(Drawable vectorDrawable, Double scaleFactor) {
        int width = (int) (vectorDrawable.getIntrinsicWidth() * scaleFactor);
        int height = (int) (vectorDrawable.getIntrinsicHeight() * scaleFactor);
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @SuppressLint("MissingPermission")
    private void onPetCurrentLocation() {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(petLatLng)
                .icon(BitmapFromVector(changeDrawableColor(MAP_PET_ICON_CONSTANT, Color.parseColor(MAP_PET_COLOR_CONSTANT)),
                        (double) MAP_PET_SIZE_CONSTANT/10))
                .anchor(0.5f, 0.5f);

        if (petMarker != null) petMarker.remove();

        petMarker = googleMap.addMarker(markerOptions);

        if (petFinder.getGeofenceData().getLatLng()==null) return;

        //CHECK IF INSIDE/OUTSIDE GEOFENCE
        android.location.Location locationA = new android.location.Location("point A");
        locationA.setLatitude(petLatLng.latitude);
        locationA.setLongitude(petLatLng.longitude);


        android.location.Location locationB = new android.location.Location("point B");
        locationB.setLatitude(GEOFENCE_LATLNG_CONSTANT.latitude);
        locationB.setLongitude(GEOFENCE_LATLNG_CONSTANT.longitude);

        float distance = locationA.distanceTo(locationB);

        if (distance > GEOFENCE_RADIUS_CONSTANT) { //if pet is outside geofence.
            if (!previousGeofenceState) { //if notification has not yet been sent.
                changeGPSDelay("GPS_STATE_ACTIVE");

                Uri soundUri = Uri.parse("content://com.example.app.provider/assets/geofence_notification.mp3");
                notif = new NotificationCompat.Builder(Location.this, "GEOFENCE_NOTIF");
                notif.setContentTitle("Your Pet Got Out!")
                        .setContentText("The geofence system detected that your pet got out.")
                        .setSmallIcon(R.drawable.ic_pet)
                        .setAutoCancel(true)
                        .setSound(soundUri);

                managerCompat = NotificationManagerCompat.from(Location.this);
                managerCompat.notify(1, notif.build());
                previousGeofenceState = true;
            }
        } else { //if pet is inside geofence.
            if (managerCompat != null){
                managerCompat.cancel(1);
                notif = null;
                managerCompat = null;
            }
            if (previousGeofenceState){
                previousGeofenceState = false;
                changeGPSDelay("GPS_STATE_IDLE");
            }
        }
    }

    private void onSetGeofence(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .icon(BitmapFromVector(changeDrawableColor(MAP_GEO_ICON_CONSTANT, Color.parseColor(MAP_GEO_COLOR_CONSTANT)),
                        (double) MAP_GEO_SIZE_CONSTANT/10))
                .anchor(0.5f, 0.5f);

        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        geofenceMarker = googleMap.addMarker(markerOptions);

        geofenceCircle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(meters)
                .strokeColor(ContextCompat.getColor(this, R.color.teal_transparent_700))
                .fillColor(ContextCompat.getColor(this, R.color.teal_transparent_200)));
        geofenceLatLng = latLng;
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable changeDrawableColor(Integer drawableID, Integer color) {
        Drawable modifiedDrawable = getResources().getDrawable(drawableID).mutate();
        modifiedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return modifiedDrawable;
    }

    private void onSetUserView(LatLng latLng){
        changedUserView = true;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }

    private void updateGeofence(int radius){
        if (geofenceCircle != null){
            geofenceCircle.setRadius(radius);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //PERMISSION RESULT FOR PHONE'S LOCATION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewLatLng = getPhoneLocation();
                if (viewLatLng!=null) onSetUserView(viewLatLng);
            }
        }
    }

    //THIS IS TO USE PHONE'S LOCATION
    @SuppressLint("MissingPermission")
    @Nullable
    private LatLng getPhoneLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            final double[] latitude = new double[1];
            final double[] longitude = new double[1];
            final CountDownLatch latch = new CountDownLatch(1);

            if (lastKnownLocation != null) {
                latitude[0] = lastKnownLocation.getLatitude();
                longitude[0] = lastKnownLocation.getLongitude();
                return new LatLng(latitude[0], longitude[0]);
            } else {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull android.location.Location location) {
                        latitude[0] = location.getLatitude();
                        longitude[0] = location.getLongitude();
                        latch.countDown();
                    }
                }, null);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new LatLng(latitude[0], longitude[0]);
            }
        } else {
            makeText("Please turn on GPS Location.");
            return null;
        }
    }

    private void changeGPSDelay(String data) {
        if (!isBluetoothConnected) return;
        if (bluetoothGatt != null && characteristic != null) {
            // Characteristic available, proceed with sending data
            repeatSend.addString(data).startSending();
        }
    }

    private void makeText(String message){
        Toast.makeText(Location.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGPSChange(LatLng latLng) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                petLatLng = latLng;
                if (googleMap!=null) onPetCurrentLocation();
            }
        });
    }

    @Override
    public void onGPSNoData() {
        setWarning("No GPS Signal.",
                 "Please wait until the collar detects a GPS signal.");
    }

    private void setWarning(String reason, String resolution){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                offlineMode.setVisibility(View.VISIBLE);
                dataUnavailableReason.setText(reason);
                dataUnavailableResolution.setText(resolution);
            }
        });
    }
    private void removeWarning(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                offlineMode.setVisibility(View.GONE);
                dataUnavailableReason.setText("");
                dataUnavailableResolution.setText("");
                dataUnavailableResolution.setOnClickListener(null);
            }
        });
    }

    @Override
    public void onWait() {
        if (petFinder.bluetoothObject.isNull()) {
            petFinder.setBluetoothObject(bluetoothGatt,
                    bluetoothGattCallbackHandler.getCharacteristic(),
                    bluetoothGattCallbackHandler);
        }
        removeWarning();
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onConnectionStateChange(boolean isConnected) {
        if (isConnected){
            removeWarning();
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        } else {
            setWarning("Collar not found.",
                    "Please make sure that the collar is turned on. Click here to" +
                            " connect to device.");
            dataUnavailableResolution.setOnClickListener(view -> connectToBluetooth());
            petFinder.deleteBluetoothObject();
        }
    }
}
