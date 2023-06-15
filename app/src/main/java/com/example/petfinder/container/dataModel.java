package com.example.petfinder.container;

public class dataModel {
    public static class GPS {
        String mac_address;
        String latitude;
        String longitude;
        String date;
        String time;

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

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
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

        public String getMAC_Address() {
            return MAC_Address;
        }

        public void setMAC_Address(String MAC_Address) {
            this.MAC_Address = MAC_Address;
        }

        public int getPedometer() {
            return pedometer;
        }

        public void setPedometer(int pedometer) {
            this.pedometer = pedometer;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
