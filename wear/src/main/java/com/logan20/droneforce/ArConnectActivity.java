package com.logan20.droneforce;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.ARDrone;
import com.logan20.droneforceshared.droneforceshared.SensorClass;
import com.logan20.droneforceshared.droneforceshared.SensorInterface;

import java.io.IOException;

public class ArConnectActivity extends Activity implements SensorInterface{
    private final int CONNECT_TIMEOUT = 4000;
    private ARDrone drone;
    private  TextView view;
    private Thread droneThread;
    private Thread dronePilotThread;
    private SensorClass sensor;
    private String state;
    private volatile float[] params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_reading);
        view = (TextView) findViewById(R.id.tv_sensorText);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sensor = new SensorClass((SensorManager)getSystemService(Context.SENSOR_SERVICE),this);
        state = "Not Ready";
        params = new float[]{0,0,0,0};
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setText("Waiting for connection");
            }
        });
        initPilot();
        init();

    }

    private void initPilot() {
        dronePilotThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try {
                        if (params[0]==0&&params[1]==0&&params[2]==0&&params[3]==0){
                            drone.hover();
                        }
                        else {
                            drone.move(params[0]/2,params[1]/2,params[2]/2,params[3]/2);
                        }
                        Thread.sleep(25);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    private void init() {
        droneThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    // Create ARDrone object,
                    // connect to drone and initialize it.
                    drone = new ARDrone();
                    drone.connect();
                    drone.clearEmergencySignal();
                    // Wait until drone is ready
                    drone.waitForReady(CONNECT_TIMEOUT);
                    drone.trim();
                    Thread.sleep(4000);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setText("Ready");
                        }
                    });
                    Thread.sleep(2000);
                    drone.takeOff();
                    state = "Ready";
                    Thread.sleep(1000);
                    dronePilotThread.start();
                } catch(Throwable e)
                {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setText("Error, please restart drone");
                        }
                    });
                    state = "Not ready";
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        droneThread.start();
        Log.d("Hurr dur", "onResume: Starting deont THread");
    }

    @Override
    protected void onPause() {
        if (drone!=null){
            try {
                drone.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            drone=null;
        }
        dronePilotThread=null;
        droneThread = null;
        sensor.unregisterListeners();
        sensor= null;
        super.onPause();
    }

    @Override
    public void onDataChanged(final SensorClass.Direction direction, final float speed){
        if (!state.equals("Not Ready")){
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.setText("Moving with a speed of "+speed+" with direction "+direction.name());
                }
            });
            switch (direction){
                case LEFT:
                    params[0]=-speed;
                    params[1]=0;
                    params[2]=0;
                    params[3]=0;
                    break;
                case RIGHT:
                    params[0]=speed;
                    params[1]=0;
                    params[2]=0;
                    params[3]=0;
                    break;
                case UP:
                    params[0]=0;
                    params[1]=0;
                    params[2]=speed;
                    params[3]=0;
                    break;
                case DOWN:
                    if (speed<10){
                        params[0]=0;
                        params[1]=0;
                        params[2]=0;
                        params[3]=0;
                    }
                    else if (speed<25){
                        params[0]=0;
                        params[1]=0;
                        params[2]=-speed;
                        params[3]=0;
                    }
                    else{
                        try {
                            drone.sendEmergencySignal();
                            dronePilotThread.interrupt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case FORWARD:
                    params[0]=0;
                    params[1]=-speed;
                    params[2]=0;
                    params[3]=0;
                    break;
                case BACKWARD:
                    params[0]=0;
                    params[1]=speed;
                    params[2]=0;
                    params[3]=0;
                    break;
            }
        }
    }
}
