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
import java.net.Socket;
import java.net.UnknownHostException;

//public class SensorReceiverRunnable implements Runnable {
//    private String IP;
//    private String port;
//    private Handler mainHandler;
//
//    public SensorReceiverRunnable(String IP, String port, Handler handler){
//        this.IP = IP;
//        this.port = port;
//        mainHandler = handler;
//    }
//
//    @Override
//    public void run() {
//        String result = "Hello for now";
////        try {
////            //Create a client socket and define internet address and the port of the server
////            Socket socket = new Socket(IP, Integer.parseInt(port));
////
////            //Get the input stream of the client socket
////            InputStream is = socket.getInputStream();
////            //Get the output stream of the client socket
////            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
////
////            //Write data to the output stream of the client socket
////            //out.println(params[2]);
////
////            //Buffer the data coming from the input stream
////            BufferedReader br = new BufferedReader(new InputStreamReader(is));
////            //Read data in the input buffer
////            result = br.readLine();
////            //Close the client socket
////            socket.close();
////        } catch (NumberFormatException e) {
////            e.printStackTrace();
////        } catch (UnknownHostException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//        Message message = mainHandler.obtainMessage();
//        message.obj = "Receive from Runnable";
//        mainHandler.sendMessage(message);
//    }
//
//}

public class SensorReceiverService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private String IP;
    private String port;

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
                    Message message = new Message();
                    message.obj = "Receive from Runnable";
                    try {
                        mainClient.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Message sent");
                    break;
                case MSG_START_LISTENING:
                    String result = "";
//                    try {
//                        //Create a client socket and define internet address and the port of the server
//                        Socket socket = new Socket(IP, Integer.parseInt(port));
//
//                        //Get the input stream of the client socket
//                        InputStream is = socket.getInputStream();
//                        //Get the output stream of the client socket
//                        //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//
//                        //Write data to the output stream of the client socket
//                        //out.println(params[2]);
//
//                        //Buffer the data coming from the input stream
//                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                        //Read data in the input buffer
//                        result = br.readLine();
//                        //Close the client socket
//                        socket.close();
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                    } catch (UnknownHostException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    break;
                case MSG_STOP_SERVICE:
                    // Stop the service using the startId, so that we don't stop
                    // the service in the middle of handling another job
                    stopSelf(msg.arg1);
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