package com.example.petfinder.container;

import com.google.android.gms.maps.model.LatLng;

public class dataModel {
    public static class GPSData {
        String mac_address;
        Double latitude;
        Double longitude;
        String date;
        String time;

        public GPSData() {
            this.mac_address = null;
            this.latitude = null;
            this.longitude = null;
            this.date = null;
            this.time = null;
        }

        public GPSData(String mac_address, Double latitude, Double longitude, String date, String time) {
            this.mac_address = mac_address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.date = date;
            this.time = time;
        }

        public String getMac_address() {
            return mac_address;
        }

        public void setMac_address(String mac_address) {
            this.mac_address = mac_address;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public boolean isComplete() {
            return (longitude != null) && (latitude != null);
        }
    }
    public static class PedometerData {
        String MAC_Address;
        int pedometer;
        String date;

        public PedometerData(String MAC_Address, int pedometer, String date) {
            this.MAC_Address = MAC_Address;
            this.pedometer = pedometer;
            this.date = date;
        }

        public String getMAC_Address() {
            return MAC_Address;
        }
        public int getPedometer() {
            return pedometer;
        }
        public String getDate() {
            return date;
        }
    }
    public static class GeofenceData{
        LatLng latLng;
        Integer radius;

        public GeofenceData() {
            this.latLng = null;
            this.radius = null;
        }

        public GeofenceData(LatLng latLng, Integer radius) {
            this.latLng = latLng;
            this.radius = radius;
        }

        public LatLng getLatLng() {
            return latLng;
        }
        public void setLatLng(LatLng latLng) {
            this.latLng = latLng;
        }

        public Integer getRadius() {
            return radius;
        }
        public void setRadius(Integer radius) {
            this.radius = radius;
        }
    }
    public static class MapPreferences{
        Integer mapStyle, geofenceIcon, petIcon, geofenceSize, petSize;
        String geofenceColor, petColor;

        public MapPreferences() {
            this.mapStyle = null;
            this.geofenceIcon = null;
            this.petIcon = null;
            this.geofenceColor = null;
            this.petColor = null;
        }

        public MapPreferences(Integer mapStyle, Integer geofenceIcon, Integer petIcon, String geofenceColor, String petColor, Integer geofenceSize, Integer petSize) {
            this.mapStyle = mapStyle;
            this.geofenceIcon = geofenceIcon;
            this.petIcon = petIcon;
            this.geofenceColor = geofenceColor;
            this.petColor = petColor;
            this.geofenceSize = geofenceSize;
            this.petSize = petSize;
        }

        public Integer getMapStyle() {
            return mapStyle;
        }
        public void setMapStyle(Integer mapStyle) {
            this.mapStyle = mapStyle;
        }

        public Integer getGeofenceIcon() {
            return geofenceIcon;
        }
        public void setGeofenceIcon(Integer geofenceIcon) {
            this.geofenceIcon = geofenceIcon;
        }

        public Integer getPetIcon() {
            return petIcon;
        }
        public void setPetIcon(Integer petIcon) {
            this.petIcon = petIcon;
        }

        public String getGeofenceColor() {
            return geofenceColor;
        }
        public void setGeofenceColor(String geofenceColor) {
            this.geofenceColor = geofenceColor;
        }

        public String getPetColor() {
            return petColor;
        }
        public void setPetColor(String petColor) {
            this.petColor = petColor;
        }

        public Integer getGeofenceSize() {
            return geofenceSize;
        }
        public void setGeofenceSize(Integer geofenceSize) {
            this.geofenceSize = geofenceSize;
        }

        public Integer getPetSize() {
            return petSize;
        }
        public void setPetSize(Integer petSize) {
            this.petSize = petSize;
        }
    }
}
