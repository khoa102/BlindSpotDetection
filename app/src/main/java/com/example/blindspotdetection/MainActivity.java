package com.example.blindspotdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends AppCompatActivity {
    private SensorConnection sensorConnection;
    private final static int REQUEST_ENABLE_BT = 1;
    private TextView textView;


    /** Messenger for communicating with the service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /** Flag to remember the status of the service */
    private boolean running = false;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            // Suppose to execute visualizer
            System.out.println(msg.obj);
            textView.setText(textView.getText() + msg.obj.toString());
        }
    };

    /** Target we publish for clients to send messages to IncomingHandler. */
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
    }

    public void runThread(View view){
//        new Thread(new SensorReceiverRunnable("192.168.1.1", "8080", handler)).run();
//        if (mBound)
//            textView.setText(textView.getText() + "Service is still bound");
//        else
//            textView.setText(textView.getText() + "Service is not bound");
        setMainMessenger();
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
            e.printStackTrace();
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
            msg.what = SensorReceiverService.MSG_STOP_SERVICE;
            running = false;
        }
        // Create a CountDownTimer to make the SensorReceiver to report back every one minute.
//        msg.obj = (60000, 10000);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        // Using explicit intent to avoid trouble
        Intent intent = new Intent(this, com.example.blindspotdetection.SensorReceiverService.class);
        intent.putExtra("IP", "192.168.1.1");
        intent.putExtra("port", "8080");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setMainMessenger();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}
