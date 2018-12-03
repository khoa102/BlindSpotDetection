package com.example.blindspotdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigureActivity extends AppCompatActivity {

    private EditText angleTextbox;

    private Button updateAngleButton;

    private EditText minXTextbox;
    private EditText maxXTextbox;
    private EditText minYTextbox;
    private EditText maxYTextbox;

    private String TAG = "ConfigureActivity";
    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_activity);

        angleTextbox = findViewById(R.id.angleTextBox);
        updateAngleButton = findViewById(R.id.updateAngleButton);
        minXTextbox = findViewById(R.id.minXTextbox);
        maxXTextbox = findViewById(R.id.maxXTextbox);
        minYTextbox = findViewById(R.id.minYTextbox);
        maxYTextbox = findViewById(R.id.maxYTextbox);
    }

    public void setAngle(View view){
        // Assume that the text is always an integer.
        int updateAngle = Integer.parseInt(angleTextbox.getText().toString());
        Log.i(TAG, String.format("%d", updateAngle));
        if (updateAngle > 0 && updateAngle < 180){
            Intent i = new Intent();
            i.putExtra("new_angle", updateAngle);
            setResult(RESULT_OK, i);
            Log.i(TAG,"Valid Angle, sent to main.");
            finish();
        } else{
            Toast.makeText(getApplicationContext(), "Invalid Angle",Toast.LENGTH_SHORT);
            Log.i(TAG,"Invalid Angle.");
        }
    }

    public void setBoundary(View view){
        int min_x = Integer.parseInt(minXTextbox.getText().toString());
        int max_x = Integer.parseInt(maxXTextbox.getText().toString());
        int min_y = Integer.parseInt(minYTextbox.getText().toString());
        int max_y = Integer.parseInt(maxYTextbox.getText().toString());

        if (min_x >= max_x || min_y > max_y)
            Toast.makeText(getApplicationContext(), "Invalid Boundary",Toast.LENGTH_SHORT);
        else{
            Intent i = new Intent();
            i.putExtra("min_x", min_x);
            i.putExtra("max_x", max_x);
            i.putExtra("min_y", min_y);
            i.putExtra("max_y", max_y);
            setResult(RESULT_OK, i);
            Log.i(TAG,"Valid Angle, sent to main.");
            finish();
        }

    }

}
