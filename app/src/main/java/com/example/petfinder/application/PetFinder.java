package com.example.petfinder.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.bluetooth.BluetoothObject;
import com.example.petfinder.container.PetModel;
import com.example.petfinder.container.dataModel.PedometerData;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PetFinder extends Application{
    private static final String TAG = "PetFinder";
    public BluetoothObject bluetoothObject;
    private static PetFinder instance;
    public String currentMacAddress;
    public PetModel currentPetModel;
    private Pedometer pedometer;
    private GPS gps;
    private RepeatSend repeatSend;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        bluetoothObject = new BluetoothObject();
        repeatSend = new RepeatSend();
        currentPetModel = new PetModel();
        pedometer = new Pedometer(PetFinder.this, getCurrentDate());
        gps = new GPS(PetFinder.this, getCurrentDate());
        Log.d(TAG, "Application started");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static PetFinder getInstance(){
        return instance;
    }

    public RepeatSend getRepeatSend() { return repeatSend; }

    public Pedometer getPedometer() {
        return pedometer;
    }

    public GPS getGps() {
        return gps;
    }

    public BluetoothObject getBluetoothObject() {
        return bluetoothObject;
    }

    Boolean stop = false;

    public void setBluetoothObject(BluetoothGatt bluetoothGatt,
                                   BluetoothGattCharacteristic characteristic,
                                   BluetoothGattCallbackHandler bluetoothgattCallbackHandler) {
        this.bluetoothObject.setBluetoothGatt(bluetoothGatt);
        this.bluetoothObject.setCharacteristic(characteristic);
        this.bluetoothObject.setHandlerInstance(bluetoothgattCallbackHandler);

        this.pedometer.setBluetoothObject(this.bluetoothObject);
        this.gps.setBluetoothObject(this.bluetoothObject);
    }
    public void setBluetoothObject(BluetoothGatt bluetoothGatt,
                                   BluetoothGattCharacteristic characteristic,
                                   BluetoothGattCallbackHandler bluetoothgattCallbackHandler,
                                   Boolean setDataSenders){
        this.bluetoothObject.setBluetoothGatt(bluetoothGatt);
        this.bluetoothObject.setCharacteristic(characteristic);
        this.bluetoothObject.setHandlerInstance(bluetoothgattCallbackHandler);
        if (setDataSenders){
            this.pedometer.setBluetoothObject(this.bluetoothObject);
            this.gps.setBluetoothObject(this.bluetoothObject);
        }
    }

    public void deleteBluetoothObject(){
        if (!bluetoothObject.isNull()) {
            this.bluetoothObject.nullify();
            this.pedometer.saveData();
            this.pedometer.setBluetoothObject(this.bluetoothObject);
            this.repeatSend.emptyList();
            this.repeatSend.stopSending();
        }
    }

    public String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String formattedMonth = String.format("%02d", month);
        String formattedDay = String.format("%02d", day);

        return formattedMonth + "/" + formattedDay + "/" + year;
    }

    public String getCurrentMacAddress() {
        return currentMacAddress;
    }

    public void setCurrentMacAddress(String currentMacAddress) {
        this.currentMacAddress = currentMacAddress;
    }

    public void removeCurrentMacAddress(){
        this.currentMacAddress = null;
        currentPetModel.nullify();
    }

    public PetModel getCurrentPetModel() {
        return currentPetModel;
    }

    public void setCurrentPetModel(PetModel currentPetModel) {
        this.currentPetModel = currentPetModel;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (!bluetoothObject.isNull()) pedometer.saveData();
        repeatSend.stopSending();
        repeatSend.emptyList();
    }

    // INTERNAL CLASSES
    public static class Pedometer implements CharChangedListener.PedometerDataReceived {
        BluetoothObject bluetoothObject;
        BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
        BluetoothGatt bluetoothGatt;
        BluetoothGattCharacteristic characteristic;
        Integer pedometerCount = 0;
        DatabaseHelper databaseHelper;
        String Date;
        String rawDate;

        private RepeatSend repeatSend;

        public Pedometer(Context context, String Date) {
            databaseHelper = new DatabaseHelper(context);
            this.Date = Date;
            repeatSend = new RepeatSend();
            bluetoothObject = new BluetoothObject();
        }

        public void setBluetoothObject(BluetoothObject bluetoothObject) {
            this.bluetoothObject = bluetoothObject;
            this.bluetoothGattCallbackHandler = bluetoothObject.getHandlerInstance();
            this.bluetoothGatt = bluetoothObject.getBluetoothGatt();
            this.characteristic = bluetoothObject.getCharacteristic();
            if (!bluetoothObject.isNull()) {

                CharChangedListener charChangedListener = new CharChangedListener(bluetoothObject);
                charChangedListener.setPedometerDataReceived(this);

                PedometerData rawData = databaseHelper.getLatestPedometer(bluetoothGatt.getDevice().getAddress());
                this.rawDate = rawData.getDate();
                if (Objects.equals(this.rawDate, this.Date)) pedometerCount = rawData.getPedometer();
                if (bluetoothGatt != null && characteristic != null) {
                    // Characteristic available, proceed with sending data
                    repeatSend.addString("SEND_STEPS_COUNT").startSending();
                }
            }
        }

        public Integer getPedometerCount() {
            return pedometerCount;
        }

        private boolean isStringDigit(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }
            return str.matches("\\d+");
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

        @Override
        public void onPedometerDataReceived(String value) {
            if (value != null) {
                if (value.equals("+1")) pedometerCount += 1;
                else if(isStringDigit(value)) {
                    repeatSend.stopSending();
                    pedometerCount += Integer.parseInt(value);
                }
            }
            Log.d("PEDOMETER", String.valueOf(pedometerCount));
        }
    }
    public static class GPS implements CharChangedListener.GPSDataReceived {
        BluetoothObject bluetoothObject;
        BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
        BluetoothGatt bluetoothGatt;
        BluetoothGattCharacteristic characteristic;
        Double Longitude, Latitude;
        String LocationStatus;
        String date;
        DatabaseHelper databaseHelper;
        RepeatSend repeatSend;

        public GPS(Context context, String date) {
            this.date = date;
            databaseHelper = new DatabaseHelper(context);
            bluetoothObject = new BluetoothObject();
            repeatSend = PetFinder.getInstance().getRepeatSend();
        }

        public void setBluetoothObject(BluetoothObject bluetoothObject) {
            this.bluetoothObject = bluetoothObject;
            this.bluetoothGattCallbackHandler = bluetoothObject.getHandlerInstance();
            this.bluetoothGatt = bluetoothObject.getBluetoothGatt();
            this.characteristic = bluetoothObject.getCharacteristic();

            CharChangedListener charChangedListener = new CharChangedListener(bluetoothObject);
            charChangedListener.setGPSDataReceived(this);

            if (bluetoothGatt != null && characteristic != null) {
                //Characteristic available, proceed with sending data
                repeatSend.addString("START_SENDING_GPS").startSending();
                Log.d("GPS CODE", "START_SENDING_GPS");
            }
        }

        public boolean setOutsideGeofence(boolean outsideGeofence) {
            if (bluetoothObject.isNull()) return false;
            if (bluetoothGatt != null && characteristic != null) {
                String code;
                if (outsideGeofence) code = "GPS_STATE_ACTIVE";
                else code = "GPS_STATE_IDLE";
                repeatSend.addString(code).startSending();
                Log.d("GPS CODE", code);
                return true;
            } else return false;
        }
        public Double getLongitude() {
            return Longitude;
        }
        public Double getLatitude() {
            return Latitude;
        }
        public String getLocationStatus() {
            return LocationStatus;
        }

        private String extractValue(String part) {
            // Helper method to extract the value after the colon (:) in each parT
            String[] subParts = part.split(":");
            return subParts[1];
        }
        private String convertDateTime(String date, String time){
            // Specify the UTC time and format
            String utcTime = date+"T"+time;
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("d/M/yyyy'T'HH:mm:ss");

            // Parse the UTC time string to LocalDateTime
            LocalDateTime utcDateTime = LocalDateTime.parse(utcTime, utcFormatter);

            // Convert UTC time to Philippine time
            ZoneId utcZone = ZoneId.of("UTC");
            ZoneId philippineZone = ZoneId.of("Asia/Manila");
            ZonedDateTime utcZonedDateTime = ZonedDateTime.of(utcDateTime, utcZone);
            ZonedDateTime philippineZonedDateTime = utcZonedDateTime.withZoneSameInstant(philippineZone);

            // Format the Philippine time as a string
            DateTimeFormatter philippineFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy;HH:mm:ss");
            return philippineZonedDateTime.format(philippineFormatter);
        }

        @Override
        public void onGPSDataReceived(String value) {

            if (value.equals("GPS_TURNED_ACTIVE") ||
                   value.equals("GPS_TURNED_IDLE")||
                   value.equals("GPS_SEND_TRUE")) {
                switch (repeatSend.currentString()) {
                    case "GPS_STATE_ACTIVE":
                        if (Objects.equals(value, "GPS_TURNED_ACTIVE")) repeatSend.stopSending();
                        break;
                    case "GPS_STATE_IDLE":
                        if (Objects.equals(value, "GPS_TURNED_IDLE")) repeatSend.stopSending();
                        break;
                    case "START_SENDING_GPS":
                        if (Objects.equals(value, "GPS_SEND_TRUE")) repeatSend.stopSending();
                        break;
                }
                return;
            }
            //THIS WILL GET THE GPS DATA.
            if (value.equals("NOT_AVAILABLE")) {
                Latitude = null;
                Longitude = null;
                LocationStatus = "DATA_NOT_AVAILABLE";
            } else {
                String date, time;
                if (value.contains("DATE_UNKNOWN")) {
                    Calendar calendar_instance = Calendar.getInstance();
                    Date calendar_time = calendar_instance.getTime();
                    date = this.date;                                   // "M/D/YYYY"
                    time = calendar_time.getHours() + ":" +
                            calendar_time.getMinutes() + ":" +
                            calendar_time.getSeconds();                 // "HH:MM:SS"
                    String[] parts = value.split(";");
                    Latitude = Double.valueOf(extractValue(parts[1]));   // "##.######"
                    Longitude = Double.valueOf(extractValue(parts[2]));  // "##.######"
                } else {
                    String[] parts = value.split(";");

                    // Extract the individual data and store them in variables
                    date = parts[0];                                    // "M/D/YYYY"
                    time = parts[1];                                    // "HH:MM:SS"
                    Latitude = Double.valueOf(extractValue(parts[2]));    // "##.######"
                    Longitude = Double.valueOf(extractValue(parts[3]));   // "##.######"

                    parts = convertDateTime(date, time).split(";");
                    date = parts[0];
                    time = parts[1];
                }
                Log.d("GPS DATA", "Date: " + date + ", Time: " + time + ", Lat: " + Latitude + ", Lon: " + Longitude);

                //SAVE DATA TO DATABASE
                databaseHelper.storeGPSData(bluetoothGatt.getDevice().getAddress(),
                        String.valueOf(Latitude),
                        String.valueOf(Longitude),
                        time,
                        date);
            }
        }
    }
    public static class RepeatSend implements BluetoothGattCallbackHandler.CharacteristicWriteCallback{

        static List<String> value;
        static Boolean running = false, waiting = false, success;
        Integer sentMessage;

        private BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
        private BluetoothGatt bluetoothGatt;
        private BluetoothGattCharacteristic characteristic;
        private BluetoothObject bluetoothObject;
        private Thread thread;

        public RepeatSend() {
            value = new ArrayList<>();
        }

        public int startSending(){
            if (value.size() == 0) return -1;
            if (running) return -2;
            running = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    sendData();
                }
            };
            thread = new Thread(runnable);
            thread.start();
            return 1;
        }

        public void stopSending(){
            running = false;
            success = null;
            Log.d("DEBUGGER", "Value: "+value.toString());
            if (value.size() == 0) return;
            value.remove(0);
            if (value.size() > 0) startSending();
        }

        public void emptyList(){
            value.clear();
        }

        public String currentString(){
            return value.get(0);
        }

        public RepeatSend addString(String message){
            value.add(message);
            Log.d("DEBUGGER", "Value: "+value.toString());
            return this;
        }

        @Nullable
        public Boolean successStatus(){ return success; }

        @SuppressLint("MissingPermission")
        private void sendData(){
            bluetoothObject = PetFinder.getInstance().getBluetoothObject();
            bluetoothGattCallbackHandler = bluetoothObject.getHandlerInstance();
            bluetoothGatt = bluetoothObject.getBluetoothGatt();
            characteristic = bluetoothObject.getCharacteristic();
            bluetoothGattCallbackHandler.setCharacteristicWriteCallback(this);

            if (bluetoothGatt != null && characteristic != null) {
                String message = value.get(0);
                characteristic.setValue(message + "\n");
                Log.d("DEBUGGER", message);
                sentMessage = 0;
                while (running) {
                    Log.d("DEBUGGER", message+": Waiting...");
                    while (waiting);
                    Log.d("DEBUGGER", message+": Waiting done!");
                    success = bluetoothGatt.writeCharacteristic(characteristic);
                    if (success) {
                        Log.d("DEBUGGER", message+": sent!");
                        waiting = true;
                        try {
                            Thread.sleep(200); // Sleep for 200 milliseconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sentMessage ++;
                    if (!running) break;
                }
                waiting = false; //make sure waiting becomes false after successful send.
                Log.d("DEBUGGER", message+": Success!");
            }
        }

        @Override
        public void onCharacteristicWrite(Boolean writeOperationStatus) {
            while(!waiting); //wait until waiting becomes true
            waiting = false;
        }
    }
    private static class CharChangedListener implements BluetoothGattCallbackHandler.CharacteristicChangedCallback{

        BluetoothObject bluetoothObject;
        BluetoothGatt bluetoothGatt;
        BluetoothGattCharacteristic characteristic;
        BluetoothGattCallbackHandler bluetoothGattCallbackHandler;

        static PedometerDataReceived pedometerDataReceived;
        static GPSDataReceived gpsDataReceived;
        static OtherDataReceived otherDataReceived;

        public CharChangedListener(BluetoothObject bluetoothObject) {
            Log.d("CharChangedListener", "Instance created!");
            this.bluetoothObject = bluetoothObject;
            bluetoothGatt = bluetoothObject.getBluetoothGatt();
            characteristic = bluetoothObject.getCharacteristic();
            bluetoothGattCallbackHandler = bluetoothObject.getHandlerInstance();
            bluetoothGattCallbackHandler.setCharacteristicChangedCallback(this);
        }

        public interface PedometerDataReceived{
            void onPedometerDataReceived(String value);
        }

        public void setPedometerDataReceived(PedometerDataReceived callback){
            Log.d("CharChangedListener", "Pedometer Listener created!");
            pedometerDataReceived = callback;
        }

        public interface GPSDataReceived{
            void onGPSDataReceived(String value);
        }

        public void setGPSDataReceived (GPSDataReceived callback){
            Log.d("CharChangedListener", "GPS Listener created!");
            gpsDataReceived = callback;
        }

        public interface OtherDataReceived{
            void onOtherDataReceived(String value);
        }

        public void setOtherDataReceived (OtherDataReceived callback){
            Log.d("CharChangedListener", "Other Listener created!");
            otherDataReceived = callback;
        }

        @Override
        public void onCharacteristicChanged(String value) {
            Log.d("FROM_BLUETOOTH", value);
            if (Objects.equals(value, "+1") || isStringDigit(value)) {
                Log.d("DEBUGGER", value + ": Went inside pedometer.");
                pedometerDataReceived.onPedometerDataReceived(value);
            }
            else if (Objects.equals(value, "GPS_SEND_TRUE") ||
                     Objects.equals(value, "GPS_TURNED_IDLE") ||
                     Objects.equals(value, "GPS_TURNED_ACTIVE") ||
                     Objects.equals(value, "NOT_AVAILABLE") ||
                     value.startsWith("DATE_UNKNOWN")||
                     value.contains(";lat:")) {
                Log.d("DEBUGGER", value + ": Went inside gps.");
                gpsDataReceived.onGPSDataReceived(value);
            }
            else {
                Log.d("DEBUGGER", value+": Went inside others.");

                otherDataReceived.onOtherDataReceived(value);
            }
        }

        private boolean isStringDigit(String str) {
            if (str == null || str.isEmpty()) return false;
            return str.matches("\\d+");
        }
    }
}