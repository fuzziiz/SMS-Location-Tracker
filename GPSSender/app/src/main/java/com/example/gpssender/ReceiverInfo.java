package com.example.gpssender;

public class ReceiverInfo {
    private String phoneNumber;
    private double latitude;
    private double longitude;

    public ReceiverInfo(String phoneNumber, double latitude, double longitude) {
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public  void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return  longitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
}
