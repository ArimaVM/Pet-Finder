package com.example.petfinder.DATABASE.reports;

import static com.example.petfinder.DATABASE.reports.GPSReport.TimeDistance.ERROR;
import static com.example.petfinder.DATABASE.reports.GPSReport.TimeDistance.LESS_THAN;
import static com.example.petfinder.DATABASE.reports.GPSReport.TimeDistance.MORE_THAN;
import static com.example.petfinder.DATABASE.reports.GPSReport.TimeDistance.ONE_MINUTE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.container.dataModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GPSReport {

    DatabaseHelper databaseHelper;


    public enum GPSMode {
        DAILY, WEEKLY, MONTHLY;
    }

    enum TimeDistance{
        LESS_THAN, ONE_MINUTE, MORE_THAN, ERROR;
    }

    enum State{
        NO_DATA, HAS_DATA;
    }

    public GPSReport(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public PercentageToday getPercentageToday(GPSMode GPSMode){
        return new PercentageToday(databaseHelper, GPSMode);
    }
    public GeodeticCentroid getGeodeticCentroid(GPSMode GPSMode){
        return new GeodeticCentroid(databaseHelper, GPSMode);
    }
    public MarkerConnections getMarkerConnections(){
        return new MarkerConnections(databaseHelper);
    }

    public Boolean hasGeofence(){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_NAME2,
                null,
                Constants.COLUMN_ID+" = ? ",
                new String[]{PetFinder.getInstance().getCurrentMacAddress()},
                null,
                null,
                null,
                null
        );
        return cursor.getCount()>0;
    }

    private static List<String> getThisWeek(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        calendar.setTimeInMillis(System.currentTimeMillis());

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_WEEK, -1);
        }

        List<String> returnValue = new ArrayList<>();
        while (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            returnValue.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
        return returnValue;
    }
    private static List<String> getThisMonth(){
        List<String> debugReturn = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        List<String> returnValue = new ArrayList<>();

        while (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            returnValue.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return returnValue;
    }
    @SuppressLint("SimpleDateFormat")
    private static TimeDistance isWithinOneMinute(String previousTime, String currentTime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            Date prevTime = format.parse(previousTime);
            Date currTime = format.parse(currentTime);
            long difference = Math.abs(prevTime.getTime() - currTime.getTime());
            if (55*1000 <= difference && difference <= 65*1000){
                return ONE_MINUTE;
            } else if (55*1000 >= difference){
                return LESS_THAN;
            } else {
                return MORE_THAN;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return ERROR;
        }
    }
    private static int calculateMinuteDifference(String previousTime, String currentTime){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            Date prevTime = format.parse(previousTime);
            Date currTime = format.parse(currentTime);
            long difference = Math.abs(prevTime.getTime() - currTime.getTime());

            int diff = (int) difference/1000; //convert milliseconds to seconds.
            return diff/60; //convert seconds to minutes, disregarding excess seconds.

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static LatLng getCenterViewValue(List<MarkerRadius> geodeticData){
        ArrayList<LatLng> allLatLngs = new ArrayList<>();
        for (MarkerRadius markerRadius : geodeticData) allLatLngs.add(markerRadius.latLng);
        return getPolygonCenterPoint(allLatLngs);
    }
    private static LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList){
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < polygonPointsList.size() ; i++) {
            builder.include(polygonPointsList.get(i));
        }
        if (polygonPointsList.size()!=0) {
            LatLngBounds bounds = builder.build();
            centerLatLng = bounds.getCenter();
        }
        return centerLatLng;
    }

    public static class PercentageToday{

        enum Geofence{
            INSIDE, OUTSIDE;
        }

        DatabaseHelper databaseHelper;
        LatLng geofenceLatLng;
        Integer geofenceRadius;
        State state;

        Double inside;

        public PercentageToday(DatabaseHelper databaseHelper, GPSMode GPSMode) {
            this.databaseHelper = databaseHelper;
            dataModel.GeofenceData geofenceData = databaseHelper.getGeofence(PetFinder.getInstance().getCurrentMacAddress());
            geofenceLatLng = geofenceData.getLatLng();
            geofenceRadius = geofenceData.getRadius();
            inside = getInitialData(GPSMode);
        }

        public Double inside(){
            if (state == State.NO_DATA) return 0.0;
            return inside;
        }

        public Double outside(){
            if (state == State.NO_DATA) return 0.0;
            return 1.0-inside;
        }


        private Double getInitialData(GPSMode GPSMode){

            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Integer[] accumulatedCount = new Integer[]{0, 0};
            Integer[] count;
            Cursor cursor;
            boolean hadData = false;
            switch (GPSMode){
                case DAILY:
                    cursor = db.query(
                            Constants.TABLE_NAME4,
                            null,
                            Constants.COLUMN_ID + " = ? AND " + Constants.COLUMN_DATE2 + " = ?",
                            new String[]{PetFinder.getInstance().getCurrentMacAddress(), PetFinder.getInstance().getCurrentDate()},
                            null,
                            null,
                            null,
                            null
                    );

                    if (cursor.getCount() == 0) {
                        state = State.NO_DATA;
                        accumulatedCount[0] = 0;
                        accumulatedCount[1] = 0;
                    }
                    state = State.HAS_DATA;
                    count = processCursor(cursor);
                    accumulatedCount[0] += count[0];
                    accumulatedCount[1] += count[1];
                    break;
                case WEEKLY:
                    for (String date : getThisWeek()) {
                        cursor = db.query(
                                Constants.TABLE_NAME4,
                                null,
                                Constants.COLUMN_ID + " = ? AND " + Constants.COLUMN_DATE2 + " = ?",
                                new String[]{PetFinder.getInstance().getCurrentMacAddress(), date},
                                null,
                                null,
                                null,
                                null
                        );

                        if (cursor.getCount() == 0) {
                            if (!hadData) state = State.NO_DATA;
                            continue;
                        }
                        count = processCursor(cursor);
                        hadData = true;
                        state = State.HAS_DATA;
                        accumulatedCount[0] += count[0];
                        accumulatedCount[1] += count[1];
                    }
                    break;
                case MONTHLY:
                    for (String date : getThisMonth()) {
                        cursor = db.query(
                                Constants.TABLE_NAME4,
                                null,
                                Constants.COLUMN_ID + " = ? AND " + Constants.COLUMN_DATE2 + " = ?",
                                new String[]{PetFinder.getInstance().getCurrentMacAddress(), date},
                                null,
                                null,
                                null,
                                null
                        );

                        if (cursor.getCount() == 0) {
                            if (!hadData) state = State.NO_DATA;
                            continue;
                        }
                        count = processCursor(cursor);
                        hadData = true;
                        state = State.HAS_DATA;
                        accumulatedCount[0] += count[0];
                        accumulatedCount[1] += count[1];
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + GPSMode);
            }
            //calculate percentage.
            double total = accumulatedCount[0] + accumulatedCount[1];
            return accumulatedCount[0]/total;
        }

        @SuppressLint("Range")
        private Integer[] processCursor(Cursor cursor){
            Integer[] count = new Integer[]{0, 0}; //INSIDE, OUTSIDE
            int iteration = 0;
            String previousTime = "";
            while (cursor.moveToNext()) {
                iteration++;
                String currentTime = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIME));
                LatLng latLng = new LatLng(
                        Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAT))),
                        Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LONG))));
                if (iteration == 1) {
                    if (Objects.equals(geofence(latLng), Geofence.INSIDE)) count[0] += 1;
                    else count[1] += 1;
                    previousTime = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIME));
                } else {
                    switch (isWithinOneMinute(previousTime, currentTime)) {
                        case ONE_MINUTE:
                            if (Objects.equals(geofence(latLng), Geofence.INSIDE))
                                count[0] += 1;
                            else count[1] += 1;
                            previousTime = currentTime;
                            break;
                        case LESS_THAN:
                            continue; //skip to next valid one minute distance.
                        case MORE_THAN:
                            for (int i = 0; i <= calculateMinuteDifference(previousTime, currentTime); i++) {
                                if (Objects.equals(geofence(latLng), Geofence.INSIDE))
                                    count[0] += 1;
                                else count[1] += 1;
                            }
                            previousTime = currentTime;
                            break;
                        default:
                            break;
                    }
                }
            }
            return count;
        }

        private Geofence geofence(LatLng latLng) {
            android.location.Location locationA = new android.location.Location("point A");
            locationA.setLatitude(latLng.latitude);
            locationA.setLongitude(latLng.longitude);


            android.location.Location locationB = new android.location.Location("point B");
            locationB.setLatitude(geofenceLatLng.latitude);
            locationB.setLongitude(geofenceLatLng.longitude);

            float distance = locationA.distanceTo(locationB);

            if (distance > geofenceRadius) {
                return Geofence.OUTSIDE;
            }
            return Geofence.INSIDE;
        }
    }

    public static class GeodeticCentroid{
        DatabaseHelper databaseHelper;
        LatLng centerView;
        GPSMode gpsMode;
        LatLng geofenceLatLng;
        Integer geofenceRadius;
        List<MarkerRadius> points;

        public GeodeticCentroid(DatabaseHelper databaseHelper, GPSMode gpsMode) {
            this.databaseHelper = databaseHelper;
            this.gpsMode = gpsMode;
            dataModel.GeofenceData geofenceData = databaseHelper.getGeofence(PetFinder.getInstance().getCurrentMacAddress());
            geofenceLatLng = geofenceData.getLatLng();
            geofenceRadius = geofenceData.getRadius();
            points = getPoints();
        }

        public MarkerRadius getGeofence() {
            return new MarkerRadius(geofenceLatLng, geofenceRadius);
        }
        public LatLng getCenterView(){ return centerView; }
        public List<MarkerRadius> getMarkers(){return points;}

        @SuppressLint("Range")
        private List<MarkerRadius> getPoints(){
            List<MarkerRadius> geodeticData = new ArrayList<>();
            //ALGORITHM:
            // - QUERY FOR GPS DATA DEPENDING ON `gpsMode`.
            // - PUT ALL LOCATIONS IN A LIST.
            // - ITERATE THROUGH THAT LIST AND CREATE ANOTHER LIST DEPENDING ON:
            //      - 1 METER FROM THE FIRST `LatLng` OF THE LIST.
            // - REMOVE THE `LatLng` FROM THE FIRST LIST.
            // - GET THE RESULTING `LatLng` FROM `getPolygonCenterPoint` AND STORE IT INSIDE `geodeticData`
            //   VARIABLE AS KEY AND THE LIST LENGTH AS VALUE.
            // - REPEAT UNTIL THE FIRST LIST IS EMPTY.
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            List<LatLng> latLngs = new ArrayList<>();
            List<String> dates = gpsMode==GPSMode.DAILY?
                    new ArrayList<>(Collections.singleton(PetFinder.getInstance().getCurrentDate())):
                    (gpsMode==GPSMode.WEEKLY? getThisWeek() : getThisMonth());
            for (String date : dates) {
                Cursor cursor = db.query(
                        Constants.TABLE_NAME4,
                        null,
                        Constants.COLUMN_ID + " = ? AND " + Constants.COLUMN_DATE + " = ?",
                        new String[]{PetFinder.getInstance().getCurrentMacAddress(), date},
                        null,
                        null,
                        null,
                        null
                );
                while (cursor.moveToNext())
                    latLngs.add(new LatLng(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAT))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LONG)))
                    ));
            }
            while (latLngs.size()>0){
                ArrayList<LatLng> bundledLatLng = new ArrayList<>();
                LatLng bundledLatLngLead = new LatLng(0, 0);
                int iteration = 0;
                List<LatLng> referenceLatLngs = new ArrayList<>(latLngs);
                for (LatLng latLng : referenceLatLngs) {
                    iteration++;
                    if (iteration==1){
                        bundledLatLng.add(latLng);
                        bundledLatLngLead = latLng;
                        latLngs.remove(latLng);
                        continue;
                    }
                    if (withinFiveMeters(bundledLatLngLead, latLng)){
                        bundledLatLng.add(latLng);
                        latLngs.remove(latLng);
                    }
                }
                geodeticData.add(new MarkerRadius(getPolygonCenterPoint(bundledLatLng), bundledLatLng.size()));
            }
            centerView = getCenterViewValue(geodeticData);
            return geodeticData;
        }
        private Boolean withinFiveMeters(LatLng latLngA, LatLng latLngB){
            android.location.Location locationA = new android.location.Location("point A");
            locationA.setLatitude(latLngA.latitude);
            locationA.setLongitude(latLngA.longitude);

            android.location.Location locationB = new android.location.Location("point B");
            locationB.setLatitude(latLngB.latitude);
            locationB.setLongitude(latLngB.longitude);

            float distance = locationA.distanceTo(locationB);
            return distance <= 5;
        }
    }

    public static class MarkerConnections{
        DatabaseHelper databaseHelper;
        LatLng centerView;
        LatLng geofenceLatLng;
        Integer geofenceRadius;
        List<List<LatLng>> points;

        public MarkerConnections(DatabaseHelper databaseHelper) {
            this.databaseHelper = databaseHelper;
            dataModel.GeofenceData geofenceData = databaseHelper.getGeofence(PetFinder.getInstance().getCurrentMacAddress());
            geofenceLatLng = geofenceData.getLatLng();
            geofenceRadius = geofenceData.getRadius();
            points = getPoints();
        }

        public List<List<LatLng>> getData(){
            return points;
        }

        public LatLng getCenterView(){
            return centerView;
        }

        public MarkerRadius getGeofence(){
            return new MarkerRadius(geofenceLatLng, geofenceRadius);
        }

        @SuppressLint("Range")
        private List<List<LatLng>> getPoints() {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    Constants.TABLE_NAME4,
                    null,
                    Constants.COLUMN_ID + " = ? AND " + Constants.COLUMN_DATE + " = ?",
                    new String[]{PetFinder.getInstance().getCurrentMacAddress(), PetFinder.getInstance().getCurrentDate()},
                    null,
                    null,
                    null,
                    null
            );
            List<List<LatLng>> returnData = new ArrayList<>();
            List<LatLng> data = new ArrayList<>();
            List<MarkerRadius> allData = new ArrayList<>();
            String previousTime = null;
            int iteration = 0;
            while (cursor.moveToNext()) {
                iteration++;
                String currentTime = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIME));
                LatLng latLng = new LatLng(
                        Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAT))),
                        Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LONG)))
                );
                allData.add(new MarkerRadius(latLng, 1));
                if (iteration == 1){
                    data.add(latLng);
                    previousTime = currentTime;
                } else {
                    switch (isWithinOneMinute(previousTime, currentTime)) {
                        case ONE_MINUTE:
                            data.add(latLng);
                            previousTime = currentTime;
                            break;
                        case LESS_THAN:
                            continue;
                        case MORE_THAN:
                            returnData.add(data);
                            data = new ArrayList<>();
                            data.add(latLng);
                            previousTime = currentTime;
                            break;
                    }
                }
            }
            centerView = getCenterViewValue(allData);
            return returnData;
        }
    }

    public static class MarkerRadius{
        LatLng latLng;
        Integer radius;

        public MarkerRadius(LatLng latLng, Integer radius) {
            this.latLng = latLng;
            this.radius = radius;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public Integer getRadius() {
            return radius;
        }
    }
}
