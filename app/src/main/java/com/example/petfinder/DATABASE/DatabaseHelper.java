package com.example.petfinder.DATABASE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.petfinder.container.DeviceModel;
import com.example.petfinder.container.GPSModel;
import com.example.petfinder.container.RecordModel;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public Context context;
    public DatabaseHelper(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.query);
        db.execSQL(Constants.query2);
        db.execSQL(Constants.query3);
        db.execSQL(Constants.query4);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME4);
        onCreate(db);
    }
    public long storeData(String address, String petName, String breed, String sex, String age,
                          String weight, String petPic, String addedtime, String updatedtime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, address);
        values.put(Constants.COLUMN_PETNAME, petName);
        values.put(Constants.COLUMN_BREED, breed);
        values.put(Constants.COLUMN_SEX, sex);
        values.put(Constants.COLUMN_AGE, age);
        values.put(Constants.COLUMN_WEIGHT, weight);
        values.put(Constants.COLUMN_IMAGE, petPic);
        values.put(Constants.COLUMN_ADDED_TIMESTAMP, addedtime);
        values.put(Constants.COLUMN_UPDATED_TIMESTAMP, updatedtime);

        long id = db.insert(Constants.TABLE_NAME, null, values);  // Corrected table name usage
        db.close();
        return id;
    }

    public long storeDeviceData(String deviceName, String latitude, String longitude, String btName, String btAddress){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DEVICENAME, deviceName);
        values.put(Constants.COLUMN_LATITUDE, latitude);
        values.put(Constants.COLUMN_LONGITUDE, longitude);
        values.put(Constants.COLUMN_BTNAME, btName);
        values.put(Constants.COLUMN_BTADDRESS, btAddress);

        long id = db.insert(Constants.TABLE_NAME2, null, values);  // Corrected table name usage
        db.close();
        return id;
    }

    public long storeGPSData(String address, String lat, String longi, String time, String date){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID4, address);
        values.put(Constants.COLUMN_LONG, longi);
        values.put(Constants.COLUMN_LAT, lat);
        values.put(Constants.COLUMN_TIME, time);
        values.put(Constants.COLUMN_DATE2, date);

        long id = db.insert(Constants.TABLE_NAME4, null, values);  // Corrected table name usage
        db.close();
        return id;
    }

    public long storePedometerData(String numSteps, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_NUMSTEPS, numSteps);
        values.put(Constants.COLUMN_DATE, date);

        long id = db.insert(Constants.TABLE_NAME3, null, values);  // Corrected table name usage
        db.close();
        return id;
    }

    public void updateData(String id, String petName, String breed, String sex, String age,
                          String weight, String petPic, String addedtime, String updatedtime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PETNAME, petName);
        values.put(Constants.COLUMN_BREED, breed);
        values.put(Constants.COLUMN_SEX, sex);
        values.put(Constants.COLUMN_AGE, age);
        values.put(Constants.COLUMN_WEIGHT, weight);
        values.put(Constants.COLUMN_IMAGE, petPic);
        values.put(Constants.COLUMN_ADDED_TIMESTAMP, addedtime);
        values.put(Constants.COLUMN_UPDATED_TIMESTAMP, updatedtime);

        db.update(Constants.TABLE_NAME, values, Constants.COLUMN_ID +" = ?", new String[] {id});
        db.close();
    }

    public void updatePedometerData(String id, String numstep, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_NUMSTEPS, numstep);
        values.put(Constants.COLUMN_DATE, date);


        db.update(Constants.TABLE_NAME3, values, Constants.COLUMN_ID +" = ?", new String[] {id});
        db.close();
    }

    public ArrayList<RecordModel> getAllRecords (String orderby) {
        ArrayList<RecordModel> recordsList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " ORDER BY " + orderby;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") RecordModel recordModel = new RecordModel(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PETNAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BREED)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SEX)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_AGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_WEIGHT)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_UPDATED_TIMESTAMP)));

                recordsList.add(recordModel);
            } while (cursor.moveToNext());
        }
        db.close();
        return recordsList;
    }

    @SuppressLint("Range")
    public GPSModel getLatestGPS(String orderby) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME4,
                null,
                null,
                null,
                null,
                null,
                orderby + " DESC",
                "1"
        );

        GPSModel latestGPS = null;
        if (cursor.moveToFirst()) {
            latestGPS = new GPSModel(
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID4)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LONG)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAT)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIME)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATE2))
            );
        }
        cursor.close();
        db.close();
        return latestGPS;
    }

    public ArrayList<DeviceModel> getAllDeviceRecords (String orderBy){
        ArrayList<DeviceModel> deviceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME2 + " ORDER BY " + orderBy;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") DeviceModel deviceModel = new DeviceModel(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID2)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEVICENAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LATITUDE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LONGITUDE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BTNAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BTADDRESS)));

                deviceList.add(deviceModel);
            } while (cursor.moveToNext());
        }
        db.close();
        return deviceList;
    }
    public ArrayList<DeviceModel> searchDeviceRecords (String query) {
        ArrayList<DeviceModel> deviceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME2 + " WHERE " + Constants.COLUMN_DEVICENAME + " LIKE '%" + query +"%'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") DeviceModel deviceModel = new DeviceModel(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID2)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEVICENAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LATITUDE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LONGITUDE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BTNAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BTADDRESS)));

                deviceList.add(deviceModel);
            } while (cursor.moveToNext());
        }
        db.close();
        return deviceList;
    }

    public ArrayList<RecordModel> searchRecords (String query) {
        ArrayList<RecordModel> recordsList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.COLUMN_PETNAME + " LIKE '%" + query +"%'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") RecordModel recordModel = new RecordModel(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PETNAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BREED)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SEX)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_AGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_WEIGHT)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ADDED_TIMESTAMP)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_UPDATED_TIMESTAMP)));

                recordsList.add(recordModel);
            } while (cursor.moveToNext());
        }
        db.close();
        return recordsList;
    }

    public void deleteData(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.close();
    }
    public void deleteAllData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
        db.close();
    }

    public int getRecordsCount() {
        String countQuery = "SELECT * FROM " + Constants.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

}
