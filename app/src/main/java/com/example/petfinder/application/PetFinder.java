package com.example.petfinder.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.pages.pet.ScanBluetooth;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PetFinder extends Application
                        implements BluetoothGattCallbackHandler.CharacteristicChangedCallback{
    private static final String TAG = "PetFinder";

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallbackHandler bluetoothGattCallbackHandler;

    private String MAC_ADDRESS;
    private static PetFinder instance;
    private boolean isConnected;
    private List<DataObserver> observers = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Application started");

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public interface DataObserver {
        void onDataUpdated(boolean newData);
    }

    public static synchronized PetFinder getInstance(){
        return instance;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
        notifyObservers(isConnected);

        new UpdateDataTask().execute(isConnected);
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt){
        this.bluetoothGatt = bluetoothGatt;
    }

    public void registerObserver(DataObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(DataObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(boolean newData) {
        for (DataObserver observer : observers) {
            observer.onDataUpdated(newData);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }

    private class UpdateDataTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... booleans) {
            Boolean data = booleans[0];

            if (data) {
                // Bluetooth is on
                MAC_ADDRESS = bluetoothGatt.getDevice().getAddress();
            } else {
                MAC_ADDRESS = null;
            }
            return null;
        }
    }

    @Override
    public void onCharacteristicChanged(String value) {
        //save to MAC_ADDRESS
    }
}
