package com.example.petfinder.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class BluetoothObject {
    BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattCallbackHandler handlerInstance;

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCallbackHandler getHandlerInstance() {
        return handlerInstance;
    }

    public void setHandlerInstance(BluetoothGattCallbackHandler handlerInstance) {
        this.handlerInstance = handlerInstance;
    }

    public void nullify(){
        this.setBluetoothGatt(null);
        this.setCharacteristic(null);
        this.setHandlerInstance(null);
    }

    public boolean isNull(){
        boolean is_null = false;
        if (this.bluetoothGatt == null) {
            is_null = true;
        }
        if (this.characteristic == null) {
            is_null = true;
        }
        if (this.handlerInstance == null) {
            is_null = true;
        }
        return is_null;
    }
}
