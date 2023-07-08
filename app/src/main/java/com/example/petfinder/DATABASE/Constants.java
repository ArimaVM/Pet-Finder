package com.example.petfinder.DATABASE;

public class Constants {

    public static final String DATABASE_NAME = "PetKoinu.db";
    public static final int DATABASE_VERSION = 4;

    //TABLE1
    public static final String TABLE_NAME = "PetRecord";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PETNAME = "petName";
    public static final String COLUMN_BREED = "breed";
    public static final String COLUMN_SEX = "sex";
    public static final String COLUMN_BIRTHDATE = "birthdate";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_IMAGE = "petPic";
    public static final String COLUMN_ADDED_TIMESTAMP = "added_timestamp";
    public static final String COLUMN_UPDATED_TIMESTAMP = "updated_timestamp";
    public static final String COLUMN_PET_FEEDER_ID = "petFeederId";
    public static final String COLUMN_PET_FINDER_ID = "petFinderId";
    public static String query = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " TEXT PRIMARY KEY, "
            + COLUMN_PETNAME + " TEXT, "
            + COLUMN_BREED + " TEXT, "
            + COLUMN_SEX + " TEXT, "
            + COLUMN_BIRTHDATE + " TEXT, "
            + COLUMN_AGE + " INTEGER, "
            + COLUMN_WEIGHT + " INTEGER, "
            + COLUMN_IMAGE + " TEXT, "
            + COLUMN_ADDED_TIMESTAMP + " TEXT, "
            + COLUMN_UPDATED_TIMESTAMP + " TEXT,"
            + COLUMN_PET_FEEDER_ID + " TEXT);";

    //TABLE2
    public static final String TABLE_NAME2 = "Geofence";
    public static final String COLUMN_ID2 = "_id2";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_RADIUS = "radius";

    public static String query2 = "CREATE TABLE " + TABLE_NAME2 + "("
            + COLUMN_ID2 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LATITUDE + " DOUBLE, "
            + COLUMN_LONGITUDE + " DOUBLE, "
            + COLUMN_RADIUS + " INTEGER, "
            + COLUMN_ID + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + "));";

    //Table3
    public static final String TABLE_NAME3 = "Pedometer";
    public static final String COLUMN_ID3 = "_id3";
    public static final String COLUMN_NUMSTEPS = "numSteps";
    public static final String COLUMN_DATE = "date";

    public static String query3 = "CREATE TABLE " + TABLE_NAME3 + "("
            + COLUMN_ID3 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NUMSTEPS + " INTEGER, "
            + COLUMN_DATE + " TEXT, "
            + COLUMN_ID + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + "));";


    //TABLE4
    public static final String TABLE_NAME4 = "GPS";
    public static final String COLUMN_ID4 = "_id4";
    public static final String COLUMN_LONG = "long";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DATE2 = "date";

    public static String query4 = "CREATE TABLE " + TABLE_NAME4 + "("
            + COLUMN_ID4 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LONG + " TEXT, "
            + COLUMN_LAT + " TEXT, "
            + COLUMN_TIME + " TEXT, "
            + COLUMN_DATE2 + " TEXT, "
            + COLUMN_ID + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + "));";

    //TABLE 5
    public static final String TABLE_NAME5 = "PetMoreInfo";
    public static final String COLUMN_ID5 = "_id5";
    public static final String COLUMN_ALLERGIES = "allergies";
    public static final String COLUMN_MEDICATIONS = "medications";
    public static final String COLUMN_VETNAME = "vetName";
    public static final String COLUMN_VETCONTACT = "vetContact";

    public static String query5 = "CREATE TABLE " + TABLE_NAME5 + "("
            + COLUMN_ID5 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ALLERGIES + " TEXT, "
            + COLUMN_MEDICATIONS + " TEXT, "
            + COLUMN_VETNAME + " TEXT, "
            + COLUMN_VETCONTACT + " TEXT, "
            + COLUMN_ID + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + "));";


    //TABLE6
    public static final String TABLE_NAME6 = "GeofencePreferences";
    public static final String COLUMN_ID6 = "_id6";
    public static final String MAP_STYLE = "map_style";
    public static final String MAP_GEO_ICON = "geofence_marker_icon";
    public static final String MAP_PET_ICON = "pet_marker_icon";
    public static final String MAP_GEO_COLOR = "geofence_marker_color";
    public static final String MAP_PET_COLOR = "pet_marker_color";
    public static final String MAP_GEO_SIZE = "geofence_marker_size";
    public static final String MAP_PET_SIZE = "pet_marker_size";

    public static String query6 = "CREATE TABLE " + TABLE_NAME6 + "("
            + COLUMN_ID6 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MAP_STYLE + " INTEGER, "
            + MAP_GEO_ICON + " INTEGER, "
            + MAP_PET_ICON + " INTEGER, "
            + MAP_GEO_COLOR + " TEXT, "
            + MAP_PET_COLOR + " TEXT, "
            + COLUMN_ID + " TEXT, "
            + MAP_GEO_SIZE + " INTEGER, "
            + MAP_PET_SIZE + " INTEGER, "
            + "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + "));";
}