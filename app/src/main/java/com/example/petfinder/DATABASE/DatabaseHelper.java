package com.example.petfinder.DATABASE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.petfinder.container.DeviceModel;
import com.example.petfinder.container.PetModel;
import com.example.petfinder.container.RecordModel;
import com.example.petfinder.container.dataModel.PedometerData;

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
        db.execSQL(Constants.query5);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME4);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME5);
        onCreate(db);
    }
    public long storeData(String btAddress, String petName, String breed, String sex, String bdate, Integer age,
                          Integer weight, String petPic, String addedtime, String updatedtime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, btAddress); // Use Bluetooth address as ID
        values.put(Constants.COLUMN_PETNAME, petName);
        values.put(Constants.COLUMN_BREED, breed);
        values.put(Constants.COLUMN_SEX, sex);
        values.put(Constants.COLUMN_BIRTHDATE, bdate);
        values.put(Constants.COLUMN_AGE, age);
        values.put(Constants.COLUMN_WEIGHT, weight);
        values.put(Constants.COLUMN_IMAGE, petPic);
        values.put(Constants.COLUMN_ADDED_TIMESTAMP, addedtime);
        values.put(Constants.COLUMN_UPDATED_TIMESTAMP, updatedtime);

        long id = db.insert(Constants.TABLE_NAME, null, values);
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
        values.put(Constants.COLUMN_ID, address);
        values.put(Constants.COLUMN_LONG, longi);
        values.put(Constants.COLUMN_LAT, lat);
        values.put(Constants.COLUMN_TIME, time);
        values.put(Constants.COLUMN_DATE2, date);

        long id = db.insert(Constants.TABLE_NAME4, null, values);  // Corrected table name usage
        db.close();
        return id;
    }

    public long storePedometerData(String MAC_ADDRESS, int numSteps, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, MAC_ADDRESS);
        values.put(Constants.COLUMN_NUMSTEPS, numSteps);
        values.put(Constants.COLUMN_DATE, date);

        long id = db.insert(Constants.TABLE_NAME3, null, values);  // Corrected table name usage
        db.close();
        return id;
    }

    public int updateData(String id, String petName, String breed, String sex, String bdate, Integer age,
                           Integer weight, String petPic, String addedtime, String updatedtime, String petFeederID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PETNAME, petName);
        values.put(Constants.COLUMN_BREED, breed);
        values.put(Constants.COLUMN_SEX, sex);
        values.put(Constants.COLUMN_BIRTHDATE, bdate);
        values.put(Constants.COLUMN_AGE, age);
        values.put(Constants.COLUMN_WEIGHT, weight);
        values.put(Constants.COLUMN_IMAGE, petPic);
        values.put(Constants.COLUMN_ADDED_TIMESTAMP, addedtime);
        values.put(Constants.COLUMN_UPDATED_TIMESTAMP, updatedtime);
        values.put(Constants.COLUMN_PET_FEEDER_ID, petFeederID);

        int returnValue =
                db.update(Constants.TABLE_NAME, values, Constants.COLUMN_ID +" = ?", new String[] {id});
        db.close();

        //TODO: ALSO UPDATE IN PET FEEDER IF NECESSARY.

        return returnValue;
    }

    public void updatePedometerData(String id, int numstep, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, id);
        values.put(Constants.COLUMN_NUMSTEPS, numstep);
        values.put(Constants.COLUMN_DATE, date);

        db.update(Constants.TABLE_NAME3, values, Constants.COLUMN_DATE +" = ? AND "+ Constants.COLUMN_ID +"= ?", new String[] {date, id});
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

    public Cursor getAllPets () {
        //USED FOR CONTENT PROVIDER.
        String table1 = Constants.TABLE_NAME;
        String table2 = Constants.TABLE_NAME5;
        String ID = Constants.COLUMN_ID;

        String selectQuery = "SELECT * FROM " + table1 + " INNER JOIN " + table2 +
                " ON " + table1+"."+ID + " = " + table2+"."+ID +
                " UNION " +
                "SELECT * FROM " + table2 + " LEFT OUTER JOIN " + table1 +
                " ON " + table1+"."+ID + " = " + table2+"."+ID;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        db.close();
        return cursor;
    }

    @SuppressLint("Range")
    public PedometerData getLatestPedometer(String MAC_ADDRESS) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME3,
                null,
                Constants.COLUMN_ID + "= ?",
                new String[]{MAC_ADDRESS},
                null,
                null,
                Constants.COLUMN_DATE+" DESC",
                "1"
        );

        PedometerData latestPedometer = new PedometerData(null, 0, null);
        if (cursor.moveToFirst()) {
            latestPedometer = new PedometerData(
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_NUMSTEPS)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATE))
            );
        }
        cursor.close();
        db.close();
        return latestPedometer;
    }

    public Cursor getAllSteps() {
        //USED FOR CONTENT PROVIDER.
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME3,
                null,
                null,
                null,
                null,
                null,
                Constants.COLUMN_DATE+" DESC",
                null
        );
        cursor.close();
        db.close();
        return cursor;
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

        //TODO: ALSO DELETE IN PET FEEDER IF NECESSARY.
    }
    public void deleteAllData(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
        db.close();
        //TODO: ALSO DELETE IN PET FEEDER IF NECESSARY.
    }

    public int deleteData(String whereClause, String[] whereValues) {
        //USED FOR CONTENT PROVIDER.
        SQLiteDatabase db = getWritableDatabase();
        int feedback = db.delete(Constants.TABLE_NAME, whereClause, whereValues);
        db.close();
        return feedback;
    }

    public int getRecordsCount() {
        String countQuery = "SELECT * FROM " + Constants.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }
    public ArrayList<String> getAllBTAddresses() {
        ArrayList<String> btAddresses = new ArrayList<>();
        String selectQuery = "SELECT " + Constants.COLUMN_ID + " FROM " + Constants.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String btAddress = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));
                btAddresses.add(btAddress);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return btAddresses;
    }

    @SuppressLint("Range")
    public PetModel getRecordDetails(String recordID) {
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.COLUMN_ID + "=\"" + recordID + "\"";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        PetModel petModel = new PetModel();

        if (cursor.moveToFirst()) {
            do {
                petModel.setMAC_ADDRESS(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID)));
                petModel.setName(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PETNAME)));
                petModel.setBreed(
                        "" +cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BREED)));
                petModel.setSex(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SEX)));
                petModel.setAge(
                        cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_AGE)));
                petModel.setWeight(
                        cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_WEIGHT)));
                petModel.setBirthdate(
                        cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BIRTHDATE)));
                petModel.setImage(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_IMAGE)));
                petModel.setPetFeederID(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PET_FEEDER_ID)));
            } while (cursor.moveToNext());
        }
        db.close();

        return petModel;
    }

    public Cursor getPetWhere(String whereClause, String[] whereValues){
        //USED FOR CONTENT RESOLVER.
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + whereClause;
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(selectQuery, whereValues);
    }
}
