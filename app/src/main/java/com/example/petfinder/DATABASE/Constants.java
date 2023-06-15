package com.example.petfinder.DATABASE;

public class Constants {

    public static final String DATABASE_NAME = "PetDatabase.db";
    public static final int DATABASE_VERSION = 1;

    //TABLE1
    public static final String TABLE_NAME = "PetRecord";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PETNAME = "petName";
    public static final String COLUMN_BREED = "breed";
    public static final String COLUMN_SEX = "sex";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_IMAGE = "petPic";
    public static final String COLUMN_ADDED_TIMESTAMP = "added_timestamp";
    public static final String COLUMN_UPDATED_TIMESTAMP = "updated_timestamp";

    public static String query = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " TEXT, "
            + COLUMN_PETNAME + " TEXT, "
            + COLUMN_BREED + " TEXT, "
            + COLUMN_SEX + " TEXT, "
            + COLUMN_AGE + " TEXT, "
            + COLUMN_WEIGHT + " TEXT, "
            + COLUMN_IMAGE + " TEXT, "
            + COLUMN_ADDED_TIMESTAMP + " TEXT, "
            + COLUMN_UPDATED_TIMESTAMP + " TEXT);";

    //TABLE2
    public static final String TABLE_NAME2 = "DeviceRecord";
    public static final String COLUMN_ID2 = "_id2";
    public static final String COLUMN_DEVICENAME = "deviceName";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_BTNAME = "BTName";
    public static final String COLUMN_BTADDRESS = "BTaddress";

    public static String query2 = "CREATE TABLE " + TABLE_NAME2 + "("
            + COLUMN_ID2 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DEVICENAME + " TEXT, "
            + COLUMN_LATITUDE + " TEXT, "
            + COLUMN_LONGITUDE + " TEXT, "
            + COLUMN_BTNAME + " TEXT, "
            + COLUMN_BTADDRESS + " TEXT);";

    //Table3
    public static final String TABLE_NAME3 = "Pedometer";
    public static final String COLUMN_ID3 = "_id3";
    public static final String COLUMN_NUMSTEPS = "numSteps";
    public static final String COLUMN_DATE = "date";

    public static String query3 = "CREATE TABLE " + TABLE_NAME3 + "("
            + COLUMN_ID3 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NUMSTEPS + " TEXT, "
            + COLUMN_DATE + " TEXT);";

}
