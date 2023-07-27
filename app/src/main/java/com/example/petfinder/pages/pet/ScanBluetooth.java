package com.example.petfinder.pages.pet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.components.Dashboard;
import com.example.petfinder.container.RecordModel;
import com.example.petfinder.container.ScanBTListViewAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanBluetooth extends AppCompatActivity
                                implements BluetoothGattCallbackHandler.ConnectionStateChangeCallback
                                         , BluetoothGattCallbackHandler.DescriptorWriteCallback
                                         , BluetoothGattCallbackHandler.CharacteristicChangedCallback
                                         , ScanBTListViewAdapter.OnItemClickListener {


    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_LOCATION_PERMISSION = 2;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private List<ScanResult> scanResults;

    private String bluetoothAddress;
    private Button scanButton;
    private boolean isBluetoothConnected = false;
    PetFinder petFinder;

    private boolean isScanning = false;
    private String unlistedID = "";

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothDevice device;
    private BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
    private PetFinder.RepeatSend repeatSend;

    private RecyclerView mRecyclerView;

    private ScanBTListViewAdapter scanBTListViewAdapter;
    List<ScannedDevices> DeviceScanList;

    Handler handler2 = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d("TIMEOUT", "THE TIMEOUT HAS RAN AT: " + System.currentTimeMillis());
            repeatSend.stopSending();
            disconnectGatt();
            Toast.makeText(ScanBluetooth.this, "Invalid Device.", Toast.LENGTH_SHORT).show();
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bluetooth);;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported, show an error message or take appropriate action
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getIntent().hasExtra("UNLISTED")){
            //if contains `UNLISTED`, it means the purpose of this is to import.
            unlistedID = getIntent().getStringExtra("UNLISTED");
        }

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Set the custom back arrow as the navigation icon
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Set a click listener on the navigation icon
        myToolbar.setNavigationOnClickListener(v -> onBackPressed());

        scanButton = findViewById(R.id.scanButton);

        Handler handler = new Handler(Looper.getMainLooper());

        petFinder = PetFinder.getInstance();

        bluetoothGattCallbackHandler = new BluetoothGattCallbackHandler(ScanBluetooth.this, handler);
        bluetoothGattCallbackHandler.setConnectionStateChangeCallback(this);
        bluetoothGattCallbackHandler.setDescriptorWriteCallback(this);
        bluetoothGattCallbackHandler.setCharacteristicChangedCallback(this);

        repeatSend = petFinder.getRepeatSend();

        // Check if the device supports BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check if Bluetooth is enabled, and if not, request to enable it
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else checkLocationPermissions();
        }

        // Initialize the Bluetooth LE scanner and scan settings
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        // Create an empty list for scan results
        scanResults = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recyclerview);
        // Create a recyclerview for the device
        DeviceScanList = new ArrayList<>();
        scanBTListViewAdapter = new ScanBTListViewAdapter(this, DeviceScanList);
        scanBTListViewAdapter.setOnItemClickListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(scanBTListViewAdapter);
        scanBTListViewAdapter.notifyDataSetChanged();

        // Set click listener for scan button
        scanButton.setOnClickListener(v -> {
            if (isScanning) {
                stopScan();
            } else {
                startScan();
            }
        });

    }

    private void askForVerification() {
        String data = "SEND_DEVICE_CODE";

        if (!isBluetoothConnected) { return; }
        if (bluetoothGatt != null && characteristic != null) {
            // Characteristic available, proceed with sending data
            repeatSend.addString(data).startSending();
            if (Objects.equals(repeatSend.currentString(), "SEND_DEVICE_CODE")){
                while (repeatSend.successStatus() == null); // wait until repeatSend actually started the loop.
                if (repeatSend.successStatus()) {
                    handler2.postDelayed(runnable, 2000);
                    Log.d("TIMEOUT", "I SET THE TIMEOUT AT: " + System.currentTimeMillis());
                    makeToastOnUI("Verifying...");
                } else {
                    makeToastOnUI("Verification failed. Please try again later.");
                }
            }
        } else {
            makeToastOnUI("An error occured. Please try again later.");
        }
    }

    public void checkLocationPermissions(){
        //CHECK IF PERMISSION IS GRANTED
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            //REQUEST TO ENABLE LOCATION IF PERMISSION IS GRANTED.
            enableLoc();
        }
    }

    private void startScan() {
        // Check if Bluetooth LE scanner is initialized
        if (bluetoothLeScanner != null) {
            // Clear the previous scan results
            scanResults.clear();
            // Clear recyclerview
            int itemCount = DeviceScanList.size();
            DeviceScanList.clear();
            scanBTListViewAdapter.notifyItemRangeRemoved(0, itemCount);

            // Start scanning for BLE devices
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);

            // Update UI
            scanButton.setText("Stop Scan");
            isScanning = true;
        } else {
            // Bluetooth LE scanner is not initialized, show an error message
            Toast.makeText(this, "Failed to initialize Bluetooth LE scanner", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan() {
        // Stop scanning for BLE devices
        bluetoothLeScanner.stopScan(scanCallback);

        // Update UI
        scanButton.setText("Start Scan");
        isScanning = false;
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            // Handle scan result here
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();

            if (deviceName == null) return;

            // Check if the device is already in the list
            boolean isNewDevice = true;
            for (ScanResult scanResult : scanResults) {
                if (scanResult.getDevice().getAddress().equals(device.getAddress())) {
                    isNewDevice = false;
                    break;
                }
            }

            // Add the device to the list if it's not already present
            if (isNewDevice) {
                scanResults.add(result);
                addData(new ScannedDevices(deviceName, deviceAddress));
            }
        }

        private void addData(ScannedDevices data){
            runOnUiThread(() -> {
                DeviceScanList.add(data);
                scanBTListViewAdapter.notifyDataSetChanged();
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            // Handle scan failure here
            Toast.makeText(ScanBluetooth.this, "Scan failed with error code: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //ENABLE LOCATION
                enableLoc();
            } else {
                Toast.makeText(this, "Location services is necessary to scan the device.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(ScanBluetooth.this, Dashboard.class));
                finish();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled, initialize the Bluetooth LE scanner
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                checkLocationPermissions();
            } else {
                // Bluetooth enabling was canceled or failed, handle the error
                Toast.makeText(this, "Bluetooth is necessary to scan the device.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(ScanBluetooth.this, Dashboard.class));
                finish();
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        // Disconnect any existing connection
        disconnectGatt();
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);
        bluetoothGattCallbackHandler.setGatt(bluetoothGatt);
    }

    //BluetoothGattCallback start
    @Override
    public void onConnectionStateChange(boolean isConnected) {
        isBluetoothConnected = isConnected;
        if (!isBluetoothConnected) disconnectGatt();
    }

    @Override
    public void onWait() {
        characteristic = bluetoothGattCallbackHandler.getCharacteristic();
        petFinder.setBluetoothObject(bluetoothGatt, characteristic, bluetoothGattCallbackHandler, false);
        askForVerification();
    }

    private void enableLoc() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(
                                        ScanBluetooth.this, 1001);
                            } catch (IntentSender.SendIntentException ignored) {}
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: break;
                    }
                }
            }
        });

    }

    @Override
    public void onItemClick(int position, boolean isUsed) {

        device = scanResults.get(position).getDevice();
        bluetoothAddress = device.getAddress();

        if (isUsed) {
            petFinder.setCurrentMacAddress(bluetoothAddress);
            ScanBluetooth.this.startActivity(new Intent(ScanBluetooth.this, DisplayPetDetails.class));
            finish();
        } else {
            Toast.makeText(ScanBluetooth.this, "Trying to connect...", Toast.LENGTH_SHORT).show();
            connectToDevice(device);
        }
    }

    @Override
    public void onCharacteristicChanged(String value) {
        if (value.equals("rDmI4NXH08")){
            Log.d("TIMEOUT", "CODE IS RECEIVED AT: " + System.currentTimeMillis());
            repeatSend.stopSending();
            handler2.removeCallbacks(runnable);
            makeToastOnUI("Device Validation Success!");
            // Connect to the selected device
            onValidationSuccess();
        } else {
            makeToastOnUI("Invalid Device.");
        }
    }

    //BluetoothGattCallback end

    private void onValidationSuccess(){
        /*
           This creates new bluetoothGatt, characteristic, and bluetoothGattCallbackHandler, which
           is why it does not need the code snippet:
               if (petFinder.bluetoothObject.isNull()){...}
        */
        if (unlistedID.isEmpty()) {
            petFinder.setCurrentMacAddress(bluetoothAddress);
            petFinder.setBluetoothObject(bluetoothGatt, characteristic, bluetoothGattCallbackHandler);
            startActivity(new Intent(ScanBluetooth.this, AddPet.class));
            finish();
        } else {
            petFinder.setCurrentMacAddress(bluetoothAddress);
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            RecordModel petModel = null;
            for (RecordModel recordModel: PetFinder.getInstance().getUnlistedPets()) {
                if (recordModel.getPetFeederID().equals(unlistedID)) petModel = recordModel;
            }
            if (petModel!=null) {
                databaseHelper.storeData(
                        ""+bluetoothAddress,
                        ""+petModel.getName(),
                        ""+petModel.getBreed(),
                        ""+petModel.getSex(),
                        ""+petModel.getBirthdate(),
                        Integer.parseInt(petModel.getAge()),
                        Integer.parseInt(petModel.getWeight()),
                        ""+petModel.getImage(),
                        ""+petModel.getAddedtime(),
                        ""+petModel.getUpdatedtime(),
                        ""+unlistedID
                        );
                databaseHelper.updateHealthInfo(
                            "" + bluetoothAddress,
                            "" + petModel.getAllergies(),
                        ""+petModel.getMedications(),
                        ""+petModel.getVetname(),
                            ""+petModel.getVetContact());
            }

            petFinder.setBluetoothObject(bluetoothGatt, characteristic, bluetoothGattCallbackHandler);
            startActivity(new Intent(ScanBluetooth.this, DisplayPetDetails.class));
            finish();
        }
    }

    private void disconnectGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private void makeToastOnUI(String message){
        runOnUiThread(() -> {
            Toast.makeText(ScanBluetooth.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    public class ScannedDevices {
        private String DeviceName;
        private String MACAddress;

        public ScannedDevices(String DeviceName, String MACAddress) {
            this.DeviceName = DeviceName;
            this.MACAddress = MACAddress;
        }

        public String getDeviceName() {
            return DeviceName;
        }

        public void setDeviceName(String deviceName) {
            DeviceName = deviceName;
        }

        public String getMACAddress() {
            return MACAddress;
        }

        public void setMACAddress(String MACAddress) {
            this.MACAddress = MACAddress;
        }
    }

}