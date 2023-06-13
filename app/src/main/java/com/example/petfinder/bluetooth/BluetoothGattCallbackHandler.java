package com.example.petfinder.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.petfinder.pages.splash.MainActivity;

import java.util.UUID;

public class BluetoothGattCallbackHandler extends BluetoothGattCallback {

    private static final String TAG = BluetoothGattCallbackHandler.class.getSimpleName();

    private static final UUID SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic characteristic;

    private Context context; // Context reference for displaying Toast
    private Handler handler;

    public BluetoothGattCallbackHandler(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            // Connected to GATT server
            displayToast("Connected to device.");

            // Discover services
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            // Disconnected from GATT server
            displayToast("Disconnected to device.");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Find the service and characteristic of interest
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);

            // Perform operations on the characteristic
            // For example, read or write data

            // Read the characteristic value
            gatt.readCharacteristic(characteristic);
        } else {
            displayToast("Service discovery failed");
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Characteristic read successfully
            final String value = characteristic.getStringValue(0);
            displayToast("Characteristic value: " + value);
        } else {
            displayToast("Characteristic read failed");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        // Characteristic value changed
        Log.d(TAG, "Characteristic value changed");

        // Parse and process the updated characteristic value if needed
        byte[] value = characteristic.getValue();
        if (value != null && value.length > 0) {
            // Process the updated value
        }
    }

    private void displayToast(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}