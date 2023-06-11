package com.example.petfinder.application;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

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
