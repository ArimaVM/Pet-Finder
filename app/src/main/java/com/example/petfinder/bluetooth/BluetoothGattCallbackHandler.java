package com.example.petfinder.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BluetoothGattCallbackHandler extends BluetoothGattCallback {

    private BluetoothGattCharacteristic characteristic;
    private ConnectionStateCallback connectionStateCallback;
    private ServiceDiscoveredCallback serviceDiscoveredCallback;
    private CharacteristicChangedCallback characteristicChangedCallback;

    private Context context; // Context reference for displaying Toast
    private Handler handler;

    public BluetoothGattCallbackHandler(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public interface ConnectionStateCallback {
        void onConnectionStateChanged(boolean isConnected);
    }

    public void setConnectionStateCallback(ConnectionStateCallback callback) {
        this.connectionStateCallback = callback;
    }

    public interface CharacteristicChangedCallback {
        void onCharacteristicChanged(String value);
    }

    public void setCharacteristicChangedCallback(CharacteristicChangedCallback callback) {
        this.characteristicChangedCallback = callback;
    }

    public interface ServiceDiscoveredCallback {
        void onServiceDiscoveredCallback(BluetoothGattCharacteristic characteristic);
    }

    public void setServiceDiscoveredCallback(ServiceDiscoveredCallback callback) {
        this.serviceDiscoveredCallback = callback;
    }


    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            // Device connected
            makeToast("Connected to device");
            //Return true
            if (connectionStateCallback != null) {
                connectionStateCallback.onConnectionStateChanged(true);
            }
            // Discover services
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            // Device disconnected
            makeToast("Disconnected from device");
            //Return false
            if (connectionStateCallback != null) {
                connectionStateCallback.onConnectionStateChanged(false);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Find the characteristic you want to receive data from
            BluetoothGattService service = gatt.getService(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB"));
            characteristic = service.getCharacteristic(UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB"));

            if (serviceDiscoveredCallback != null) {
                serviceDiscoveredCallback.onServiceDiscoveredCallback(characteristic);
            }
            // Enable notifications for the characteristic
            boolean isNotificationEnabled = gatt.setCharacteristicNotification(characteristic, true);

            if (isNotificationEnabled) {
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        // Check if the descriptor write operation was successful
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Ready to receive data from the characteristic
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        if (status != BluetoothGatt.GATT_SUCCESS) {
            makeToast("Characteristic read failed.");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        byte[] data = characteristic.getValue();
        String receivedData = new String(data, StandardCharsets.UTF_8);
        Log.d("Bluetooth", "Received data: " + receivedData);
        //Return receivedData
        if (characteristicChangedCallback != null) {
            characteristicChangedCallback.onCharacteristicChanged(receivedData);
        }
    }

    private void makeToast(String message){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}