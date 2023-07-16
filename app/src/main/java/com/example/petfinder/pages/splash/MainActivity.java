package com.example.petfinder.pages.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

import com.example.petfinder.components.Dashboard;
import com.example.petfinder.R;
import com.example.petfinder.pages.NotFound.NotFound;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    ProgressBar pb;
    int counter = 0;

    private final String NO_BLUETOOTH = "Bluetooth not supported.";
    private final String NO_BLUETOOTH_LE = "Bluetooth Low-Energy not supported.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prog();
    }

    public void prog() {
        pb = findViewById(R.id.pb);

        final Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                counter++;
                pb.setProgress(counter);

                if(counter == 100) {
                    t.cancel();
                    CheckForAppCompatibility();

                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    if (!sharedPreferences.getBoolean(OnBoarding.COMPLETED_ONBOARDING_PREF_NAME, false)) {
                        startActivity(new Intent(MainActivity.this, OnBoarding.class));
                        finish();
                    } else {
                        startActivity(new Intent(MainActivity.this, Dashboard.class));
                        finish();
                    }
                }
            }
        };

        t.schedule(tt, 0, 30);
    }

    private void CheckForAppCompatibility(){
        //Bluetooth compatibility
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            NotFound(NO_BLUETOOTH);
        }

        //BLE compatibility
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            NotFound(NO_BLUETOOTH_LE);
        }
    }

    private void NotFound(String reason){
        Intent intent = new Intent(MainActivity.this, NotFound.class);
        intent.putExtra("REASON", reason);
        startActivity(intent);
    }
}