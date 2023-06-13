package com.example.petfinder.application;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.petfinder.pages.pet.ScanBluetooth;

import java.nio.charset.Charset;

public class PetFinder extends Application {
    private static final String TAG = "PetFinder";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application started");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }

}
