package com.logan20.droneforceshared;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by kwasi on 3/7/2016.
 */
public class sensorClass implements SensorEventListener{
    SensorManager sm;  //Sensor manager
    Sensor sGyro, sAccel; //Gyroscope and Accelerometer sensors
    float[][] readings; //0th row is accelerometer
                        //1st row is gyroscope
                        //2nd row is linear acceleration
                        //3rd row is gravity
    String[] headings;
    String[] orientations;
    String[] movements;
    float alpha =0.4f;
    final float ACTION_THRESHOLD = 0.3f;
    final float GYRO_ACTION_THRESHOLD = 1.5f;
    final float ACCELEROMETER_NOISE_THRESHOLD = 0.2f;
    int currMvmnt;

    private void setSensorManager(SensorManager x){
        sm = x;
    }
    private void initArr(){
        readings = new float[5][3]; //initialise the readings matrix
        headings = new String[]{"Accelerometer","Gyroscope","Lin Accel","Gravity"};
        orientations = new String[]{"Normal Landscape", "Reverse Landscape",
                                    "Normal Portrait", "Reverse Portrait",
                                    "Screen Facing Up", "Screen Facing Down"};
        movements = new String[]{   "Moving Left","Moving Right","Moving Away From Body",
                                    "Moving Towards Body", "Raised into the air",
                                    "Dropping towards the floor"};
        currMvmnt=0;
    }
    public sensorClass(SensorManager x){
        setSensorManager(x);//set the sensor manager to the manager passed in from the main class
        initArr();//initialise the arrays that hold data
        setSensors(); //sets the sensors
        registerListeners(); //register the sensors

    }

    public void registerListeners(){
        if (!sm.registerListener(this, sGyro, SensorManager.SENSOR_DELAY_FASTEST)){
            Log.d("NoSensorError","This device does not have a gyroscope");
        }
        if(!sm.registerListener(this, sAccel, SensorManager.SENSOR_DELAY_FASTEST)){
            Log.d("NoSensorError","This device does not have an accelerometer");
        }
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
        Sensor s = event.sensor;
        int row=0;
        if(s.getType()==Sensor.TYPE_GYROSCOPE){
            row=1;

        }
        else if (s.getType()==Sensor.TYPE_ACCELEROMETER){

            //update linear acceleration and gravity once accelerometer readings change
            for (int a=0;a<3;a++){
                // Isolate the force of gravity with the low-pass filter.
                readings[3][a]=(alpha * readings[3][a]) + ((1.0f - alpha) * event.values[a]);

                // Remove the gravity contribution with the high-pass filter.
                readings[2][a] = event.values[a] - readings[3][a];

                //linear acceleration should be 0 if movement isn't fast enough
                readings[2][a]=Math.abs(readings[2][a])<ACTION_THRESHOLD?0.0f:readings[2][a]*100;
                currMvmnt=getCurrentMovement(event.values);
            }
            if (Math.abs(readings[2][0])>0.3)
                System.out.println("\n"+readings[2][0]+"\t"+readings[2][1]+"\t"+readings[2][2]);
        }
        System.arraycopy(event.values,0,readings[row],0,3);

    }

    private int getCurrentMovement(float[] values) {
        //action on the x axis
        if (Math.abs(readings[0][0])-Math.abs(values[0])>ACTION_THRESHOLD)
            return values[0]>0?0:1;
        else if (Math.abs(readings[0][1]-Math.abs(values[0]))>ACTION_THRESHOLD)
            return values[1]>0?2:3;
        return values[2]>0?4:5;
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

    public String getOrientationStr(){
        return orientations[getOrientation()];
    }
    public int getAction(){
        //for a device whose screen is facing upwards and is in normal portrait,
        // 0 is moving right, 1 is moving left, 2 is moving away from user's body.
        // 3 is moving towards user's body, 4 is raising the device vertically in the air
        // 5 is the device is moving towards the ground
        double[] linAccel = getCurrentSensorReading("linear acceleration");
        double[] gyro = getCurrentSensorReading("gyroscope");
        if (gyro[0]<GYRO_ACTION_THRESHOLD && gyro[1]<GYRO_ACTION_THRESHOLD && gyro[2]<GYRO_ACTION_THRESHOLD) {
            if (Math.abs(linAccel[0]) > Math.abs(linAccel[1]) && Math.abs(linAccel[0]) > Math.abs(linAccel[2]))
                return linAccel[0] < 0 ? 0 : 1;
            else if (Math.abs(linAccel[1]) > Math.abs(linAccel[0]) && Math.abs(linAccel[1]) > Math.abs(linAccel[2]))
                return linAccel[1] < 0 ? 2 : 3;
            return linAccel[2] < 0 ? 4 : 5;
        }
        return -1;
    }

    public String getActionString(){
        int y=getAction();
        if (y!=-1)
            return movements[currMvmnt];
        return "";
    }
}
