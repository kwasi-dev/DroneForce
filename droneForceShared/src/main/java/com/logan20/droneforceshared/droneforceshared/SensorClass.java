package com.logan20.droneforceshared.droneforceshared;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * Created by kwasi on 3/7/2016.
 * CODE MOD ON 13-9-16 applies a low pass filter to filter out data hence increasing efficiency
 * and reduces processor cycles to calculate results.
 * Noted: Memory use is less that original
 */

public class SensorClass implements SensorEventListener{
    private final SensorInterface notifier;
    private SensorManager sm;               //Sensor manager
    private Sensor  sAccel;                 //Accelerometer sensors

    public enum Direction {LEFT, RIGHT, UP, DOWN, FORWARD, BACKWARD, UP_RIGHT, UP_LEFT, DOWN_RIGHT ,DOWN_LEFT}; //possible states

    private volatile float[][] readings;    //0th row is gravity reading
                                            //1st row is filtered values

    private final float ALPHA = 0.8f;       //alpha constant for app, may need fiddling
    private final float ZERO = 6.0f;        //all speed movemements must be stronger than this value to trigger the notifier
    private final long PEAKTIMER = 500;     //amount of time to wait for the negative peak of a motion

    private float speed;
    private long peakTime;
    private Direction currDirection;

    public SensorClass(SensorManager x, SensorInterface notif){
        this.notifier = notif;
        setSensorManager(x);//set the sensor manager to the manager passed in from the main class
        initArr();//initialise the arrays that hold data
        setSensors(); //sets the sensors;
        registerListeners();
    }

    private void setSensorManager(SensorManager x){
        sm = x;
    }

    private void initArr(){
        readings = new float[2][3]; //initialise the readings matrix
    }


    public void registerListeners(){
        sm.registerListener(this, sAccel, SensorManager.SENSOR_DELAY_UI);//register the listeners for the accelerometer
    }
    public void unregisterListeners(){ //unregister listeners to save battery when sensors not in use
        sm.unregisterListener(this, sAccel);
    }

    private void setSensors(){//sets the sensors
        sAccel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //isolate gravity with low pass filter
        for (int a=0;a<3;a++){
            readings[0][a] = ALPHA * readings[0][a] + (1-ALPHA) * event.values[a];
        }
        //remove gravity with a high pass filter
        for (int a=0;a<3;a++){
            readings[1][a]= event.values[a]-readings[0][a];
        }
        //set the speed using vector equation
        float tempSpeed = 0;
        for(float x:readings[1]){
            tempSpeed+=Math.pow(x,2);
        }
        tempSpeed = (float) Math.sqrt(tempSpeed);

        // readings should only be taken into consideration if there is a more significant motion recorded
        // or enough time has passed between the last movement to ensure that the opposite peak doesn't
        // affect the reading and the motion made was significant enough
        long currTime = System.currentTimeMillis();

        if ((tempSpeed > speed) || ((currTime - peakTime > PEAKTIMER)&&(tempSpeed>ZERO))){
            //update speed
            speed = tempSpeed;
            //update time
            peakTime = currTime;

            //update direction (NEED TO MODIFY IN ORDER TO TAKE INTO CONSIDERATION DIAGONAL MOVEMENT)
            float absX = Math.abs(readings[1][0]);
            float absY = Math.abs(readings[1][1]);
            float absZ = Math.abs(readings[1][2]);

            if (absX > absY && absX > absZ){
                currDirection = readings[1][0]<0?Direction.FORWARD:Direction.BACKWARD;
            }
            else if (absY>absX && absY > absZ){
                currDirection = readings[1][1]<0?Direction.LEFT:Direction.RIGHT;
            }
            else{
                currDirection = readings[1][2]<0?Direction.UP:Direction.DOWN;
            }
            onReadingUpdate();
        }
    }

    private void onReadingUpdate() {
        if (notifier!=null){
            notifier.onDataChanged(currDirection,speed);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}