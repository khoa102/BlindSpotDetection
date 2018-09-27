package com.example.blindspotdetection;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.example.blindspotdetection.R;


public class SensorConnection{
    private BluetoothAdapter mBluetoothAdapter;
    public SensorConnection(BluetoothAdapter adapter){
        mBluetoothAdapter = adapter;

    }

    public Boolean checkBLEAvailability(Context context){
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(context, "BLE not supported", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            Toast.makeText(context, "BLE is supported", Toast.LENGTH_SHORT).show();
            return true;
        }
    }


}
