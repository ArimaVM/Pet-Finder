package com.example.petfinder.application;

import android.app.Application;
import android.util.Log;

public class PetFinder extends Application {
    private static final String TAG = "PetFinder";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application started");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }
}
