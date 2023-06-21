package com.example.petfinder.application;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.bluetooth.BluetoothObject;
import com.example.petfinder.container.dataModel.PedometerData;
import com.example.petfinder.container.dataModel.GPSData;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PetFinder extends Application{
    private static final String TAG = "PetFinder";
    public BluetoothObject bluetoothObject;
    private static PetFinder instance;
    private Pedometer pedometer;
    private GPS gps;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothObject = new BluetoothObject();
        pedometer = new Pedometer(PetFinder.this, getCurrentDate());
        instance = this;
        Log.d(TAG, "Application started");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static PetFinder getInstance(){
        return instance;
    }

    public Pedometer getPedometer() {
        return pedometer;
    }

    public GPS getGps() {
        return gps;
    }

    public BluetoothObject getBluetoothObject() {
        return bluetoothObject;
    }

    public void setBluetoothObject(BluetoothGatt bluetoothGatt,
                                   BluetoothGattCharacteristic characteristic,
                                   BluetoothGattCallbackHandler bluetoothgattCallbackHandler) {
        this.bluetoothObject.setBluetoothGatt(bluetoothGatt);
        this.bluetoothObject.setCharacteristic(characteristic);
        this.bluetoothObject.setHandlerInstance(bluetoothgattCallbackHandler);
        this.pedometer.setBluetoothObject(this.bluetoothObject);
    }

    public void deleteBluetoothObject(){
        this.bluetoothObject.nullify();
        this.pedometer.saveData();
        this.pedometer.setBluetoothObject(this.bluetoothObject);
    }

    public String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (!bluetoothObject.isNull()) {
            pedometer.saveData();
        }
    }

    private static class Pedometer implements BluetoothGattCallbackHandler.CharacteristicChangedCallback{
        BluetoothObject bluetoothObject;
        BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
        BluetoothGatt bluetoothGatt;
        BluetoothGattCharacteristic characteristic;
        Integer pedometerCount = 0;
        DatabaseHelper databaseHelper;
        String Date;
        String rawDate;

        public Pedometer(Context context, String Date) {
            databaseHelper = new DatabaseHelper(context);
            this.Date = Date;

        }

        public void setBluetoothObject(BluetoothObject bluetoothObject) {
            this.bluetoothObject = bluetoothObject;
            this.bluetoothGattCallbackHandler = bluetoothObject.getHandlerInstance();
            this.bluetoothGatt = bluetoothObject.getBluetoothGatt();
            this.characteristic = bluetoothObject.getCharacteristic();
            if (!bluetoothObject.isNull()) {
                this.bluetoothGattCallbackHandler.setCharacteristicChangedCallback(this);
                PedometerData rawData = databaseHelper.getLatestPedometer(bluetoothGatt.getDevice().getAddress());
                this.rawDate = rawData.getDate();
                if (Objects.equals(this.rawDate, this.Date)) pedometerCount = rawData.getPedometer();
            }
            if (bluetoothGatt != null && characteristic != null) {
                // Characteristic available, proceed with sending data
                this.characteristic.setValue("SEND_STEPS_COUNT");
                this.bluetoothGatt.writeCharacteristic(characteristic);
            }
        }

        public Integer getPedometerCount() {
            return pedometerCount;
        }

        @Override
        public void onCharacteristicChanged(String value) {
            if (value != null) {
                if (value.equals("+1")) pedometerCount += 1;
                else pedometerCount += Integer.parseInt(value);
            }
            Log.d("PEDOMETER", String.valueOf(pedometerCount));
        }

        public void saveData() {
            if (Objects.equals(this.rawDate, this.Date)) {
                //IF DATE EXISTS (SAME DAY), UPDATE THE DATA
                databaseHelper.updatePedometerData(
                        bluetoothGatt.getDevice().getAddress(),
                        pedometerCount,
                        this.rawDate
                );
            } else {
                //IF DATE EXISTS (NEW DAY), STORE THE DATA
                databaseHelper.storePedometerData(
                        bluetoothGatt.getDevice().getAddress(),
                        pedometerCount,
                        Date
                );
            }
        }
    }

    private static class GPS {
        private ScheduledExecutorService executorService;
        private Runnable logGPS;
        private Long delay = 60 * 1000L;
        Long Longitude;
        Long Latitude;

        public GPS() {
            startAsyncLoop();
            logGPS = new Runnable() {
                @Override
                public void run() {
                    //TODO: GET CURRENT LONGITUDE AND LATITUDE AND LOG IT TO DATABASE.

                }
            };
        }

        private void startAsyncLoop() {
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(logGPS, 0, this.delay, TimeUnit.MILLISECONDS);
        }
        public void cleanUp(){
            stopAsyncLoop();
        }
        private void stopAsyncLoop() {
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void setOutsideGeofence(boolean outsideGeofence) {
            if (outsideGeofence) this.delay = 15 * 1000L;
            else this.delay = 60 * 1000L;
        }
        public Long getLongitude() {
            return Longitude;
        }
        public Long getLatitude() {
            return Latitude;
        }
    }
}