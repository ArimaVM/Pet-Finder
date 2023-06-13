package com.example.petfinder.application.DATABASE;

public class Constants {

    public static final String DATABASE_NAME = "PetDatabase.db";
    public static final int DATABASE_VERSION = 1;

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
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PETNAME + " TEXT, "
            + COLUMN_BREED + " TEXT, "
            + COLUMN_SEX + " TEXT, "
            + COLUMN_AGE + " TEXT, "
            + COLUMN_WEIGHT + " TEXT, "
            + COLUMN_IMAGE + " TEXT, "
            + COLUMN_ADDED_TIMESTAMP + " TEXT, "
            + COLUMN_UPDATED_TIMESTAMP + " TEXT);";

}
