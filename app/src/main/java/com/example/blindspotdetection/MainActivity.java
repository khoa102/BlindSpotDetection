package com.example.blindspotdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;


/**
 * Main Activity for the app.
 */
public class MainActivity extends AppCompatActivity {
//    /** Variable for setting up connection*/
//    private SensorConnection sensorConnection;
    private final static int REQUEST_ENABLE_BT = 1;

    /** View that is used to put information on the screen */
    private TextView textView;
    GraphView graph;

    /** A Point Series to represent the detected objects. */
    PointsGraphSeries<DataPoint> pointSeries;

    /** A list of detected data in the current frame. */
    DetectedObject[] objects;

    /** Media Player object to play detecting object sound.. */
    MediaPlayer detectedPlayer;

    /** Media Player object to play object leaves detection boundary sound.. */
    MediaPlayer leavePlayer;

    /** SensorProcessor to process data buffer from sensor.*/
    SensorProcessor sensorprocessor = new SensorProcessor();

    /** A TAG for logging. */
    static final String TAG = "MainActivity";

    /** Messenger of the Service to send message to the Service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /** Flag to remember the status of the service */
    private boolean running = false;

    /** Flag indicating whether we have detected an object inside the detection boundary or not*/
    private boolean isDetected = false;

    /** Variable to hold time that last detected object is in boundary */
    private long lastDetectedTime;

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
                    textView.setText(String.format("%s%s", textView.getText(), msg.obj.toString()));
                    break;
                case MSG_DETECTED_OBJECT:
                    byte buffer[] = (byte[]) msg.obj;
                    // An object to read the receive data packet and process it
                    sensorprocessor.loadPacket(buffer);
                    boolean result = sensorprocessor.processData();
                    if (result){
                        textView.setText(String.format("%s\n\n Successfully process data frame \n", textView.getText()));
                        objects = sensorprocessor.getDetectedObjects();
                        for (DetectedObject object1 : objects) {
                            textView.setText(String.format("%s%s", textView.getText(), object1.toString()));
                        }
                        textView.setText(String.format("%s\n", textView.getText()));

                        DataPoint newDataPoint[] = new DataPoint[objects.length];
                        int index = 0;
                        for (DetectedObject object : objects){
                            newDataPoint[index] = new DataPoint(object.getX(), object.getY());
                            index++;
                        }
                        pointSeries.resetData(newDataPoint);
                    }
                    else {
                        textView.setText(String.format("%s\n\n Failed process data frame \n", textView.getText()));
                    }

                    if (isDetected == true){
                        // The object should not be detected for as least 2 seconds???
                        if (!sensorprocessor.getIsInBound() && System.currentTimeMillis() - lastDetectedTime > 200){
                            isDetected = false;
                            lastDetectedTime = 0;
                            leavePlayer.start();
                        }else if (sensorprocessor.getIsInBound()){
                            lastDetectedTime = System.currentTimeMillis();
                        }
                    }else {
                        if (sensorprocessor.getIsInBound()){
                            isDetected = true;
                            lastDetectedTime = System.currentTimeMillis();
                            detectedPlayer.start();
                        }
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
//        sensorConnection = new SensorConnection(setUpBluetooth());

//        if (sensorConnection.checkBLEAvailability(getApplicationContext()))
//            textView.setText(R.string.true_string);
//        else
//            textView.setText(R.string.false_string);

        // Bind to the service
        // Using explicit intent to avoid trouble
        Intent intent = new Intent(this, com.example.blindspotdetection.SensorReceiverService.class);
        intent.putExtra("IP", "192.168.1.1");
        intent.putExtra("port", "8080");
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setMainMessenger();

        // Setting up the graph view to display dectected objects
        graph = findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-2);
        graph.getViewport().setMaxX(2);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(5);
        graph.getViewport().setScrollable(false); // disables horizontal scrolling
        graph.getViewport().setScrollableY(false); // disables vertical scrolling
        graph.getViewport().setScalable(false); // disables horizontal zooming and scrolling
        graph.getViewport().setScalableY(false); // disables vertical zooming and scrolling
        pointSeries = new PointsGraphSeries<>();
        pointSeries.setShape(PointsGraphSeries.Shape.RECTANGLE);

        graph.addSeries(pointSeries);

        sensorprocessor.setDetectionBound(-1, 1, 0, 3);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_configure:
                startActivity(new Intent(this, ConfigureActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        if (detectedPlayer.isPlaying()){
            detectedPlayer.pause();
        }
        else{
            detectedPlayer.start();
        }
    }
    private void setSound(){
        detectedPlayer = MediaPlayer.create(getApplicationContext(), R.raw.grandblue_test);
    }

    private BluetoothAdapter setUpBluetooth(){
        // Getting the device bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = null;
        if (bluetoothManager != null) {
            adapter = bluetoothManager.getAdapter();
        }

        // Check to see if bluetooth is enabled or not
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        return adapter;
    }
}
