package com.example.petfinder.application;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.container.dataModel;

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
    private dataModel.GPS currentGPS;
    private dataModel.PedometerData pedometerData;
    private DatabaseHelper databaseHelper = new DatabaseHelper(this);


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Application started");
        currentGPS = new dataModel.GPS();
        pedometerData = new dataModel.PedometerData(null, 0, null);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public String getMAC_ADDRESS() {
        return MAC_ADDRESS;
    }

    public void setMAC_ADDRESS(String MAC_ADDRESS) {
        this.MAC_ADDRESS = MAC_ADDRESS;
    }

    public dataModel.GPS getCurrentGPS() {
        return currentGPS;
    }

    public void setCurrentGPS(dataModel.GPS currentGPS) {
        this.currentGPS = currentGPS;
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
        //gpslon;[dateStr];[timeStr];[latStr]
        //gpslat;[dateStr];[timeStr];[lonStr]
        //ped;[dateStr];[timeStr];[stepsCount]

        String[] dataArray = value.split(";");

        String dataType = dataArray[0];
        String dateStr = dataArray[1];
        String timeStr = dataArray[2];
        String dataValue = dataArray[3].trim();

        if (dataType.equals("ped")){
            //pedometer data
            pedometerData = databaseHelper.getLatestPedometer(MAC_ADDRESS);
            if (pedometerData.getDate().equals(dateStr)) {
                int steps = pedometerData.getPedometer() + 1;
                pedometerData.setPedometer(steps);
                databaseHelper.updatePedometerData(MAC_ADDRESS, steps, dateStr);
            } else {
                databaseHelper.storePedometerData( MAC_ADDRESS,1, dateStr);
            }
        } else {
            //gps data
            if (currentGPS.isComplete()){
                //insert to sql
                databaseHelper.storeGPSData(currentGPS.getMac_address(),
                                            currentGPS.getLatitude(),
                                            currentGPS.getLongitude(),
                                            currentGPS.getTime(),
                                            currentGPS.getDate());
                currentGPS = new dataModel.GPS();
            }
            if (dataType.equals("gpslon")){
                currentGPS.setLongitude(dataValue);
                currentGPS.setDate(dateStr);
                currentGPS.setTime(timeStr);
                currentGPS.setMac_address(MAC_ADDRESS);
            } else {
                currentGPS.setLatitude(dataValue);
            }
        }
    }
}
