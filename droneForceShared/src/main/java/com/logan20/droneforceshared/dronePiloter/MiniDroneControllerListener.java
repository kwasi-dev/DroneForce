package com.logan20.droneforceshared.dronePiloter;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
    private final float EMERGENCYLAND = 40;
    private Context context;
    private ARDeviceController deviceController;
    private float[] currReadings;
    public int toggle=0;
    private ProgressDialog progress;
    private Thread pilotThread;

    public MiniDroneControllerListener(Context ctx, ARDeviceController dvc){
        deviceController=dvc;
        context=ctx;
        pilotFromSensor();
        initProgress();
    }

    private void initProgress() {
        progress = new ProgressDialog(context);
        progress.setMessage("Waiting for OK from drone");
        progress.show();
    }
    private void stopProgress(){
        if(progress!=null){
            progress.dismiss();
            progress=null;
        }

    }
    public void toggleAutoTakeoff(){
        if (deviceController!=null){
            toggle=1-toggle;
            deviceController.getFeatureMiniDrone().sendPilotingAutoTakeOffMode((byte)toggle);
            if (context!=null){
                if (toggle == 1) {
                    Toast.makeText(context.getApplicationContext(),"Auto Take-off Mode Enabled",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context.getApplicationContext(),"Auto Take-off Mode Disabled",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void pilotFromSensor() {
        pilotThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    if (sensor!=null){
                        currReadings=sensor.getOrientationRelativeSpeedReadings();
                        if (currReadings[0]!=0.0f&&currReadings[1]!=0.0f&&currReadings[2]!=0.0f){
                            //only if we have significant speed movement
                            switch (getState()){
                                case "landed state":
                                    //the only thing you can do when landed is take off
                                    if (currReadings[2]>0){
                                        takeOff();
                                        stopProgress();
                                        Log.d("TAKEOFF","Takeoff initiated with speed :"+currReadings[2]);
                                    }
                                    else{
                                        pilot();
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
            }
        });
        pilotThread.start();
    }

    public void stopPilot(){
        if (pilotThread!=null){
            pilotThread.interrupt();
        }
    }
    private void pilot() {
        float vSpeed = currReadings[2];
        float hSpeed = currReadings[0];
        float dSPeed = currReadings[1];
        int timestamp = (int)System.currentTimeMillis();
        if (vSpeed>EMERGENCYLAND||hSpeed>EMERGENCYLAND){//land if extreme movement
            deviceController.getFeatureMiniDrone().sendPilotingEmergency();
        }
        else{
            deviceController.getFeatureMiniDrone().setPilotingPCMD((byte)1,(byte)hSpeed,(byte)dSPeed,(byte)0,(byte)vSpeed,timestamp);
        }
    }

    private void takeOff() {
        deviceController.getFeatureMiniDrone().sendPilotingTakeOff();
    }

    @Nullable
    private String getState(){
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
                if (sensor==null)
                    sensor=new sensorClass((SensorManager)context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE));
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
                    if (ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(state)!=flyingState){
                        Log.d("STATE",flyingState+" ");
                    }

                    flyingState = ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(state);
                }
            }
        }
        else{
            Log.d("ERR","Element Dictionary is null");
        }
    }


}
