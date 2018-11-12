package com.example.blindspotdetection;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.example.blindspotdetection.R;

/**
 *  @deprecated A Class that is used to set up connection. It is currently deprecated.
 */
public class SensorConnection{
    private BluetoothAdapter mBluetoothAdapter;
    public SensorConnection(BluetoothAdapter adapter){
        mBluetoothAdapter = adapter;

    }

    /**
     *  Check to see if BLE is available on the current device or not.
     * @param context   Current Application context
     * @return  true if BLE is available and otherwise false.
     */
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
