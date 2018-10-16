package com.example.blindspotdetection;

import java.io.Serializable;

public class DetectedObject implements Serializable {
    private double range;
    private double doppler;
    private double peakVal;
    private short x;
    private short y;
    private short z;

    public DetectedObject(double range, double doppler, double peakVal, short x, short y, short z){
        this.range = range;
        this.doppler = doppler;
        this.peakVal = peakVal;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getDoppler() {
        return doppler;
    }

    public void setDoppler(double doppler) {
        this.doppler = doppler;
    }

    public double getPeakVal() {
        return peakVal;
    }

    public void setPeakVal(double peakVal) {
        this.peakVal = peakVal;
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public short getZ() {
        return z;
    }

    public void setZ(short z) {
        this.z = z;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
}
