package com.logan20.droneforceshared.dronePiloter;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import com.logan20.droneforceshared.sensorClass;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFeatureMiniDrone;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;

/**
 * Created by kwasi on 4/14/2016.
 */
public class MiniDroneControllerListener implements ARDeviceControllerListener{
    private ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState;
    private sensorClass sensor;
    private static final long THREAD_TIMEOUT = 200;
    private final float EMERGENCYLAND = 20;
    private ARDeviceController deviceController;
    private float[] prevReadings,currReadings;
    private static float SENSITIVITY = -5;

    public MiniDroneControllerListener(Context context, ARDeviceController deviceController){
        sensor=new sensorClass((SensorManager)context.getSystemService(Context.SENSOR_SERVICE));
        this.deviceController=deviceController;
        pilotFromSensor();
    }

    private void pilotFromSensor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    prevReadings=currReadings;
                    currReadings=sensor.getOrientationRelativeSpeedReadings();

                    if (currReadings[0]!=0.0f&&currReadings[1]!=0.0f&&currReadings[2]!=0.0f){
                        //only if we have significant speed movement
                        switch (getState()){
                            case "landed state":
                                //the only thing you can do when landed is take off
                                if (currReadings[2]>0){
                                    takeOff();
                                    Log.d("TAKEOFF","Takeoff initiated with speed :"+currReadings[2]);
                                }
                                break;
                            case "emergency state":
                                Log.d("Emergency","Emergency state");
                                //prompt the user of battery level or that emergency state initiated
                                break;
                            case "taking off state":
                                //if you're taking off, the only thing you can do is go higher
                                if (currReadings[2]<0){
                                    deviceController.getFeatureMiniDrone().setPilotingPCMDYaw((byte)currReadings[2]);
                                }
                                break;
                            case "flying state":
                                pilot();
                                break;
                            case "hovering state":
                                pilot();
                                break;
                            default:
                                break;
                        }
                    }
                    Log.d("State",flyingState+" ");
                    try{
                        Thread.sleep(THREAD_TIMEOUT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void pilot() {
        if (currReadings[0]!=0){
            setHorizontalMotion(currReadings[0]*SENSITIVITY);
        }
        if (currReadings[1]!=0){
            setDepthMotion(currReadings[1]*SENSITIVITY);
        }
        if (currReadings[2]!=0){
            setVerticalMotion(currReadings[2]*SENSITIVITY);
        }
    }

    private void setHorizontalMotion(float currReading) {
        deviceController.getFeatureMiniDrone().setPilotingPCMDPitch((byte)currReading);
        Log.d("MOVEMENT","Moving hhorizontaly");
    }
    private void setDepthMotion(float currReading) {
        if (currReading>EMERGENCYLAND){
            deviceController.getFeatureMiniDrone().sendPilotingEmergency();
            Log.d("MOVEMENT","Emergency land");
        }
        else{
            deviceController.getFeatureMiniDrone().setPilotingPCMDRoll((byte)currReading);
            Log.d("MOVEMENT","move toward/away");
        }

    }
    private void setVerticalMotion(float currReading) {
        deviceController.getFeatureMiniDrone().setPilotingPCMDYaw((byte)currReading);
        Log.d("MOVEMENT","Moving vertically");
    }


    private void takeOff() {
        deviceController.getFeatureMiniDrone().sendPilotingTakeOff();
    }

    String getState(){
        if (flyingState!=null){
            return (flyingState+"").toLowerCase();
        }
        return null;
    }
    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        switch (newState){
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                Log.d("STATE","Controller is RUNNING");
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                Log.d("STATE","Controller is STOPPED...starting controller");
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                Log.d("STATE","Controller is STARTING");
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                Log.d("STATE","Controller is STOPPING");
                break;
            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if (elementDictionary!=null){
            //if the command recieved is battery update
            if (commandKey==ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args!=null){
                    Integer batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    Log.d("BATTERY","New battery is : "+batValue+"%");
                }
            }
            else if (commandKey==ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args!=null){
                    Integer state = (Integer) args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                    flyingState = ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(state);

                    Log.d("STATE",flyingState+" ");
                }
            }
        }
        else{
            Log.d("ERR","Element Dictionary is null");
        }
    }


}
