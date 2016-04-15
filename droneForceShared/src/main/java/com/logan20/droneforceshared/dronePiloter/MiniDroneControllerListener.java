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
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFeatureMiniDrone;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;

/**
 * Created by kwasi on 4/14/2016.
 */
public class MiniDroneControllerListener implements ARDeviceControllerListener{
    private ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState;
    private sensorClass sensor;
    private static final long THREAD_TIMEOUT = 200;
    private ARDeviceController deviceController;

    public MiniDroneControllerListener(Context context, ARDeviceController deviceController){
        sensor=new sensorClass((SensorManager)context.getSystemService(Context.SENSOR_SERVICE));
        this.deviceController=deviceController;
        pilotFromSensor();
    }

    private void pilotFromSensor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String y = sensor.getOrientationRelativeSpeedReadings()[0]+"\t";
                    y+= sensor.getOrientationRelativeSpeedReadings()[1] + "\t";
                    y+= sensor.getOrientationRelativeSpeedReadings()[2];
                    Log.d("SENSOR",y);
                    Log.d("State",flyingState+" ");
                    if (flyingState==ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED&&sensor.getOrientationRelativeSpeedReadings()[2]>0){
                        deviceController.getFeatureMiniDrone().sendPilotingTakeOff();
                    }
                    try{
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
