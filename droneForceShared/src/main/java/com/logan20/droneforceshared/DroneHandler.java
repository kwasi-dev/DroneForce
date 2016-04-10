package com.logan20.droneforceshared;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwasi on 4/10/2016.
 */
public class DroneHandler implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate,ARDeviceControllerListener{
    private final Context context;
    private final int MAX_DRONES = 10;
    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private List<ARDiscoveryDeviceService> deviceList;
    private ARDiscoveryDevice device;
    private ARDeviceController deviceController;

    public DroneHandler(Context context){
        this.context = context;
        ARSDK.loadSDKLibs(); //load the native libraries
        initDiscoveryService(); //start the drone discovery
        registerReceivers(); //register the reciever that will listen for the drones
    }

    private void initDiscoveryService(){
        //create the service connection
        if (mArdiscoveryServiceConnection == null){
            mArdiscoveryServiceConnection = new ServiceConnection(){

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mArdiscoveryService=((ARDiscoveryService.LocalBinder)service).getService();
                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mArdiscoveryService=null;
                }
            };
        }

        if (mArdiscoveryService==null){
            //if discovery service doesn't exist, bind to it
            Intent i = new Intent(context,ARDiscoveryService.class);
            context.bindService(i,mArdiscoveryServiceConnection,Context.BIND_AUTO_CREATE);
        }
        else{
            //if discovery service already exists, start discovery
            mArdiscoveryService.start();
        }
    }

    private void startDiscovery(){
        if (mArdiscoveryService!=null){
            mArdiscoveryService.start();
        }
    }

    private void registerReceivers()
    {
        mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }
    @Override
    public void onServicesDevicesListUpdated() {
        Log.d("DRONE LIST","Drone list updated");
        if (mArdiscoveryService!=null){
            deviceList = mArdiscoveryService.getDeviceServicesArray();
        }
    }

    private void closeServices() {
        if (mArdiscoveryService != null){
            new Thread(new Runnable() {
                @Override
                public void run(){
                    mArdiscoveryService.stop();
                    context.unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }

    private void unregisterRecievers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }
    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service){
        ARDiscoveryDevice drone = null;
        if ((service != null) && (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE.equals(ARDiscoveryService.getProductFromProductID(service.getProductID())))){
            try{
                drone = new ARDiscoveryDevice();
                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();
                drone.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
            }
            catch (ARDiscoveryException e){
                e.printStackTrace();
            }
        }
        Log.d("Set Complete","setting of drone complete");
        return drone;
    }

    public CharSequence[] getDroneList(){
        if (deviceList==null)
            return null;

        List<String> strList = new ArrayList<String>();
        for (ARDiscoveryDeviceService x:deviceList){
            strList.add(x.getName());
        }
        return strList.toArray(new CharSequence[deviceList.size()]);
    }

    public void setActiveDrone(int activeDrone) {
        Log.d ("ACTIVE","Setting active drone: "+deviceList.get(activeDrone).getName());
        device = createDiscoveryDevice(deviceList.get(activeDrone));
        //unregister listeners etc after setting drone
        unregisterRecievers();
        closeServices();

        //add device controller
        addDeviceController();

    }

    private void addDeviceController() {
        try{
            deviceController = new ARDeviceController (device);
            deviceController.addListener(this);
        }
        catch (ARControllerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        switch (newState){
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                Log.d("STATE","RUNING");
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                Log.d("STATE","STOPPED");
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                Log.d("STATE","STARTING");
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                Log.d("STATE","STOPPING");
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
        if (elementDictionary != null){
            // if the command received is a battery state changed
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null){
                    Integer batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    Log.d("BATTERY LEVEL",batValue+"%");
                }
            }
        }
        else
        {
            Log.e("ERROR", "elementDictionary is null");
        }
    }
}
