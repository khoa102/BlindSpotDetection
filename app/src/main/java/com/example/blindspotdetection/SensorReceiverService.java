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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class SensorReceiverService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    /** A variable to keep control of the thread for the service. */
    private HandlerThread thread;

    /** A TAG for logging. */
    static final String TAG = "SensorReceiverService";

    /**  Command to the service to set Main Handler. */
    static final int MSG_SET_MAIN_MESSENGER = 1;
    /**  Command to the service to start listening for UDP packet.  */
    static final int MSG_START_LISTENING = 2;
    /**  Command to the service to start listening for UDP packet.  */
    static final int MSG_STOP_LISTENING = 3;
    /**  Command to the service to stop the service. */
    static final int MSG_STOP_SERVICE = 4;


    /** Keeps track of main clients. */
    Messenger mainClient;
    /**  Target we publish for clients to send messages to Service Handler. */
    Messenger mMessenger;
    Message message;

    private DatagramSocket socket;
    private boolean running = false;
    private byte[] buf = new byte[1025];

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
                    message = new Message();
                    message.obj = "Receive from Service";
                    try {
                        mainClient.send(message);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Remote Exception while sending message back to main.");
                    }

                    System.out.println("Message sent");
                    break;
                case MSG_START_LISTENING:
                    try {
                        socket = new DatagramSocket(4445);
                        socket.setSoTimeout(1000);
                    } catch (SocketException e){
                        Log.e(TAG, "Socket Exception");
                    }
                    running = true;
                    while (running) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {

                            System.out.println("Setting up socket. Waiting: ");
                            socket.receive(packet);
                            System.out.println("Packet received!");
                            String received = new String(packet.getData(), 0, packet.getLength());

//                            InetAddress address = packet.getAddress();
//                            int port = packet.getPort();
//
//                            packet = new DatagramPacket(buf, buf.length, address, port);
//                            socket.send(packet);
                            if (received.equals("end")){
                                running = false;
                                socket.close();
                            }
                            Toast.makeText(getApplicationContext(), "Receive Packet", Toast.LENGTH_SHORT).show();

                            message = new Message();
                            message.obj = received;
                            System.out.println("Sending Message!");
                            mainClient.send(message);
                        }  catch (IOException e) {
//                            e.printStackTrace();
                            Log.e(TAG, "IO Exception");
                        } catch (RemoteException e){
//                            e.printStackTrace();
                            Log.e(TAG, "Remote Exception");
                        }
                        if (mServiceHandler.hasMessages(MSG_STOP_LISTENING)){
                            running = false;
                            socket.close();
                        }
                    }
                    break;
                case MSG_STOP_SERVICE:
//                    // Stop the service using the startId, so that we don't stop
//                    // the service in the middle of handling another job
                    stopSelf(msg.arg1);
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
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();


        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mMessenger = new Messenger(mServiceHandler);
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
        if (!socket.isClosed()) socket.close();

        return true;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}