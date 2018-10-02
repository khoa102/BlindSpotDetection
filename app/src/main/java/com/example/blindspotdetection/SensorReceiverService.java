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
    private String IP;
    private String port;
    private boolean isListening;

    /** A TAG for logging. */
    static final String TAG = "SensorReceiverService";

    /**  Command to the service to set Main Handler. */
    static final int MSG_SET_MAIN_MESSENGER = 1;
    /**  Command to the service to start listening for UDP packet.  */
    static final int MSG_START_LISTENING = 2;
    /**  Command to the service to stop the service. */
    static final int MSG_STOP_SERVICE = 3;


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
                        e.printStackTrace();
                    }

                    System.out.println("Message sent");
                    break;
                case MSG_START_LISTENING:
                    String result = "";
                    try {
                        socket = new DatagramSocket(4445);
                        socket.setSoTimeout(10000);
                    } catch (SocketException e){
                        e.printStackTrace();
                    }
                    running = true;
                    while (running) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {

                            System.out.println("Setting up socket. Waiting: ");
                            socket.receive(packet);
                            System.out.println("Packet received!");
                            String received = new String(packet.getData(), 0, packet.getLength());

                            InetAddress address = packet.getAddress();
                            int port = packet.getPort();

                            packet = new DatagramPacket(buf, buf.length, address, port);


                            if (received.equals("end") || mServiceHandler.hasMessages(MSG_STOP_SERVICE)) {
                                running = false;
                                socket.close();
                                continue;
                            }
                            System.out.println("Toasting!");
                            Toast.makeText(getApplicationContext(), "Get Packet", Toast.LENGTH_SHORT).show();
//                            socket.send(packet);
                            message = new Message();
                            message.obj = received;
                            System.out.println("Sending Message!");
                            mainClient.send(message);
                        }  catch (IOException e) {
                            e.printStackTrace();
                        } catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }
//                    socket.close();
                    break;
//                case MSG_STOP_SERVICE:
//                    // Stop the service using the startId, so that we don't stop
//                    // the service in the middle of handling another job
//                    stopSelf(msg.arg1);
            }
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("Radar Sensor Receiver",
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

        // Check to make sure that port and IP are included in the intent
        if (!intent.hasExtra("port") || !intent.hasExtra("IP")){
            stopSelf();
            return START_NOT_STICKY;
        }
        else {
            IP = intent.getStringExtra("IP");
            port = intent.getStringExtra("port");
        }

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        System.out.println("Service start normally");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Check to make sure that port and IP are included in the intent
        if (!intent.hasExtra("port") || !intent.hasExtra("IP")){
            stopSelf();
            return null;
        }
        else {
            IP = intent.getStringExtra("IP");
            port = intent.getStringExtra("port");
        }
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}