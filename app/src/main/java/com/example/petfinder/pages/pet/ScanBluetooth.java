package com.example.petfinder.pages.pet;

import com.example.petfinder.R;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanBluetooth extends AppCompatActivity
                                implements BluetoothGattCallbackHandler.ConnectionStateCallback
                                         , BluetoothGattCallbackHandler.CharacteristicChangedCallback
                                         , BluetoothGattCallbackHandler.ServiceDiscoveredCallback {


    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_LOCATION_PERMISSION = 2;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private List<ScanResult> scanResults;
    private ArrayAdapter<String> deviceListAdapter;

    private ListView deviceListView;
    private Button scanButton;
    private boolean isBluetoothConnected = false;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Button saveButton;

    private BluetoothGattCharacteristic characteristic;

    private double geofenceLatitude;
    private double geofenceLongitude;

    private boolean isScanning = false;

    private BluetoothGatt bluetoothGatt;

    private static final UUID SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private TextView receivedDataTextView;

    private BluetoothGattCallbackHandler bluetoothGattCallbackHandler;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bluetooth);

        deviceListView = findViewById(R.id.deviceList);
        scanButton = findViewById(R.id.scanButton);
        receivedDataTextView = findViewById(R.id.receivedDataTextView);

        Handler handler = new Handler(Looper.getMainLooper());

        bluetoothGattCallbackHandler = new BluetoothGattCallbackHandler(ScanBluetooth.this, handler);
        bluetoothGattCallbackHandler.setConnectionStateCallback(this);
        bluetoothGattCallbackHandler.setCharacteristicChangedCallback(this);
        bluetoothGattCallbackHandler.setServiceDiscoveredCallback(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // Connect to the Bluetooth device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("F4:B8:5E:94:7A:85");
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);

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

        // Create an adapter for the device list view
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        deviceListView.setAdapter(deviceListAdapter);

        // Set click listener for device list items
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle device selection here
                BluetoothDevice device = scanResults.get(position).getDevice();
                connectToDevice(device);
            }
        });

        // Set click listener for scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    stopScan();
                } else {
                    startScan();
                }
            }
        });

        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGeofencePerimeter();
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

    private void saveGeofencePerimeter() {
        if (!isBluetoothConnected) {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        String latitudeText = latitudeEditText.getText().toString();
        String longitudeText = longitudeEditText.getText().toString();

        if (!latitudeText.isEmpty() && !longitudeText.isEmpty()) {
            try {
                geofenceLatitude = Double.parseDouble(latitudeText);
                geofenceLongitude = Double.parseDouble(longitudeText);

                Toast.makeText(this, "Geofence perimeter saved", Toast.LENGTH_SHORT).show();
                sendData(geofenceLatitude, geofenceLongitude);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Latitude or longitude cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendData(Double geolat, Double geolon) {

        String data = geolat + ";" + geolon;

        if (!isBluetoothConnected) {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothGatt != null && characteristic != null) {
            // Characteristic available, proceed with sending data
            characteristic.setValue(data);

            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
            if (success) {
                Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth connection or characteristic not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startScan() {
        // Clear the previous scan results
        scanResults.clear();
        deviceListAdapter.clear();

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
            int rssi = result.getRssi();

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
                deviceListAdapter.add(deviceName + " (" + deviceAddress + ") - RSSI: " + rssi + "dBm");
            }
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

//    private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//        gatt.setCharacteristicNotification(characteristic, true);
//
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(SERVICE_UUID);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        gatt.writeDescriptor(descriptor);
//    }

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
        if (!isBluetoothConnected){
            // Clean up
            disconnectGatt();
        }
    }

    @Override
    public void onCharacteristicChanged(String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receivedDataTextView.setText("Received Data: " + value );
            }
        });
    }

    @Override
    public void onServiceDiscoveredCallback(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }
}