package com.example.petfinder.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BluetoothGattCallbackHandler extends BluetoothGattCallback {

    private BluetoothGattCharacteristic characteristic;
    private BluetoothGatt bluetoothGatt;

    private ConnectionStateChangeCallback connectionStateChangeCallback;
    private DescriptorWriteCallback descriptorWriteCallback;
    private CharacteristicReadCallback characteristicReadCallback;
    private CharacteristicChangedCallback characteristicChangedCallback;


    private Context context; // Context reference for displaying Toast
    private Handler handler;

    public BluetoothGattCallbackHandler(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    //GETTER AND SETTER


    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }

    public void setGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    //INTERFACES
    public interface ConnectionStateChangeCallback{
        void onConnectionStateChange(boolean isConnected);
    }
    public void setConnectionStateChangeCallback(ConnectionStateChangeCallback callback){
        this.connectionStateChangeCallback = callback;
    }

    public interface DescriptorWriteCallback{
        void onWait();
    }
    public void setDescriptorWriteCallback(DescriptorWriteCallback callback){
        this.descriptorWriteCallback = callback;
    }

    public interface CharacteristicReadCallback{
        void onCharacteristicRead(String value);
    }
    public void setCharacteristicReadCallback(CharacteristicReadCallback callback){
        this.characteristicReadCallback = callback;
    }

    public interface CharacteristicChangedCallback{
        void onCharacteristicChanged(String value);
    }
    public void setCharacteristicChangedCallback(CharacteristicChangedCallback callback){
        this.characteristicChangedCallback = callback;
    }


    //OVERRIDES
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            // Device connected
            makeToast("Connected to device");
            if (connectionStateChangeCallback != null) {
                connectionStateChangeCallback.onConnectionStateChange(true);
            }

            // Discover services
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            // Device disconnected
            makeToast("Disconnected from device");
            if (connectionStateChangeCallback != null) {
                connectionStateChangeCallback.onConnectionStateChange(false);
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

            // Enable notifications for the characteristic
            boolean isNotificationEnabled = bluetoothGatt.setCharacteristicNotification(characteristic, true);

            if (isNotificationEnabled) {
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                }
            }
            gatt.readCharacteristic(characteristic);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        // Check if the descriptor write operation was successful
        if (status == BluetoothGatt.GATT_SUCCESS && descriptorWriteCallback != null) {
            descriptorWriteCallback.onWait();
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Characteristic read successfully
            final String value = characteristic.getStringValue(0);
            if (characteristicReadCallback != null) {
                characteristicReadCallback.onCharacteristicRead(value.trim());
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        byte[] data = characteristic.getValue();
        String receivedData = new String(data, StandardCharsets.UTF_8);
        if (characteristicChangedCallback != null) {
            characteristicChangedCallback.onCharacteristicChanged(receivedData.trim());
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