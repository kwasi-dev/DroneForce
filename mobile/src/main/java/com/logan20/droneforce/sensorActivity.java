package com.logan20.droneforce;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.logan20.droneforceshared.sensorClass;

public class sensorActivity extends AppCompatActivity {
    sensorClass sensor;
    TextView accelTxt, gyroTxt, linAccelTxt, currActionTxt;
    private final long REFRESH_TIME = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        init();
        updateTextThread();
    }

    @Override
    protected void onPause(){
        sensor.unregisterListeners();
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        sensor.registerListeners();
    }

    private void init() {
        sensor = new sensorClass((SensorManager)this.getSystemService(Context.SENSOR_SERVICE));
        accelTxt = (TextView)findViewById(R.id.idAccelTxt);
        gyroTxt = (TextView)findViewById(R.id.idGyroTxt);
        linAccelTxt = (TextView)findViewById(R.id.idLinAccelTxt);
        currActionTxt = (TextView)findViewById(R.id.idCurrentActionTxt);
    }

    private void updateTextThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    try{
                        updateText();
                        Thread.sleep(REFRESH_TIME);
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
    private void updateText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double[] acc = sensor.getCurrentSensorReading("accelerometer");
                double[] gyro = sensor.getCurrentSensorReading("gyroscope");

                accelTxt.setText("X:" + acc[0] + " Y:" + acc[1] + " Z:" + acc[2]);
                gyroTxt.setText("X:" + gyro[0] + " Y:" + gyro[1] + " Z:" + gyro[2]);

            }
        });
    }

    public void zeroAxis(View v){
        sensor.zeroAxis(0);
        sensor.zeroAxis(1);
        sensor.zeroAxis(2);
    }
}
