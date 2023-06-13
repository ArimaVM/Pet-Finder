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
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanBluetooth extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_LOCATION_PERMISSION = 2;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private List<ScanResult> scanResults;
    private ArrayAdapter<String> deviceListAdapter;

    private ListView deviceListView;
    private Button scanButton;

    private Handler handler = new Handler();
    private boolean isScanning = false;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;

    private static final UUID SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCallbackHandler bluetoothGattCallback;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bluetooth);


        deviceListView = findViewById(R.id.deviceList);
        scanButton = findViewById(R.id.scanButton);

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

        bluetoothGattCallback = new BluetoothGattCallbackHandler(getApplicationContext(), new Handler(Looper.getMainLooper()));
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
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
    }

    private void disconnectGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}