package com.example.petfinder.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.DataSharing.PetProviderConstants;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.bluetooth.BluetoothObject;
import com.example.petfinder.container.PetModel;
import com.example.petfinder.container.RecordModel;
import com.example.petfinder.container.dataModel.GeofenceData;
import com.example.petfinder.container.dataModel.PedometerData;
import com.example.petfinder.container.dataModel.MapPreferences;
import com.google.android.gms.maps.model.LatLng;

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

public class PetFinder extends Application implements Application.ActivityLifecycleCallbacks{

    private static final String TAG = "PetFinder";
    public BluetoothObject bluetoothObject;
    private static PetFinder instance;
    public String currentMacAddress;
    public PetModel currentPetModel;
    private Pedometer pedometer;
    private GeofenceData geofenceData;
    private MapPreferences mapPreferences;
    private GPS gps;
    private RepeatSend repeatSend;

    ContentResolver contentResolver;
    ContentObserver contentObserver;
    DatabaseHelper databaseHelper;

    Boolean contentProviderExists = false;
    Boolean previousContentProviderExists = false;

    private int drawerNavID = 0;

    ArrayList<RecordModel> unlistedPets;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        bluetoothObject = new BluetoothObject();
        repeatSend = new RepeatSend();
        currentPetModel = new PetModel();
        pedometer = new Pedometer(PetFinder.this, getCurrentDate());
        gps = new GPS(PetFinder.this, getCurrentDate());
        databaseHelper = new DatabaseHelper(this);
        unlistedPets = new ArrayList<>();
        geofenceData = new GeofenceData();
        mapPreferences = new MapPreferences();

        contentResolver = getContentResolver();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        isContentUriExists();
        contentInitialize();

        registerActivityLifecycleCallbacks(this);
    }

    private void isContentUriExists() {
        Uri uri_pets = PetProviderConstants.CONTENT_URI_PETS;
        Uri uri_step = PetProviderConstants.CONTENT_URI_STEP;
        Cursor cursorPets = null;
        String cursorStep;

        try {
            cursorPets = contentResolver.query(uri_pets, null, null, null, null);
            cursorStep = contentResolver.getType(uri_step);
            contentProviderExists = cursorPets != null && cursorStep != null;
        } catch (Exception e) {
            e.printStackTrace();
            contentProviderExists = false;
        } finally {
            if (cursorPets != null) {
                cursorPets.close();
            }
        }

        //IF CONTENT PROVIDER DOES NOT EXIST PREVIOUSLY, AND NOW EXISTS, INITIALIZE CONTENT
        if (contentProviderExists != previousContentProviderExists && contentProviderExists)
            contentInitialize();
    }

    private void contentInitialize(){
        if (contentProviderExists) {

            previousContentProviderExists = contentProviderExists;
            //IF PET FEEDER IS INSTALLED
            contentResolver = getContentResolver();
            contentObserver = new ContentObserver(new Handler()) {
                //THIS OBSERVER IS FOR DETECTING CHANGES TO PET FEEDER'S DATABASE.
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    setUnlistedPets();
                }
            };
            //REGISTER THE OBSERVER
            contentResolver.registerContentObserver(PetProviderConstants.CONTENT_URI_PETS, true, contentObserver);

            //DETECT INITIAL DATA
            setUnlistedPets();
        }
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

    public int getDrawerNavID() {
        return drawerNavID;
    }
    public GeofenceData getGeofenceData() {
        if (geofenceData.getLatLng() == null){ geofenceData = databaseHelper.getGeofence(currentMacAddress); }
        //NOTE:
        // If geofenceData's latLng is still null after the previous line, that means no geofence
        // data has been stored yet. In this case, a null value will be returned.
        return geofenceData;
    }
    public void updateGeofenceData(){ geofenceData = databaseHelper.getGeofence(currentMacAddress); }

    public MapPreferences getMapPreferences() {
        if (mapPreferences.getMapStyle() == null){ mapPreferences = databaseHelper.getMapPreferences(currentMacAddress); }
        //NOTE:
        // If geofenceData's latLng is still null after the previous line, that means no geofence
        // data has been stored yet. In this case, a null value will be returned.
        return mapPreferences;
    }
    public void updateMapPreferences(){ mapPreferences = databaseHelper.getMapPreferences(currentMacAddress); }

    //DATA SETTERS
    public void setDrawerNavID(int drawerNavID) {
        this.drawerNavID = drawerNavID;
    }

    @SuppressLint("Range")
    private void setUnlistedPets(){
        if (!contentProviderExists) return;
        //GET ALL PETS WITHOUT COLLAR MAC ADDRESS.
        Cursor cursor = contentResolver.query(PetProviderConstants.CONTENT_URI_PETS,
                null,
                Constants.COLUMN_PET_FINDER_ID + " IS NULL",
                null,
                null,
                null);
        unlistedPets.clear();
        if (cursor!=null && cursor.getCount()>0){
            while (cursor.moveToNext()){
                //Reminder: Pets without a collar mac address will not be stored in the database.
                // Although, it will still be displayed.
                RecordModel recordModel = new RecordModel(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PET_FINDER_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PETNAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BREED)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SEX)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BIRTHDATE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_AGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_WEIGHT)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_UPDATED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID)));
                unlistedPets.add(recordModel);
            }
            cursor.close();
        }
    }

    public ContentResolver getCResolver() {
        return contentResolver;
    }
    public Boolean getContentProviderExists() {
        return contentProviderExists;
    }

    public ArrayList<RecordModel> getUnlistedPets() {
        return unlistedPets;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (!bluetoothObject.isNull()) pedometer.saveData();
        repeatSend.stopSending();
        repeatSend.emptyList();
    }
    @Override
    public void onActivityCreated(@NonNull Activity activity, @androidx.annotation.Nullable Bundle bundle) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        setUnlistedPets();
        isContentUriExists();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}

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

        //INTERFACES & CALLBACKS

        GPSChangeCallback gpsChangeCallback;

        public interface GPSChangeCallback{
            void onGPSChange(LatLng latLng);
            void onGPSNoData();
        }

        public void setGPSChangeCallback(GPSChangeCallback gpsChangeCallback){
            this.gpsChangeCallback = gpsChangeCallback;
        }


        //ACTUAL CODE
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
                if (gpsChangeCallback!=null) gpsChangeCallback.onGPSNoData();
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

                LocationStatus = "DATA_AVAILABLE";

                //TRIGGER CALLBACK
                if (gpsChangeCallback!=null) gpsChangeCallback.onGPSChange(new LatLng(Latitude, Longitude));

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