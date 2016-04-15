package com.logan20.droneforceshared;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by kwasi on 3/7/2016.
 * Hello
 */
public class sensorClass implements SensorEventListener{
    SensorManager sm;  //Sensor manager
    Sensor sGyro, sAccel; //Gyroscope and Accelerometer sensors
    float[][] readings; //0th row is accelerometer's current reading
                        //1st row is gyroscope's current reading
                        //2th row is peak accelerometer reading
                        //3th row is min accelerometer reading
                        //4th row is peak accelerometer time
                        //5th row is min accelerometer time
                        //6th row is velocity (vector quantity) in each axis
    private final float ACCEL_DELTA = 1.0f;//minimum change needed before changing of values on accelerometer
    private final float ACCEL_ZERO_POINT = 0.5f;//noise level eliminator for accelerometer
    private final float GYRO_DELTA = 0.1f; //minimum change needed before changing of values on gyroscope
    private final float GYRO_ZERO_POINT = 0.05f; //noise level eliminator for gyroscope
    private final float MIN_MOTION_DELTA=8.0f;//minimum distance between peaks and trough in order to record an axis' speed
    private final long MIN_MOTION_TIME = 200; //minimum length of time user must move on a particular axis before reading is recorded
    private final float STRONG_MOVEMENT = 20.0f;


    private void setSensorManager(SensorManager x){
        sm = x;
    }

    private void initArr(){
        readings = new float[7][3]; //initialise the readings matrix
    }

    public sensorClass(SensorManager x){
        setSensorManager(x);//set the sensor manager to the manager passed in from the main class
        initArr();//initialise the arrays that hold data
        setSensors(); //sets the sensors
        registerListeners(); //register the sensors
    }

    public void registerListeners(){
        sm.registerListener(this, sGyro, SensorManager.SENSOR_DELAY_FASTEST); //registers the listeners for the gyroscope
        sm.registerListener(this, sAccel, SensorManager.SENSOR_DELAY_FASTEST);//register the listeners for the accelerometer
    }
    public double[] getCurrentSensorReading(String sensor){//gets reading of a particular sensor
        int row=0; //row of the array being accessed (defaulted to accelerometer)
        if (sensor.toLowerCase().equals("gyroscope"))  row=1; //(if gyroscope reading is the one requested, shift the row

        double[] arr=new double[3]; //new array to hold all data(formatted to 2dp)
        for (int a=0;a<3;a++){
            arr[a]=Double.valueOf(new DecimalFormat("#.##").format(readings[row][a]));
        }
        return arr;
    }

    public void unregisterListeners(){ //unregister listeners to save battery when sensors not in use
        sm.unregisterListener(this, sGyro);
        sm.unregisterListener(this, sAccel);
    }

    private void setSensors(){//sets the sensors
        sGyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sAccel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        updateSensors(event);
        resetPeakMin();
    }

    private void resetPeakMin() {//needed to reset the values of each axis so that new readings can be obtained, values cannot be defaulted to 0
        for (int a=0;a<3;a++){
            if (readings[2][a]==0.0f) readings[2][a]=readings[0][a];
            if (readings[3][a]==0.0f) readings[3][a]=readings[0][a];
        }
    }


    private void updateSensors(SensorEvent event){
        boolean t=false;//variable to see if any peak / min values was updated

        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){//for the accelerometer
            for (int a=0;a<3;a++){//for each of the x, y and z axis
                if (Math.abs(Math.abs(readings[0][a])-Math.abs(event.values[a]))>ACCEL_DELTA){//once the change in the readings is significant
                    readings[0][a]=Math.abs(event.values[a])<ACCEL_ZERO_POINT?0.00f:event.values[a];//set the value appropriately, (0 if the reading is approximately 0 else current sensor value
                    if (event.values[a]>readings[2][a]){//if the new value is greater than the peak
                        readings[2][a]=event.values[a]; //update the peak accelerometer reading
                        readings[4][a]=Math.abs(System.currentTimeMillis())%10000000;//update the time that the reading was taken
                    }
                    if (event.values[a]<readings[3][a]){//if the new value is smaller than the min
                        readings[3][a]=event.values[a];//update the min accelerometer reading
                        readings[5][a]=Math.abs(System.currentTimeMillis())%10000000;//update the time that the reading was taken
                    }
                    t=true;//set the flag that an update of peak / min was done
                }
            }
            if (t){//once an update of peak / min was done
                for (int a='x';a<='z';a++){//for each of the x,y and z axis
                    float motionMagnitude =readings[2][a-'x']-readings[3][a-'x'];
                    if (motionMagnitude>MIN_MOTION_DELTA){//once the movement's significant enough
                        if (Math.abs(Math.abs(readings[4][a-'x'])-Math.abs(readings[5][a-'x']))>MIN_MOTION_TIME || motionMagnitude>STRONG_MOVEMENT){//once the time difference is significant enough or movement is a strong movement
                            readings[6][a-'x']=readings[2][a-'x']-readings[3][a-'x'];//set speed
                            readings[6][a-'x']=readings[4][a-'x']<readings[5][a-'x']?readings[6][a-'x']:-readings[6][a-'x'];//set direction
                            //Log.d(String.valueOf((char)a).toUpperCase()+" axis","Speed: "+readings[6][a-'x']);
                        }

                    }
                }
            }
        }
        else if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){//for the gyroscope
            for (int a=0;a<3;a++){//for each of the axis
                if (Math.abs(Math.abs(readings[1][a])-Math.abs(event.values[a]))>GYRO_DELTA){//once the movement is significant enough
                    readings[1][a]=Math.abs(event.values[a])<GYRO_ZERO_POINT?0.00f:event.values[a];//update readings
                    t=true;//set the flag
                }
            }
            if (t){//once the flag has been triggered
                //need to finish code out gyroscope section...
                //accelerometer is used to give direction
                //rotational information from gyroscope will be used for when the user shakes his hand, that will stop all movement of the drone
                //
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void zeroAxis(int i) {
        for (int a=2;a<7;a++){
            readings[a][i]=0.0f;//reset the peak, min, peaktime, mintime, and speed of the ith axis
        }
    }

    public float[] getOrientationRelativeSpeedReadings() {
        return readings[6];
    }
}
