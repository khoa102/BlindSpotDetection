package com.example.blindspotdetection;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class DetectedObject implements Serializable, Comparable<DetectedObject> {
    private double range;
    private double doppler;
    private int peakVal;
    private double x;
    private double y;
    private double z;

    public DetectedObject(double range, double doppler, int peakVal, double x, double y, double z){
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

    public int getPeakVal() {
        return peakVal;
    }

    public void setPeakVal(int peakVal) {
        this.peakVal = peakVal;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public String toString(){
        return "X: " + this.x + "m  Y: "+ this.y + "m  Z: "+ this.z + "m  Velocity: "+ this.doppler + "m/s  range: "+  this.range +  "m  PeakVal: "+ this.peakVal + "\n";
    }

    /**
     *  The compareTo method allows us to compare to objects. It is compared first by x, then y, and finally z coordinate.
     * @param o
     * @return 1 if greater, 0 if equals and -1 if smaller than the object that is passed in
     */
    @Override
    public int compareTo(@NonNull DetectedObject o) {
        if (this.getX() > o.getX())
            return 1;
        else if (this.getX() < o.getX())
            return -1;
        else {
            if (this.getY() > o.getY())
                return 1;
            else if (this.getY() < o.getY())
                return -1;
            else {
                if (this.getZ() > o.getZ())
                    return 1;
                else if (this.getZ() < o.getZ())
                    return -1;
                else {
                    return 0;
                }
            }
        }
    }
}
