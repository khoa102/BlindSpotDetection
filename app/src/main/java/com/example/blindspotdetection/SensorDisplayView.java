package com.example.blindspotdetection;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 *  Custom View that is in development.
 */
public class SensorDisplayView extends View {
    /** The degree for the detection zone of the sensor */
    private int sensorDegree;

    /**  The position of the sensor. 0 means left and 1 means right*/
    private int sensorPosition;

    /** A flag for detected object */
    private boolean isDetected;

    // Declaring paint objects for draw
    Paint sensorPaint;
    Paint undetectedZonePaint;
    Paint detectedZonePaint;
    Paint zonePaint;

    /**
     *  Constructor for the SensorDisplayView
     * @param context   Application Context.
     * @param attrs     Attributes set that is read from the XML file.
     */
    public SensorDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SensorDisplayView, 0, 0);

        try{
            // Reading the degree of sensor with current default value is 0
            sensorDegree = a.getInteger(R.styleable.SensorDisplayView_sensorDegree, 0);
            sensorPosition = a.getInteger(R.styleable.SensorDisplayView_sensorPosition, 0);
            isDetected = a.getBoolean(R.styleable.SensorDisplayView_objectDetected, false);
        } finally {
            a.recycle();
        }
        init();
    }

    /**
     *  Getter for sensor degree.
     * @return  The degree of the sensor detection zone.
     */
    public int getSensorDegree(){
        return sensorDegree;
    }

    /**
     *  Getter for sensor position.
     * @return  an integer represents the position of the sensor. 0 means left and 1 means right.
     */
    public int getSensorPosition(){
        return sensorPosition;
    }

    /**
     *  Getter for objects detected flag.
     * @return  True if objects are detected and False otherwise.
     */
    public boolean getIsDetected(){
        return isDetected;
    }

    /**
     *  Setter for sensor degree
     * @param degree  the new degree of the sensor detection zone.
     */
    public void setSensorDegree(int degree){
        sensorDegree = degree;
        invalidate(); // let the system knows that it needs to be redrawn
        requestLayout(); // Request a new layout if a property changes that might affect the size or shape of the view.
    }

    /**
     *  Setter for sensor position
     * @param position  the new position of the sensor. 0 means left and 1 means right
     */
    public void setSensorPosition(int position){
        sensorPosition = position;
        invalidate();
    }

    /**
     *  Setter for whether objects is detected
     * @param detected  A boolean that is true if objects are detected and false otherwise
     */
    public void setDetected(boolean detected){
        isDetected = detected;
        invalidate();
    }


    // Currently, initialize paint object
    /**
     *  Initialize objects for onDraw().
     */
    private void init(){
        sensorPaint = new Paint();

        undetectedZonePaint = new Paint();
        undetectedZonePaint.setStyle(Style.FILL);
        undetectedZonePaint.setColor(Color.GREEN);
        undetectedZonePaint.setAntiAlias(true);


        detectedZonePaint = new Paint();
        detectedZonePaint.setStyle(Style.FILL);
        detectedZonePaint.setColor(Color.RED);
        detectedZonePaint.setAntiAlias(true);
    }

    /**
     *  Draw the view.
     * @param canvas    The canvas object for the view.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDetected)
            zonePaint = detectedZonePaint;
        else
            zonePaint = undetectedZonePaint;

        if (sensorPosition == 0) {
            int startDegree = 180 - sensorDegree/2;
            canvas.drawArc(0,
                    0,
                    this.getWidth() * 2,
                    this.getHeight(),
                    startDegree,
                    sensorDegree,
                    true,
                    zonePaint);
        }else{
            int startDegree = 360 - sensorDegree/2;
            canvas.drawArc(-this.getWidth(),
                    0,
                    this.getWidth(),
                    this.getHeight(),
                    startDegree,
                    sensorDegree,
                    true,
                    zonePaint);
        }
    }
}
