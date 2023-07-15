package com.example.petfinder.DataSharing;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;

public class PetFinderContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.petfinder";
    public static final Uri CONTENT_URI_PETS = Uri.parse("content://"+AUTHORITY+"/"+ Constants.TABLE_NAME);
    public static final Uri CONTENT_URI_STEP = Uri.parse("content://"+AUTHORITY+"/"+ Constants.TABLE_NAME3);

    private static final int PETS = 1;
    private static final int STEP = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(AUTHORITY, Constants.TABLE_NAME, PETS);
        URI_MATCHER.addURI(AUTHORITY, Constants.TABLE_NAME3, STEP);
    }

    public static final String CONTENT_TYPE_PETS = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Constants.TABLE_NAME;
    public static final String CONTENT_TYPE_STEP = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Constants.TABLE_NAME3;

    DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (URI_MATCHER.match(uri)){
            case PETS:
                if (selection==null) {
                    cursor = databaseHelper.getAllPets();
                } else {
                    cursor = databaseHelper.query(uri, projection, selection, selectionArgs, sortOrder, URI_MATCHER, PETS, getContext());
                }
                break;
            case STEP:
                cursor = databaseHelper.getAllSteps();
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case PETS: return CONTENT_TYPE_PETS;
            case STEP: return CONTENT_TYPE_STEP;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        /*
        * BOTH PETS AND STEP DOES NOT SUPPORT THIS OPERATION.
        * */
        new UnsupportedOperationException("insert operation not supported!");
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int deleteCount = -1;
        /*
         * STEP DOES NOT SUPPORT THIS OPERATION.
         * */
        if (URI_MATCHER.match(uri) == PETS) {
            deleteCount = deletePets(uri, selection, selectionArgs);
        } else {
            new UnsupportedOperationException("insert operation not supported!");
        }
        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int updateCount = -1;
        /*
         * STEP DOES NOT SUPPORT THIS OPERATION.
         * */
        if (URI_MATCHER.match(uri) == PETS) {
            updateCount = updatePets(uri, values);
        } else {
            new UnsupportedOperationException("insert operation not supported!");
        }
        return updateCount;
    }

    private int deletePets(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs){
        int returnValue = databaseHelper.deleteData(selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
    }
    private int updatePets(@NonNull Uri uri, @Nullable ContentValues values){
        assert values != null;
        int returnValue = databaseHelper.updateData(values.getAsString(Constants.COLUMN_PET_FINDER_ID),
                values.getAsString(Constants.COLUMN_PETNAME),
                values.getAsString(Constants.COLUMN_BREED),
                values.getAsString(Constants.COLUMN_SEX),
                values.getAsString(Constants.COLUMN_BIRTHDATE),
                values.getAsInteger(Constants.COLUMN_AGE),
                values.getAsInteger(Constants.COLUMN_WEIGHT),
                values.getAsString(Constants.COLUMN_IMAGE),
                values.getAsString(Constants.COLUMN_UPDATED_TIMESTAMP),
                values.getAsString(Constants.COLUMN_ID));
        getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
    }
}
