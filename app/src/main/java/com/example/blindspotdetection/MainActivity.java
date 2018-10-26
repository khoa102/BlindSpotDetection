package com.example.blindspotdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    /** Variable for setting up connection*/
    private SensorConnection sensorConnection;
    private final static int REQUEST_ENABLE_BT = 1;

    /** View that is used to put information on the screen */
    private TextView textView;
    GraphView graph;

    /** A Point Series to represent the detected objects. */
    PointsGraphSeries<DataPoint> pointSeries;

    /** A list of detected data in the current frame. */
    DetectedObject[] objects;

    /** Media Player object to play sound. */
    MediaPlayer player;

    /** A TAG for logging. */
    static final String TAG = "MainActivity";

    /** Messenger of the Service to send message to the Service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /** Flag to remember the status of the service */
    private boolean running = false;

    /** Command to the service to receive detected objects from Service.*/
    public static final int MSG_DETECTED_OBJECT = 0;

    /**  Command to the service to get a test message from the Service. */
    public static final int MSG_GET_TEST_MESSAGE = 5;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            // Suppose to execute visualizer
            switch(msg.what) {
                case MSG_GET_TEST_MESSAGE:
                    textView.setText(textView.getText() + msg.obj.toString());
                    break;
                case MSG_DETECTED_OBJECT:
                    byte buffer[] = (byte[]) msg.obj;
                    /** An object to read the receive data packet and process it. */
                    SensorProcessor sensorprocessor = new SensorProcessor();
                    sensorprocessor.loadPacket(buffer);
                    boolean result = sensorprocessor.processData();
                    if (result){
                        textView.setText(textView.getText() + "\n\n Successfully process data frame \n");
                        DetectedObject[] objects = sensorprocessor.getDetectedObjects();
                        for (int i =0; i < objects.length; i++){
                            textView.setText(textView.getText() + objects[i].toString());
                        }
                        textView.setText(textView.getText() + "\n");

                        sensorprocessor.sortDetectedObjects();
                        DataPoint newDataPoint[] = new DataPoint[objects.length];
                        int index = 0;
                        for (DetectedObject object : objects){
                            newDataPoint[index] = new DataPoint(object.getX(), object.getY());
                            index++;
                        }
                        pointSeries.resetData(newDataPoint);
                    }
                    else {
                        textView.setText(textView.getText() + "\n\n Failed process data frame \n");
                    }
//                    textView.setText(textView.getText());
//                    String result = new String(buffer, 0, buffer.length);
//                    textView.setText(textView.getText() +  result);//msg.obj.toString());
//                    DetectedObject[] objects = (DetectedObject[]) msg.obj;

//                    pointSeries.resetData(newDataPoint);
////                    Toast.makeText(this, "Update Graph", Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "UPDATE GRAPH");
                    break;
                default:
                    super.handleMessage(msg);

            }
        }
    };

    /** Messenger we publish for Service to send messages back. */
    final Messenger mMessenger = new Messenger(handler);

    /** Class for interacting with the main interface of the service. */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize sensorConnection and textView
        textView = findViewById(R.id.mainText);
        sensorConnection = new SensorConnection(setUpBluetooth());

        if (sensorConnection.checkBLEAvailability(getApplicationContext()))
            textView.setText("True");
        else
            textView.setText("False");

        // Bind to the service
        // Using explicit intent to avoid trouble
        Intent intent = new Intent(this, com.example.blindspotdetection.SensorReceiverService.class);
        intent.putExtra("IP", "192.168.1.1");
        intent.putExtra("port", "8080");
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setMainMessenger();

        graph = (GraphView) findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 6)
//        });
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-2);
        graph.getViewport().setMaxX(2);

//        graph.getViewport().set
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(5);
        graph.getViewport().setScrollable(false); // disables horizontal scrolling
        graph.getViewport().setScrollableY(false); // disables vertical scrolling
        graph.getViewport().setScalable(false); // disables horizontal zooming and scrolling
        graph.getViewport().setScalableY(false); // disables vertical zooming and scrolling
        pointSeries = new PointsGraphSeries<DataPoint>();
        pointSeries.setShape(PointsGraphSeries.Shape.RECTANGLE);

        graph.addSeries(pointSeries);
//        graph.addSeries(series);

        setSound();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        // Unbind from the service
        if (mBound) {
            Message msg = Message.obtain();
            msg.what = SensorReceiverService.MSG_STOP_LISTENING;
            try {
                mService.send(msg);
            }
            catch (RemoteException e){
                Log.e(TAG, "Remote Exception when stop listening onDestroy");
            }
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    public void getMessage(View view){
        // Create and send a message to the service, using a supported 'what' value
        setMainMessenger();
        Message msg = Message.obtain();
        msg.what = SensorReceiverService.MSG_GET_TEST_MESSAGE;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote Exception in SET_MAIN_MESSENGER");
        }

    }

    public void getTestedObject(View view){
        // Create and send a message to the service, using a supported 'what' value
        setMainMessenger();
        Message msg = Message.obtain();
        msg.what = SensorReceiverService.MSG_GET_TEST_OBJECTS;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote Exception in SET_MAIN_MESSENGER");
        }

    }

    private void setMainMessenger(){
        if (!mBound) return;

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain();
        msg.what = SensorReceiverService.MSG_SET_MAIN_MESSENGER;
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote Exception in SET_MAIN_MESSENGER");
        }
    }

    public void startReceiver(View view){
        if (!mBound) return;

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain();
        if (!running) {
            msg.what = SensorReceiverService.MSG_START_LISTENING;
            running = true;
        }else {
            msg.what = SensorReceiverService.MSG_STOP_LISTENING;
            running = false;
        }
        // Create a CountDownTimer to make the SensorReceiver to report back every one minute.
//        msg.obj = (60000, 10000);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void setEnableService(View view){
        if (mBound) {
            Message msg = Message.obtain();
            msg.what = SensorReceiverService.MSG_STOP_LISTENING;
            try {
                mService.send(msg);
            }
            catch (RemoteException e){
                Log.e(TAG, "Remote Exception when stop listening onDestroy");
            }
            unbindService(mConnection);
            mBound = false;
            Intent intent = new Intent(this, com.example.blindspotdetection.SensorReceiverService.class);
            stopService(intent);
        }
        else{
            Intent intent = new Intent(this, com.example.blindspotdetection.SensorReceiverService.class);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            setMainMessenger();
        }
    }

    public void play(View view){
        if (player.isPlaying()){
            player.pause();
        }
        else{
            player.create(getApplicationContext(), R.raw.grandblue_test);
            player.start();
        }
    }
    private void setSound(){
        player = MediaPlayer.create(getApplicationContext(), R.raw.grandblue_test);
    }

    private BluetoothAdapter setUpBluetooth(){
        // Getting the device bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        // Check to see if bluetooth is enabled or not
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        return adapter;
    }
}
