package com.example.petfinder.DATABASE.reports;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.application.PetFinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class PedometerReport {

    DatabaseHelper databaseHelper;
    Integer highestPedometer = 0;

    public PedometerReport(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    @SuppressLint("SimpleDateFormat")
    private Integer dayOfWeek(String date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(Objects.requireNonNull(new SimpleDateFormat("MM/dd/yyyy").parse(date)));
        return c.get(Calendar.DAY_OF_WEEK);
    }

    @SuppressLint("Range")
    public List<Integer> getDaily(){
        highestPedometer = 0;

        try {
            PetFinder.getInstance().getPedometer().saveData();
        }
        catch(Exception ignored) {}

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        LinkedHashMap<Integer, Integer> returnHelper = new LinkedHashMap<>();
        returnHelper.put(Calendar.SUNDAY, 0);
        returnHelper.put(Calendar.MONDAY, 0);
        returnHelper.put(Calendar.TUESDAY, 0);
        returnHelper.put(Calendar.WEDNESDAY, 0);
        returnHelper.put(Calendar.THURSDAY, 0);
        returnHelper.put(Calendar.FRIDAY, 0);
        returnHelper.put(Calendar.SATURDAY, 0);
        for (String date : GPSReport.getThisWeek()) {
            Cursor cursor = db.query(
                    Constants.TABLE_NAME3,
                    null,
                    Constants.COLUMN_ID + " = ? AND " + Constants.COLUMN_DATE + " = ?",
                    new String[]{PetFinder.getInstance().getCurrentMacAddress(), date},
                    null,
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()){
                do {
                    Integer day;
                    try {
                        day = dayOfWeek(date);
                    } catch (ParseException ignored) {
                        continue;
                    }
                    int amount = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_NUMSTEPS));
                    returnHelper.put(day, amount);
                    if (amount > highestPedometer) highestPedometer = amount;
                } while (cursor.moveToNext());
            }
        }
        db.close();
        ArrayList<Integer> returnValue = new ArrayList<>(returnHelper.values());
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

    private int getYearToday(){
        return Calendar.getInstance().get(Calendar.YEAR);
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
