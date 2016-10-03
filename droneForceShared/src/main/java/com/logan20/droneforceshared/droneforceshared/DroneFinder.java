package com.logan20.droneforceshared.droneforceshared;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parrot.arsdk.ARSDK;
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
    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private List<ARDiscoveryDeviceService> deviceList;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private DroneFoundListener listener;
    private String TAG;

    public DroneFinder(DroneFoundListener listener){
        this.listener=listener;
        TAG=getClass().getSimpleName();
        ARSDK.loadSDKLibs();//load libraries for detection of drone
        initDiscoveryService();//initialise the discovery service used to find the drones
        registerReceivers();//register the receivers
    }

    public void stopFindDrones(){
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
                    startDiscovery();
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mArdiscoveryService=null;
                }
            };
        }
        //if service connection exists, check and see if discovery service has been initialised
        if (mArdiscoveryService==null){
            //if discovery service doesn't exist, create it and bind to it
            Intent i = new Intent((Context) listener,ARDiscoveryService.class);
            ((Context)listener).bindService(i,mArdiscoveryServiceConnection,Context.BIND_AUTO_CREATE);
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
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(((Context) listener));
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    @Override
    public void onServicesDevicesListUpdated() {
        if (mArdiscoveryService!=null){
            deviceList = mArdiscoveryService.getDeviceServicesArray();
            if (deviceList.size()>0){
                listener.onDroneFound(getDroneList());
            }
        }
    }

    private List<String> getDroneList(){
        //get list of drone names
        List<String> strList = new ArrayList<>();
        for (ARDiscoveryDeviceService x:deviceList){
            strList.add(x.getName());
        }
        return strList;
    }

    public ARDiscoveryDevice createDrone(int s) {
        return createDiscoveryDevice(deviceList.get(s));
    }

    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service){
        ARDiscoveryDevice drone = null;
        switch (ARDiscoveryService.getProductFromProductID(service.getProductID())){
            case ARDISCOVERY_PRODUCT_ARDRONE:
                try{
                    drone = new ARDiscoveryDevice();
                    //WIFI CONNECTION is used by ardrone
                    ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();
                    drone.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
                }
                catch (ARDiscoveryException e){
                    e.printStackTrace();
                }
                break;

            case ARDISCOVERY_PRODUCT_MINIDRONE:
                try{
                    drone = new ARDiscoveryDevice();
                    ARDiscoveryDeviceBLEService bleDeviceService = (ARDiscoveryDeviceBLEService)service.getDevice();
                    drone.initBLE(ARDiscoveryService.getProductFromProductID(service.getProductID()),((Context) listener),bleDeviceService.getBluetoothDevice());
                } catch (ARDiscoveryException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Log.d(TAG, "createDiscoveryDevice: Unknown device ");
                break;
        }
        return drone;
    }

    private void closeServices() {
        if (mArdiscoveryService != null){
            new Thread(new Runnable() {
                @Override
                public void run(){
                    mArdiscoveryService.stop();
                    ((Context)listener).unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }
    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(((Context) listener));
        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    public ARDISCOVERY_PRODUCT_ENUM getDroneType(int which) {
        return ARDiscoveryService.getProductFromProductID((deviceList.get(which)).getProductID());
    }
}