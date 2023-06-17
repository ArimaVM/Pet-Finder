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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    private RecyclerView mRecyclerView;

    private ScanBTListViewAdapter scanBTListViewAdapter;
    List<ScannedDevices> DeviceScanList;

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

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
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

        scanButton = findViewById(R.id.scanButton);

        Handler handler = new Handler(Looper.getMainLooper());

        bluetoothGattCallbackHandler = new BluetoothGattCallbackHandler(ScanBluetooth.this, handler);
        bluetoothGattCallbackHandler.setConnectionStateCallback(this);

        // Check if the device supports BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check if Bluetooth is enabled, and if not, request to enable it
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled, initialize the Bluetooth LE scanner
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            } else {
                // Bluetooth enabling was canceled or failed, handle the error
                Toast.makeText(this, "Failed to enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void connectToDevice(BluetoothDevice device) {
        // Disconnect any existing connection
        disconnectGatt();

        // Connect to the selected device
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);
        Intent intent = new Intent(ScanBluetooth.this, AddPet.class);
        intent.putExtra("MAC_ADDRESS", bluetoothAddress);
        intent.putExtra("isEditMode", false);
        startActivity(intent);
        finish();
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
    }

    @Override
    public void onItemClick(int position) {
        BluetoothDevice device = scanResults.get(position).getDevice();
        bluetoothAddress = device.getAddress();
        connectToDevice(device);
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