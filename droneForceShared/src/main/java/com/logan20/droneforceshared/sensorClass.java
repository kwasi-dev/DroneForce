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
    float[][] readings; //0th row is accelerometer
                        //1st row is gyroscope
                        //2th row is peak accelerometer
                        //3th row is min accelerometer
                        //4th row is peak accelerometer time
                        //5th row is min accelerometer time
                        //6th row is vector quantity in each axis
    String[] headings;
    String[] orientations;
    String[] movements;
    private final float ACCEL_DELTA = 1.0f;
    private final float ACCEL_ZERO_POINT = 0.5f;
    private final float GYRO_DELTA = 0.1f;
    private final float GYRO_ZERO_POINT = 0.05f;
    private final float MIN_MOTION_DELTA=8.0f;

    private void setSensorManager(SensorManager x){
        sm = x;
    }
    private void initArr(){
        readings = new float[8][3]; //initialise the readings matrix
        headings = new String[]{"Accelerometer","Gyroscope","Lin Accel","Gravity"};
        orientations = new String[]{"Normal Landscape", "Reverse Landscape",
                                    "Normal Portrait", "Reverse Portrait",
                                    "Screen Facing Up", "Screen Facing Down"};
        movements = new String[]{   "Moving Left","Moving Right","Moving Away From Body",
                                    "Moving Towards Body", "Raised into the air",
                                    "Dropping towards the floor"};
    }
    public sensorClass(SensorManager x){
        setSensorManager(x);//set the sensor manager to the manager passed in from the main class
        initArr();//initialise the arrays that hold data
        setSensors(); //sets the sensors
        registerListeners(); //register the sensors

    }

    public void registerListeners(){
        sm.registerListener(this, sGyro, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(this, sAccel, SensorManager.SENSOR_DELAY_FASTEST);
    }
    public double[] getCurrentSensorReading(String sensor){
        int row=0; //row of the array being accessed
        double[] arr=new double[3];

        if (sensor.toLowerCase().equals("gyroscope"))  row=1;
        else if (sensor.toLowerCase().equals("linear acceleration"))  row=2;
        for (int a=0;a<3;a++){
            arr[a]=Double.valueOf(new DecimalFormat("#.##").format(readings[row][a]));
        }
        return arr;
    }

    public void unregisterListeners(){
        sm.unregisterListener(this, sGyro);
        sm.unregisterListener(this, sAccel);
    }

    private void setSensors(){
        sGyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sAccel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        updateSensors(event);
        resetPeakMin();
        //updateMotionDetector();
    }

    private void resetPeakMin() {
        for (int a=0;a<3;a++){
            if (readings[2][a]==0.0f) readings[2][a]=readings[0][a];
            if (readings[3][a]==0.0f) readings[3][a]=readings[0][a];
        }
    }


    private void updateSensors(SensorEvent event){
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            boolean t=false;
            for (int a=0;a<3;a++){
                if (Math.abs(Math.abs(readings[0][a])-Math.abs(event.values[a]))>ACCEL_DELTA){
                    readings[0][a]=Math.abs(event.values[a])<ACCEL_ZERO_POINT?0.00f:event.values[a];
                    if (event.values[a]>readings[2][a]){
                        readings[2][a]=event.values[a];
                        readings[4][a]=Math.abs(System.currentTimeMillis())%10000000;
                    }
                    if (event.values[a]<readings[3][a]){
                        readings[3][a]=event.values[a];
                        readings[5][a]=Math.abs(System.currentTimeMillis())%10000000;
                    }
                    t=true;
                }
            }
            if (t){
                for (int a='x';a<='z';a++){
                    //check and see if the movement's significant enough
                    if (readings[2][a-'x']-readings[3][a-'x']>MIN_MOTION_DELTA){
                        //get speed
                        readings[6][a-'x']=readings[2][a-'x']-readings[3][a-'x'];

                        //get direction
                        readings[6][a-'x']=readings[4][a-'x']<readings[5][a-'x']?readings[6][a-'x']:-readings[6][a-'x'];

                        Log.i(Character.valueOf((char)a)+" MAX: "," "+readings[2][a-'x']+"\t at time: "+String.format("%.0f",readings[4][a-'x']));
                        Log.i(Character.valueOf((char)a)+" MIN: "," "+readings[3][a-'x']+"\t at time: "+String.format("%.0f",readings[5][a - 'x']));
                        Log.i(Character.valueOf((char)a)+" SPEED",String.format("%.2f",readings[6][a-'x']));
                    }
                }
            }
        }
        else if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            boolean t=false;
            for (int a=0;a<3;a++){
                if (Math.abs(Math.abs(readings[1][a])-Math.abs(event.values[a]))>GYRO_DELTA){
                    readings[1][a]=Math.abs(event.values[a])<GYRO_ZERO_POINT?0.00f:event.values[a];
                    t=true;
                }
            }
            if (t){
                Log.d("GYROSCOPE",String.format("%.3f\t%.3f\t%.3f\t%d",readings[1][0],readings[1][1],readings[1][2],System.currentTimeMillis()%10000));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getOrientation(){
        //0= normal landscape, 1=reverse landscape
        //2=normal portrait, 3=reverse portrait
        //4 = screen faces up, 5 = screen faces down
        if (Math.abs(readings[0][0])>Math.abs(readings[0][1])&&Math.abs(readings[0][0])>Math.abs(readings[0][2]))
            return readings[0][0]>0?0:1;
        else if (Math.abs(readings[0][1])>Math.abs(readings[0][0])&&Math.abs(readings[0][1])>Math.abs(readings[0][2]))
            return readings[0][1]>0?2:3;
        return readings[0][2]>0?4:5;
    }
    public String getActionString(){
        return "";
    }

    public void zeroAxis(int i) {
        //reset delta and times
        for (int a=2;a<7;a++){
            readings[a][i]=0.0f;
        }
    }
}
