package com.example.blindspotdetection;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *  Android Service that will constantly listen to UDP packet even when phone is asleep.
 */
public class SensorReceiverService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    /** A variable to keep control of the thread for the service. */
    private HandlerThread thread;

    /** A TAG for logging. */
    private static final String TAG = "SensorReceiverService";

    /**  Command to the service to set Main Handler. */
    public static final int MSG_SET_MAIN_MESSENGER = 1;
    /**  Command to the service to start listening for UDP packet.  */
    public static final int MSG_START_LISTENING = 2;
    /**  Command to the service to start listening for UDP packet.  */
    public static final int MSG_STOP_LISTENING = 3;
    /**  Command to the service to stop the service. */
    public static final int MSG_STOP_SERVICE = 4;
    /**  Command to the service to get a test message from the serivce. */
    public static final int MSG_GET_TEST_MESSAGE = 5;
    /**  Command to the service to get a test Arrays of DetectObject from the serivce. */
    public static final int MSG_GET_TEST_OBJECTS = 6;

    /** An object to read the receive data packet and process it. */
    SensorProcessor sensorprocessor = new SensorProcessor();

    /** Keeps track of main clients. */
    private Messenger mainClient;

    /**  Target we publish for clients to send messages to Service Handler. */
    private Messenger mMessenger;
    private Message message;

    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    private int countFrame = 0;

    /** Handler that receives messages from the thread */
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_SET_MAIN_MESSENGER:
                    System.out.println("receive message");

                    mainClient = msg.replyTo;
                    break;
                case MSG_START_LISTENING:
                    boolean running = true;
                    try {
                        socket = new DatagramSocket(4445);
                        socket.setSoTimeout(10000);
                    } catch (SocketException e){
                        Log.e(TAG, "Socket Exception");
                    }
                    while (running) {
//                        buf = new byte[1500];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {

                            System.out.println("Setting up socket. Waiting: ");
                            socket.receive(packet);
                            countFrame ++;
                            System.out.println("Packet received!");
//                            String result = new String(packet.getData(), 0, packet.getLength());
//                            InetAddress address = packet.getAddress();
//                            int port = packet.getPort();
//
//                            packet = new DatagramPacket(buf, buf.length, address, port);
//                            socket.send(packet);

//                            sensorprocessor.loadPacket(packet);
//                            // If packet can't be read, skip this packet.
//                            if (!sensorprocessor.processData()) continue;
//                            System.out.print("SENDING DATA TO MAIN");
                            byte[] result = packet.getData();
                            for (int i = 0; i< 256; i ++){
//                                Log.i(TAG,  String.format("%02X ",result[i]));
                                System.out.print(String.format("%02X ",result[i]));
                            }

                            message = Message.obtain();
                            message.obj = packet.getData();
//                            message.obj = result;
                            message.what = MainActivity.MSG_DETECTED_OBJECT;
//                            message.obj = sensorprocessor.getDetectedObjects();
                            mainClient.send(message);
                        }  catch (IOException e) {
                            Log.e(TAG, "IO Exception");
                        } catch (RemoteException e){
                            Log.e(TAG, "Remote Exception");
                        }

                        if (mServiceHandler.hasMessages(MSG_STOP_LISTENING)){
                            running = false;
                            socket.close();
                        }
                    }
                    break;
                case MSG_STOP_LISTENING:
                    break;
                case MSG_STOP_SERVICE:
//                    // Stop the service using the startId, so that we don't stop
//                    // the service in the middle of handling another job
                    System.out.println("Stopping service");
                    stopSelf(msg.arg1);
                case MSG_GET_TEST_MESSAGE:
                    message = new Message();
                    message.what = MainActivity.MSG_GET_TEST_MESSAGE;
                    message.obj = "Receive from Service. Count Frame: " + countFrame;
                    try {
                        mainClient.send(message);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Remote Exception while sending message back to main.");
                    }

                    System.out.println("Message sent");
                    break;
                case MSG_GET_TEST_OBJECTS:
                    message = new Message();
                    message.what = MainActivity.MSG_DETECTED_OBJECT;
                    DetectedObject[] detectedObjects = new DetectedObject[4];
                    detectedObjects[0] = new DetectedObject(1, 0, 0, 1, 1, 1);
                    detectedObjects[1] = new DetectedObject(1.5, 0, 0, 1, 3, 0.5);
                    detectedObjects[2] = new DetectedObject(2, 0, 0, 2, 1, 3);
                    detectedObjects[3] = new DetectedObject(3, 0, 0, 3, 2, 2);
                    message.obj = detectedObjects;
                    try {
                        mainClient.send(message);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Remote Exception while sending message back to main.");
                    }
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        thread = new HandlerThread("Radar Sensor Receiver",
                Process.THREAD_PRIORITY_DEFAULT);
        thread.start();


        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mMessenger = new Messenger(mServiceHandler);

        // Set boundary of sensorProcessor
        sensorprocessor.setDetectionBound(-1, 1, 0, 5);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "unbinding", Toast.LENGTH_SHORT).show();
        if (socket != null) {
            if (!socket.isClosed()) socket.close();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        if (socket != null) {
            if (!socket.isClosed()) socket.close();
        }
    }
}