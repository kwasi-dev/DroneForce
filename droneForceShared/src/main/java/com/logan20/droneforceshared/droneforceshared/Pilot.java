package com.logan20.droneforceshared.droneforceshared;

import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;

import java.io.IOException;

/**
 * Created by kwasi on 9/14/2016.
 * Logic:   When the user moves their hand, the accelerometer reading will increase until movement
 *          stops or speed gets constant. The drone's movement will be set to the max reading whenever
 *          it is read to ensure responsiveness and will be adjusted based on new readings.
 *
 *          When the user stops moving their hand a peak in the opposite direction on the same axis
 *          [Within OPPOSITEPEAKTIME milliseconds] will appear on readings indicating a stop in
 *          movement. When this happens, allow new readings to take place.
 *
 *          the left right movement (drone roll) is params[0];
 *          the front back movement (drone pitch) is params[1];
 *          the up down movement (drone gaz) is params[2];
 *
 */

public class Pilot implements SensorInterface{
    private final ARDISCOVERY_PRODUCT_ENUM droneType;
    private final ARDeviceController controller;
    private final DroneState controllerListener;
    private Enum flyingState;
    private String TAG;

    private int autoPilotEnable;
    private float[] params;
    private Thread pilotThread;

    public Pilot(ARDISCOVERY_PRODUCT_ENUM droneType, ARDeviceController controller, DroneState controllerListener){
        this.TAG = getClass().getSimpleName();
        this.controllerListener  = controllerListener;
        this.autoPilotEnable = 0;
        params = new float[]{0,0,0,0};
        this.droneType = droneType;
        this.controller = controller;
        init();

    }

    private void init() {
        pilotThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try{
                        move();
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }




    @Override
    public void onDataChanged(SensorClass.Direction direction, float speed) {
        switch(direction){
            case UP:
                if (speed>10){
                    takeOff();
                    pilotThread.start();
                }
                else{
                    params[0]=0;
                    params[1]=0;
                    params[2]=speed;
                    params[3]=0;
                }

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
                    emergencyLand();
                }
                break;
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
            case FORWARD:
                params[0]=0;
                params[1]=speed;
                params[2]=0;
                params[3]=0;
                break;
            case BACKWARD:
                params[0]=0;
                params[1]=-speed;
                params[2]=0;
                params[3]=0;
                break;
        }
    }

    public void autoTakeOff(){
        switch (droneType){
            case ARDISCOVERY_PRODUCT_MINIDRONE:
                autoPilotEnable =1-autoPilotEnable;
                controller.getFeatureMiniDrone().sendPilotingAutoTakeOffMode((byte)autoPilotEnable);
                break;
        }
    }

    public void emergencyLand(){
        switch (droneType){
            case ARDISCOVERY_PRODUCT_MINIDRONE:
                controller.getFeatureMiniDrone().sendPilotingEmergency();
                break;
        }
    }
    private void move() {
        switch (droneType){
            case ARDISCOVERY_PRODUCT_MINIDRONE:
                controller.getFeatureMiniDrone().setPilotingPCMD((byte)1,(byte)params[0],(byte)params[1],(byte)params[2],(byte)params[3],(int)System.currentTimeMillis());
        }
    }

    private void land(){
        switch (droneType){
            case ARDISCOVERY_PRODUCT_MINIDRONE:
                controller.getFeatureMiniDrone().sendPilotingLanding();
                break;
        }
    }

    private void takeOff(){
        switch (droneType){
            case ARDISCOVERY_PRODUCT_MINIDRONE:
                controller.getFeatureMiniDrone().sendPilotingTakeOff();
        }
    }

    private void flip(int dir){
        // direction 0 = right
        // direction 1 = front
        // direction 2 = left
        // direction 3 = back
        switch (droneType){
            case ARDISCOVERY_PRODUCT_MINIDRONE:
                controller.getFeatureMiniDrone().sendAnimationsFlip(dir==0?ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_RIGHT:dir==1?ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_FRONT:dir==2?ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_LEFT:ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_BACK);
                break;
        }
    }

}