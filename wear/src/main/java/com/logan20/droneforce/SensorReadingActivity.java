package com.logan20.droneforce;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


import com.logan20.droneforceshared.droneforceshared.SensorClass;
import com.logan20.droneforceshared.droneforceshared.SensorInterface;

import java.util.Locale;

public class SensorReadingActivity extends WearableActivity implements SensorInterface {
    private TextView sensorText;
    private SensorClass sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_reading);
        sensorText = (TextView) findViewById(R.id.tv_sensorText);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sensor=new SensorClass((SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE),this);
    }

    @Override
    protected void onPause() {
        sensor.unregisterListeners();
        sensor=null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensor==null){
            sensor=new SensorClass((SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE),this);
        }
    }


    @Override
    public void onDataChanged(final SensorClass.Direction direction, final float speed) {
        sensorText.post(new Runnable() {
            @Override
            public void run() {
                sensorText.setText("Moving with a speed of "+speed+" with direction "+direction.name());
            }
        });

    }

}
