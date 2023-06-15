package com.example.petfinder.pages.pet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.Adapters.ScanBTListViewAdapter;
import com.example.petfinder.R;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;

import java.util.ArrayList;
import java.util.List;

public class ScanBluetooth extends AppCompatActivity
                                implements BluetoothGattCallbackHandler.ConnectionStateCallback
                                         , BluetoothGattCallbackHandler.CharacteristicChangedCallback
                                         , BluetoothGattCallbackHandler.CharacteristicReadCallback
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

    private boolean isScanning = false;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallbackHandler bluetoothGattCallbackHandler;

    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable = () -> runOnUiThread(() -> {
        //disconnect
        disconnectGatt();
        Toast.makeText(ScanBluetooth.this, "Invalid device. Disconnected.", Toast.LENGTH_SHORT).show();
    });

    private RecyclerView mRecyclerView;
    private ScanBTListViewAdapter scanBTListViewAdapter;
    List<ScannedDevices> DeviceScanList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bluetooth);

        scanButton = findViewById(R.id.scanButton);

        Handler handler = new Handler(Looper.getMainLooper());

        bluetoothGattCallbackHandler = new BluetoothGattCallbackHandler(ScanBluetooth.this, handler);
        bluetoothGattCallbackHandler.setConnectionStateCallback(this);
        bluetoothGattCallbackHandler.setCharacteristicChangedCallback(this);
        bluetoothGattCallbackHandler.setCharacteristicReadCallback(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if the device supports BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check if Bluetooth is enabled, and if not, request to enable it
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect and close the BluetoothGatt instance
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

//    private void saveGeofencePerimeter() {
//        if (!isBluetoothConnected) {
//            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!latitudeText.isEmpty() && !longitudeText.isEmpty()) {
//            try {
//                geofenceLatitude = Double.parseDouble(latitudeText);
//                geofenceLongitude = Double.parseDouble(longitudeText);
//
//                Toast.makeText(this, "Geofence perimeter saved", Toast.LENGTH_SHORT).show();
//                sendData(geofenceLatitude, geofenceLongitude);
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "Latitude or longitude cannot be empty", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void sendData(Double geolat, Double geolon) {
//
//        String data = geolat + ";" + geolon;
//
//        if (!isBluetoothConnected) {
//            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (bluetoothGatt != null && characteristic != null) {
//            // Characteristic available, proceed with sending data
//            characteristic.setValue(data);
//
//            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
//            if (success) {
//                Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "Bluetooth connection or characteristic not available", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void startScan() {
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
                // Location permission granted, start scanning
                startScan();
            } else {
                // Location permission denied, show a dialog and ask the user to grant permission
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("Please grant location permission to scan for Bluetooth devices.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Open app settings to allow the user to grant permission
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        // Disconnect any existing connection
        disconnectGatt();

        // Connect to the selected device
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);
    }

    private void disconnectGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        isBluetoothConnected = isConnected;
        if (isBluetoothConnected) {
            int timeoutMillis = 5000;
            timeoutHandler.postDelayed(timeoutRunnable, timeoutMillis);
        } else {
            // Clean up
            disconnectGatt();
        }
    }


    @Override
    public void onCharacteristicChanged(String value) {
        runOnUiThread(() -> {
            String VALID_DEVICE = "rDmI4NXH08";
            if (value.equals(VALID_DEVICE)) {
                Toast.makeText(ScanBluetooth.this, "Received Data: " + value, Toast.LENGTH_SHORT).show();
                timeoutHandler.removeCallbacks(timeoutRunnable);
                Intent intent = new Intent(ScanBluetooth.this, AddPet.class);
                intent.putExtra("MAC_ADDRESS", bluetoothAddress);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
                finish();
            } else {
                //clean up
                disconnectGatt();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        BluetoothDevice device = scanResults.get(position).getDevice();
        bluetoothAddress = device.getAddress();
        connectToDevice(device);
    }

    @Override
    public void onCharacteristicReadCallback(String value) {
        runOnUiThread(() -> {
            String VALID_DEVICE = "rDmI4NXH08";
            if (value.equals(VALID_DEVICE)) {
                Toast.makeText(ScanBluetooth.this, "Received Data: " + value, Toast.LENGTH_SHORT).show();
                timeoutHandler.removeCallbacks(timeoutRunnable);
                Intent intent = new Intent(ScanBluetooth.this, AddPet.class);
                intent.putExtra("MAC_ADDRESS", bluetoothAddress);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
                finish();
            } else {
                //clean up
                disconnectGatt();
            }
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