package com.logan20.droneforce;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.ArrayList;
import java.util.List;

public class DroneFinder implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {
    private final Context context;
    private final Activity activity;
    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private List<ARDiscoveryDeviceService> deviceList;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private ARDiscoveryDevice drone;
    private String droneType,droneName;
    private ARDeviceController deviceController;
    private Handler handler;
    private ProgressDialog progress;
    private DroneListenerClass listener;
    public DroneFinder(final Context context,final Activity activity){
        this.context=context; //set context in event i need to do anything on main context
        this.activity=activity;
        handler=new Handler(context.getMainLooper());
        ARSDK.loadSDKLibs();//load libraries for detection of drone
        findDrones(); //start required services necessary for finding the drone
        showProgress();//show progress dialog that it's looking for drones
    }

    private void showProgress() {
        progress = new ProgressDialog(context);
        progress.setMessage("Looking for drones");
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(9000);
                    if (progress!=null){
                        progress.dismiss();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity.getApplicationContext(),"No drones found, scanning for drones will continue in the background and the device will auto-connect to the drone",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void findDrones() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDiscoveryService();//initialise the discovery service used to find the drones
                registerReceivers();//register the recievers
            }
        }).start();
    }

    private void stopFindDrones(){
        unregisterReceivers();//unregister the receivers
        closeServices();//close the services
    }
    private void initDiscoveryService(){
        //create the service connection if it doesn't exist
        if (mArdiscoveryServiceConnection == null){
            mArdiscoveryServiceConnection = new ServiceConnection(){
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mArdiscoveryService=((ARDiscoveryService.LocalBinder)service).getService();
                    Log.d("SERVICE CONNECTED","Service to listen to drone has been connected");
                    startDiscovery();
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("SERVICE DISCONNECTED","Service to listen to drone has been disconnected");
                    mArdiscoveryService=null;
                }
            };
        }
        //if service connection exists, check and see if discovery service has been initialised
        if (mArdiscoveryService==null){
            //if discovery service doesn't exist, create it and bind to it
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
            Log.d("DISCOVERY BEGIN","Searching for drones has begun");
        }
    }

    private void registerReceivers(){
        mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }


    @Override
    public void onServicesDevicesListUpdated() {
        Log.d("DRONE LIST","Drone list updated");
        if (mArdiscoveryService!=null){
            deviceList = mArdiscoveryService.getDeviceServicesArray();
            if (deviceList.size()>0){
                createDrone();
            }
        }
    }

    private void createDrone() {
        if (deviceList.size()==1){
            //if only one drone is found, auto connect to the drone
            drone = createDiscoveryDevice(deviceList.get(0));
            if (progress!=null){
                progress.dismiss();
                progress=null;
            }
            droneName=deviceList.get(0).getName();
            Toast.makeText(context.getApplicationContext(),"One drone found, auto-connecting",Toast.LENGTH_LONG).show();
        }
        else{
            //get list of drone names
            List<String> strList = new ArrayList<>();
            for (ARDiscoveryDeviceService x:deviceList){
                strList.add(x.getName());
            }

            //ask the user which drone they want to connect to
            new AlertDialog.Builder(context)
                    .setTitle("Choose a drone:")
                    .setItems(strList.toArray(new CharSequence[deviceList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            droneName=deviceList.get(which).getName();
                            drone=createDiscoveryDevice(deviceList.get(which));//connect to the drone selected
                        }
                    })
                    .show();
        }
        if (drone!=null){
            Log.d("SUCCESS","Connection to the drone was successful");
            switch(droneType){
                case "minidrone":
                    try{
                        stopFindDrones(); //close off connection when finished
                        deviceController= new ARDeviceController(drone);//create the controller
                        listener = new DroneListenerClass(activity,context,deviceController,droneName,droneType);
                        deviceController.addListener(listener);//add the listener
                    } catch (ARControllerException e) {
                        e.printStackTrace();
                    }
                    break;
                case "bebop2":
                    try{
                        stopFindDrones(); //close off connection when finished
                        deviceController= new ARDeviceController(drone);//create the controller
                        listener = new DroneListenerClass(activity,context,deviceController,droneName,droneType);
                        deviceController.addListener(listener);//add the listener
                    } catch (ARControllerException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    Log.d("SUPPORT","Program doesn't yet support this type of drone");
                    break;
            }
        }
        if (deviceController!=null){
            startDeviceController();
        }
    }

    public void startDeviceController() {
        deviceController.start();
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    public void stopDeviceController(){
        if (deviceController!=null){
            deviceController.stop();//stops the background tasks etca
        }
    }

    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service){
        Log.d("CREATION","Attempting to connect to: "+service.getName());
        ARDiscoveryDevice drone = null;
        if ((ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE.equals(ARDiscoveryService.getProductFromProductID(service.getProductID())))){
            try{
                drone = new ARDiscoveryDevice();
                //WIFI CONNECTION is used by ardrone
                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();
                drone.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
                droneType="ardrone";
            }
            catch (ARDiscoveryException e){
                e.printStackTrace();
            }
        }
        else if ((ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_BEBOP_2.equals(ARDiscoveryService.getProductFromProductID(service.getProductID())))){
            try{
                drone = new ARDiscoveryDevice();
                //WIFI CONNECTION is used by bebop
                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();
                drone.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_BEBOP_2, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
                droneType="bebop2";
            }
            catch (ARDiscoveryException e){
                e.printStackTrace();
            }
        }
        else if (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_MINIDRONE.equals(ARDiscoveryService.getProductFromProductID(service.getProductID()))){
            try{
                drone = new ARDiscoveryDevice();
                //Bluetooth connection for minidrone
                Log.d("MINIDRONE DETECTED","minidrone is the drone detected, using bluetooth");
                ARDiscoveryDeviceBLEService bleDeviceService = (ARDiscoveryDeviceBLEService)service.getDevice();
                drone.initBLE(ARDiscoveryService.getProductFromProductID(service.getProductID()),context,bleDeviceService.getBluetoothDevice());
                droneType="minidrone";
            } catch (ARDiscoveryException e) {
                e.printStackTrace();
            }
        }


        Log.d("Set Complete","setting of drone complete");
        return drone;
    }

    private void closeServices() {
        if (mArdiscoveryService != null){
            new Thread(new Runnable() {
                @Override
                public void run(){
                    mArdiscoveryService.stop();
                    context.unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                    Log.d("CLOSE SERVICES","Closing the service");
                }
            }).start();
        }
    }
    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    public void stopAll() {
        removeListener();
        stopDeviceController();
    }

    private void removeListener() {
        if (listener!=null){
            listener.stopAll();
            if (deviceController!=null){
                deviceController.removeListener(listener);
            }
        }
    }

}