package com.example.blindspotdetection;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * A class to store detected objects after processing.
 */
public class DetectedObject implements Serializable, Comparable<DetectedObject> {
    private double range;
    private double doppler;
    private int peakVal;
    private double x;
    private double y;
    private double z;

    /**
     *  Create a detected object with data from sensor
     * @param range     range in meter
     * @param doppler   velocity in m/s
     * @param peakVal   peak value
     * @param x         x-coordinate in meter
     * @param y         y-coordinate in meter
     * @param z         z-coordinate in meter
     */
    public DetectedObject(double range, double doppler, int peakVal, double x, double y, double z){
        this.range = range;
        this.doppler = doppler;
        this.peakVal = peakVal;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     *  Getter for doppler
     * @return  velocity in meter per second
     */
    public double getDoppler() {
        return doppler;
    }

    /**
     *  Setter for doppler
     * @param doppler   new velocity in meter per second
     */
    public void setDoppler(double doppler) {
        this.doppler = doppler;
    }

    /**
     *  Getter for peak value
     * @return  peak value in meter
     */
    public int getPeakVal() {
        return peakVal;
    }

    /**
     *  Setter for peak value
     * @param peakVal   new peak value in meter
     */
    public void setPeakVal(int peakVal) {
        this.peakVal = peakVal;
    }

    /**
     *  Getter for x-coordinate
     * @return  x-coordinate in meter
     */
    public double getX() {
        return x;
    }

    /**
     *  Setter for x-coordinate
     * @param x   new x-coordinate in meter
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     *  Getter for y coordinate
     * @return  y-coordinate in meter
     */
    public double getY() {
        return y;
    }

    /**
     *  Setter for y-coordinate
     * @param y   new y-coordinate in meter
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     *  Getter for z coordinate
     * @return  z-coordinate in meter
     */
    public double getZ() {
        return z;
    }

    /**
     *  Setter for z-coordinate
     * @param z   new z-coordinate in meter
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     *  Getter for range
     * @return  range in meter
     */
    public double getRange() {
        return range;
    }

    /**
     *  Setter for range
     * @param range   new range in meter
     */
    public void setRange(double range) {
        this.range = range;
    }

    public String toString(){
        return "X: " + this.x + "m  Y: "+ this.y + "m  Z: "+ this.z + "m  Velocity: "+ this.doppler + "m/s  range: "+  this.range +  "m  PeakVal: "+ this.peakVal + "\n";
    }

    /**
     *  The compareTo method allows us to compare to objects. It is compared first by x, then y, and finally z coordinate.
     * @param o A another detected object that this object compares to.
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
                return Double.compare(this.getZ(), o.getZ());
            }
        }
    }
}
