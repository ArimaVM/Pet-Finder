package com.example.petfinder.DATABASE.reports;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.application.PetFinder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PedometerReport {

    DatabaseHelper databaseHelper;
    Integer highestPedometer = 0;

    public PedometerReport(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    @SuppressLint("Range")
    public List<Integer> getDaily(){
        highestPedometer = 0;
        try {
            PetFinder.getInstance().getPedometer().saveData();
        }
        catch(Exception ignored) {}

        int limit = getLimitDaily();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME3,
                null,
                Constants.COLUMN_ID+" = ? ",
                new String[]{PetFinder.getInstance().getCurrentMacAddress()},
                null,
                null,
                null,
                "7"
        );
        List<Integer> returnValue = new ArrayList<>();
        boolean sundayStarted = false;
        if (cursor.getCount()<7){
            sundayStarted = true;
        }
        int iteration = 0;
        while (cursor.moveToNext()) {
            if (!sundayStarted) {
                if (StringToDay(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATE))) == Calendar.SUNDAY) {
                    sundayStarted = true;
                }
            }
            if (sundayStarted) {
                iteration++;
                if (iteration==1) {
                    int dayOfFirstIteration = StringToDay(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATE)));
                    if (dayOfFirstIteration != Calendar.SUNDAY) {
                        for (int i = 0; i <= dayOfFirstIteration-1; i++) returnValue.add(0);
                    }
                }
                int steps = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_NUMSTEPS));
                returnValue.add(steps);
                if (steps>highestPedometer) highestPedometer = steps;
            }
        }
        for (int i = limit; i <= 7; i++) returnValue.add(0);
        cursor.close();
        db.close();
        return returnValue;
    }

    @SuppressLint("Range")
    public List<Integer> getMonthly(){
        highestPedometer = 0;
        try {
            PetFinder.getInstance().getPedometer().saveData();
        }
        catch(Exception ignored) {}

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME3,
                null,
                Constants.COLUMN_ID+" = ? AND "+Constants.COLUMN_DATE+" LIKE ?",
                new String[]{PetFinder.getInstance().getCurrentMacAddress(), "%"+getYearToday()},
                null,
                null,
                null,
                null
        );
        List<Integer> returnValue = new ArrayList<>();
        int iteration = 0;
        int previousMonth = -1;
        int count = 0;
        while (cursor.moveToNext()) {
            iteration++;

            int currentMonth = StringToMonth(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATE)));

            if (iteration==1){
                //runs only on first iteration
                previousMonth = currentMonth;
                for (int i = 0; i < currentMonth; i++) returnValue.add(0);
            }

            if (previousMonth!=currentMonth){
                returnValue.add(count);
                if (count>highestPedometer) highestPedometer = count;
                count = 0;
                previousMonth = currentMonth;
                count += cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_NUMSTEPS));
            } else {
                count += cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_NUMSTEPS));
            }
        }

        returnValue.add(count);
        if (count>highestPedometer) highestPedometer = count;

        for (int i = returnValue.size(); i <= 12; i++) returnValue.add(0);
        cursor.close();
        db.close();
        return returnValue;
    }
    @SuppressLint("Range")
    public Object[] getToday(){
        try {
            PetFinder.getInstance().getPedometer().saveData();
        }
        catch(Exception ignored) {}

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME3,
                null,
                Constants.COLUMN_ID+" = ? AND "+Constants.COLUMN_DATE+" = ?",
                new String[]{PetFinder.getInstance().getCurrentMacAddress(), PetFinder.getInstance().getCurrentDate()},
                null,
                null,
                Constants.COLUMN_DATE + " DESC",
                "1"
        );
        Object[] returnValue = new Object[]{0, 0.0};
        if (cursor.moveToFirst()){
             int value = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_NUMSTEPS));
             returnValue[0] = value;
             returnValue[1] = value * 0.5;
        }
        return returnValue;
    }

    private int getLimitDaily(){
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    private int getYearToday(){
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private int StringToDay(String date){
        Calendar calendar = Calendar.getInstance();

        String[] dateParts = date.split("/");

        int month = Integer.parseInt(dateParts[0])-1;
        int day = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        calendar.set(year, month, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    private int StringToMonth(String date){
        Calendar calendar = Calendar.getInstance();

        String[] dateParts = date.split("/");

        int month = Integer.parseInt(dateParts[0])-1;
        int day = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        calendar.set(year, month, day);
        return calendar.get(Calendar.MONTH);
    }

    public Integer getHighestPedometer() {
        return roundOff(highestPedometer);
    }

    public int roundOff(int input) {
        if (input % 5 == 0) {
            return input+5;
        } else return (input / 5) * 5 + 5;
    }
}
