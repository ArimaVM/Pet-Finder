package com.example.petfinder.DATABASE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.petfinder.DataSharing.PetFinderContentProvider;
import com.example.petfinder.DataSharing.PetProviderConstants;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.container.PetModel;
import com.example.petfinder.container.RecordModel;
import com.example.petfinder.container.dataModel.GPSData;
import com.example.petfinder.container.dataModel.GeofenceData;
import com.example.petfinder.container.dataModel.MapPreferences;
import com.example.petfinder.container.dataModel.PedometerData;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

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
        db.execSQL(Constants.query6);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME4);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME5);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME6);
        onCreate(db);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder, Context context) {
        // Implement the query operation for the Content Provider
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;

        cursor = db.query(Constants.TABLE_NAME + " LEFT JOIN " + Constants.TABLE_NAME5,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);


        // Set the notification URI on the cursor
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return cursor;
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

        long id = db.insertWithOnConflict(Constants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }
    public long storeData(String btAddress, String petName, String breed, String sex, String bdate, Integer age,
                          Integer weight, String petPic, String addedtime, String updatedtime, String PetFeederId) {
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
        values.put(Constants.COLUMN_PET_FEEDER_ID, PetFeederId);

        long id = db.insertWithOnConflict(Constants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();

        //CALLING THIS FUNCTION MEANS THAT WHAT WAS STORED HAS A PET FEEDER ID. UPDATE THE PET FEEDER PET INFO.
        if (PetFinder.getInstance().getContentProviderExists()) {
            values.remove(Constants.COLUMN_ID);
            values.put(Constants.COLUMN_ID, PetFeederId);
            values.remove(Constants.COLUMN_PET_FEEDER_ID);
            values.put(Constants.COLUMN_PET_FINDER_ID, btAddress);
            PetFinder.getInstance().getCResolver().update(PetProviderConstants.CONTENT_URI_PETS, values, null, null);
        }
        return id;
    }

    public long updateHealthInfo(String btAddress, String Allergies, String Medications, String VetName, String VetContact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, btAddress); // Use Bluetooth address as ID
        values.put(Constants.COLUMN_ALLERGIES, Allergies);
        values.put(Constants.COLUMN_MEDICATIONS, Medications);
        values.put(Constants.COLUMN_VETNAME, VetName);
        values.put(Constants.COLUMN_VETCONTACT, VetContact);

        long returnValue =
                db.update(Constants.TABLE_NAME5, values, Constants.COLUMN_ID +" = ?", new String[] {btAddress});

        if (returnValue==0){
            //if update affected no lines, it means pet has no health information stored yet.
            returnValue = db.insertWithOnConflict(Constants.TABLE_NAME5, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }

        db.close();
        return returnValue;
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

    @SuppressLint("Range")
    public GPSData getLatestGPS(String MAC_ADDRESS){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME4,
                null,
                Constants.COLUMN_ID + "= ?",
                new String[]{MAC_ADDRESS},
                null,
                null,
                Constants.COLUMN_DATE + " DESC, " + Constants.COLUMN_TIME + " DESC",
                "1"
        );

        GPSData latestGPS = new GPSData();
        if (cursor.moveToFirst()) {
            latestGPS = new GPSData(
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID)),
                    cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_LONG)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATE2)),
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIME))
            );
        }
        cursor.close();
        db.close();
        return latestGPS;
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
                           Integer weight, String petPic, String updatedtime, String petFeederID, Boolean updateFeeder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PETNAME, petName);
        values.put(Constants.COLUMN_BREED, breed);
        values.put(Constants.COLUMN_SEX, sex);
        values.put(Constants.COLUMN_BIRTHDATE, bdate);
        values.put(Constants.COLUMN_AGE, age);
        values.put(Constants.COLUMN_WEIGHT, weight);
        values.put(Constants.COLUMN_IMAGE, petPic);
        values.put(Constants.COLUMN_UPDATED_TIMESTAMP, updatedtime);
        values.put(Constants.COLUMN_PET_FEEDER_ID, petFeederID);

        int returnValue =
                db.update(Constants.TABLE_NAME, values, Constants.COLUMN_ID +" = ?", new String[] {id});

        //UPDATE TO PET FEEDER IF THE FOLLOWING CONDITIONS ARE MET:
        // - IF PET IS IMPORTED TO THE PET FEEDER.
        // - IF PET FEEDER IS INSTALLED IN THE SYSTEM.
        if (updateFeeder) {
            PetFinder petFinder = PetFinder.getInstance();
            if (petFinder.getContentProviderExists()) {
                if (petFeederID != null) {
                    values.put(Constants.COLUMN_ID, petFeederID);
                    values.remove(Constants.COLUMN_PET_FEEDER_ID);
                    values.put(Constants.COLUMN_PET_FINDER_ID, id);
                    petFinder.getCResolver().update(PetFinderContentProvider.CONTENT_URI_PETS, values, null, null);
                }
            }
        }

        db.close();
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
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BIRTHDATE)),
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

        String selectQuery = "SELECT * FROM " + table1 + " LEFT JOIN " + table2 +
                " ON " + table1 + "." + ID + " = " + table2 + "." + ID;

        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(selectQuery, null);
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
        return db.query(
                Constants.TABLE_NAME3,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
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
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BIRTHDATE)),
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

    public void deleteData(String id, Boolean deleteInBothApps) {
        if (PetFinder.getInstance().getContentProviderExists()) {
            String petId = getPetFeederIDFromId(id);
            if (petId!=null) {
                String whereClause = Constants.COLUMN_ID + " = ? ";
                if (deleteInBothApps) {
                    PetFinder.getInstance().getCResolver().delete(PetProviderConstants.CONTENT_URI_PETS,
                            whereClause,
                            new String[]{petId});
                } else {
                    PetModel petModel = getRecordDetails(id);
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_ID, petId); // Use Bluetooth address as ID
                    values.put(Constants.COLUMN_PETNAME, petModel.getName());
                    values.put(Constants.COLUMN_BREED, petModel.getBreed());
                    values.put(Constants.COLUMN_SEX, petModel.getSex());
                    values.put(Constants.COLUMN_BIRTHDATE, petModel.getBirthdate());
                    values.put(Constants.COLUMN_AGE, petModel.getAge());
                    values.put(Constants.COLUMN_WEIGHT, petModel.getWeight());
                    values.put(Constants.COLUMN_IMAGE, petModel.getImage());
                    values.put(Constants.COLUMN_ADDED_TIMESTAMP, petModel.getAdded_timestamp());
                    values.put(Constants.COLUMN_UPDATED_TIMESTAMP, System.currentTimeMillis());
                    values.putNull(Constants.COLUMN_PET_FINDER_ID);

                    PetFinder.getInstance().getCResolver().update(PetProviderConstants.CONTENT_URI_PETS,
                            values,
                            whereClause,
                            new String[]{petId});
                }
            }
        }
        SQLiteDatabase db = getWritableDatabase();
        //DELETE TO ALL TABLES WHERE DATA IS ABOUT PET.
        db.delete(Constants.TABLE_NAME, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.delete(Constants.TABLE_NAME2, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.delete(Constants.TABLE_NAME3, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.delete(Constants.TABLE_NAME4, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.delete(Constants.TABLE_NAME5, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.delete(Constants.TABLE_NAME6, Constants.COLUMN_ID + " = ?", new String[]{id});
        db.close();
    }
    @SuppressLint("Range")
    public void deleteAllData(Boolean deleteInBothApps){
        if (PetFinder.getInstance().getContentProviderExists()) {
            if (deleteInBothApps){
                //get all pet IDs
                Cursor cursor = getAllPets();
                ArrayList<String> petIds = new ArrayList<>();
                while (cursor.moveToNext()){
                    String id = getPetFeederIDFromId(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID)));
                    if (id!=null) petIds.add(id);
                }
                if (petIds.size()!=0) {
                    String whereClause = Constants.COLUMN_ID + " IN (" + TextUtils.join(",",
                            Collections.nCopies(petIds.size(), "?")) + ")";
                    PetFinder.getInstance().getCResolver().delete(PetProviderConstants.CONTENT_URI_PETS,
                            whereClause,
                            petIds.toArray(new String[petIds.size()]));
                }
            } else {
                Cursor cursor = getAllPets();
                while (cursor.moveToNext()){
                    String petFinderID = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));
                    String petFeederID = getPetFeederIDFromId(petFinderID);
                    if (petFeederID!=null){
                        PetModel petModel = getRecordDetails(petFinderID);
                        ContentValues values = new ContentValues();
                        values.put(Constants.COLUMN_ID, petFeederID); // Use Bluetooth address as ID
                        values.put(Constants.COLUMN_PETNAME, petModel.getName());
                        values.put(Constants.COLUMN_BREED, petModel.getBreed());
                        values.put(Constants.COLUMN_SEX, petModel.getSex());
                        values.put(Constants.COLUMN_BIRTHDATE, petModel.getBirthdate());
                        values.put(Constants.COLUMN_AGE, petModel.getAge());
                        values.put(Constants.COLUMN_WEIGHT, petModel.getWeight());
                        values.put(Constants.COLUMN_IMAGE, petModel.getImage());
                        values.put(Constants.COLUMN_ADDED_TIMESTAMP, petModel.getAdded_timestamp());
                        values.put(Constants.COLUMN_UPDATED_TIMESTAMP, System.currentTimeMillis());
                        values.putNull(Constants.COLUMN_PET_FINDER_ID);
                        PetFinder.getInstance().getCResolver().update(PetProviderConstants.CONTENT_URI_PETS,
                                values,
                                Constants.COLUMN_ID + " = ? ",
                                new String[]{petFeederID});
                    };
                }
            }
        }

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME2);
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME3);
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME4);
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME5);
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME6);
        db.close();
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
                petModel.setAdded_timestamp(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ADDED_TIMESTAMP)));
                petModel.setUpdated_timestamp(
                        ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_UPDATED_TIMESTAMP)));
            } while (cursor.moveToNext());
        }

        selectQuery = "SELECT * FROM " + Constants.TABLE_NAME5 + " WHERE " + Constants.COLUMN_ID + "=\"" + recordID + "\"";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount()>0) {
            if (cursor.moveToFirst()) {
                petModel.setAllergies(
                        "" + cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ALLERGIES)));
                petModel.setTreats(
                        "" + cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TREATS)));
                petModel.setMedications(
                        "" + cursor.getString(cursor.getColumnIndex(Constants.COLUMN_MEDICATIONS)));
                petModel.setVetName(
                        "" + cursor.getString(cursor.getColumnIndex(Constants.COLUMN_VETNAME)));
                petModel.setVetContact(
                        cursor.getString(cursor.getColumnIndex(Constants.COLUMN_VETCONTACT)));
            }
        }
        db.close();
        return petModel;
    }

    public long storeGeofence(String MAC_ADDRESS, LatLng latLng, Integer radius){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, MAC_ADDRESS);
        values.put(Constants.COLUMN_LATITUDE, latLng.latitude);
        values.put(Constants.COLUMN_LONGITUDE, latLng.longitude);
        values.put(Constants.COLUMN_RADIUS, radius);

        long id = db.insert(Constants.TABLE_NAME2, null, values);  // Corrected table name usage
        db.close();
        return id;
    }
    public void updateGeofence(String MAC_ADDRESS, LatLng latLng, Integer radius) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, MAC_ADDRESS);
        values.put(Constants.COLUMN_LATITUDE, latLng.latitude);
        values.put(Constants.COLUMN_LONGITUDE, latLng.longitude);
        values.put(Constants.COLUMN_RADIUS, radius);

        db.update(Constants.TABLE_NAME2, values, Constants.COLUMN_ID +"= ?", new String[] {MAC_ADDRESS});
        db.close();
    }
    @SuppressLint("Range")
    public GeofenceData getGeofence(String MAC_ADDRESS) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME2,
                null,
                Constants.COLUMN_ID + "= ?",
                new String[]{MAC_ADDRESS},
                null,
                null,
                null,
                "1"
        );

        GeofenceData geofenceData = new GeofenceData();
        if (cursor.moveToFirst())
            geofenceData = new GeofenceData(
                    new LatLng(cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_LONGITUDE))),
                    cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_RADIUS)));

        cursor.close();
        db.close();
        return geofenceData;
    }

    public long storeMapPreferences(String MAC_ADDRESS, Integer mapStyle,
                                    Integer geofenceIcon, String geofenceColor, Integer geofenceSize,
                                    Integer petIcon, String petColor, Integer petSize){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, MAC_ADDRESS);
        values.put(Constants.MAP_STYLE, mapStyle);
        values.put(Constants.MAP_GEO_ICON, geofenceIcon);
        values.put(Constants.MAP_GEO_COLOR, geofenceColor);
        values.put(Constants.MAP_PET_ICON, petIcon);
        values.put(Constants.MAP_PET_COLOR, petColor);
        values.put(Constants.MAP_GEO_SIZE, geofenceSize);
        values.put(Constants.MAP_PET_SIZE, petSize);

        long id = db.insert(Constants.TABLE_NAME6, null, values);  // Corrected table name usage
        db.close();
        return id;
    }
    public void updateMapPreferences(String MAC_ADDRESS, Integer mapStyle,
                                     Integer geofenceIcon, String geofenceColor, Integer geofenceSize,
                                     Integer petIcon, String petColor, Integer petSize) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ID, MAC_ADDRESS);
        values.put(Constants.MAP_STYLE, mapStyle);
        values.put(Constants.MAP_GEO_ICON, geofenceIcon);
        values.put(Constants.MAP_GEO_COLOR, geofenceColor);
        values.put(Constants.MAP_PET_ICON, petIcon);
        values.put(Constants.MAP_PET_COLOR, petColor);
        values.put(Constants.MAP_GEO_SIZE, geofenceSize);
        values.put(Constants.MAP_PET_SIZE, petSize);

        db.update(Constants.TABLE_NAME6, values, Constants.COLUMN_ID +"= ?", new String[] {MAC_ADDRESS});
        db.close();
    }
    @SuppressLint("Range")
    public MapPreferences getMapPreferences(String MAC_ADDRESS) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME6,
                null,
                Constants.COLUMN_ID + "= ?",
                new String[]{MAC_ADDRESS},
                null,
                null,
                null,
                "1"
        );

        MapPreferences mapPreferences = new MapPreferences();
        if (cursor.moveToFirst())
            mapPreferences = new MapPreferences(
                    cursor.getInt(cursor.getColumnIndex(Constants.MAP_STYLE)),
                    cursor.getInt(cursor.getColumnIndex(Constants.MAP_GEO_ICON)),
                    cursor.getInt(cursor.getColumnIndex(Constants.MAP_PET_ICON)),
                    cursor.getString(cursor.getColumnIndex(Constants.MAP_GEO_COLOR)),
                    cursor.getString(cursor.getColumnIndex(Constants.MAP_PET_COLOR)),
                    cursor.getInt(cursor.getColumnIndex(Constants.MAP_GEO_SIZE)),
                    cursor.getInt(cursor.getColumnIndex(Constants.MAP_PET_SIZE)));
        cursor.close();
        db.close();
        return mapPreferences;
    }


    @SuppressLint("Range")
    public String getIdFromPetFeederID(String petFeederId) {
        String query = "SELECT " + Constants.COLUMN_ID + " FROM " + Constants.TABLE_NAME + " WHERE " + Constants.COLUMN_PET_FEEDER_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{petFeederId});

        String returnValue = null;
        if (cursor.moveToFirst() && cursor.getColumnIndex(Constants.COLUMN_ID) != -1) {
            returnValue = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));
        }

        cursor.close();
        return returnValue;
    }
    @SuppressLint("Range")
    public String getPetFeederIDFromId(String MAC_ADDRESS) {
        if (MAC_ADDRESS==null) return null;
        String query = "SELECT " + Constants.COLUMN_PET_FEEDER_ID + " FROM " + Constants.TABLE_NAME + " WHERE " + Constants.COLUMN_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{MAC_ADDRESS});

        String returnValue = null;
        if (cursor.moveToFirst() && cursor.getColumnIndex(Constants.COLUMN_PET_FEEDER_ID) != -1) {
            returnValue = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PET_FEEDER_ID));
        }
        cursor.close();
        return returnValue;
    }

    public SQLiteDatabase getReadableDatabase(){
        return this.getWritableDatabase();
    }
}
