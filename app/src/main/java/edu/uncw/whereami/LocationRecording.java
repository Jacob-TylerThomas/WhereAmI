package edu.uncw.whereami;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class LocationRecording {

    @Id
    private long id;
    private Date timestamp;
    private double latitude;
    private double longitude;
    private double acc;

    public LocationRecording(Date timestamp, double latitude, double longitude, double acc) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acc = acc;
    }
    
    public LocationRecording(long id, Date timestamp, double latitude, double longitude, double acc) {
        this.id = id;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acc = acc;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAcc() {
        return acc;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }
}

